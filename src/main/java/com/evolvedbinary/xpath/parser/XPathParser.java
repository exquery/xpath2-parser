/**
 * XPath 2 Parser
 * A Parser for XPath 2
 * Copyright (C) 2016 Evolved Binary Ltd.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.evolvedbinary.xpath.parser;

import com.evolvedbinary.functional.Either;
import com.evolvedbinary.xpath.parser.ast.partial.*;
import org.parboiled.Action;
import org.parboiled.BaseParser;
import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;

import com.evolvedbinary.xpath.parser.ast.*;
import org.parboiled.support.Var;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@BuildParseTree
public class XPathParser extends BaseParser<AbstractASTNode> {

    private final boolean enableActions;

    public XPathParser(final Boolean enableActions) {
        this.enableActions = enableActions;
    }

    //TODO(AR) remove!
    @Override
    public boolean push(AbstractASTNode value) {
        if(enableActions) {
            return super.push(value);
        } else {
            return true;
        }
    }

    @Override
    public AbstractASTNode pop() {
        if(enableActions) {
            return super.pop();
        } else {
            return null;
        }
    }

    @Override
    public AbstractASTNode peek() {
        if(enableActions) {
            return super.peek();
        } else {
            return null;
        }
    }

    @Override
    public boolean dup() {
        if(enableActions) {
            return super.dup();
        } else {
            return true;
        }

        //TODO(AR)
        //getContext().getValueStack().peek()
    }

    <T> AbstractASTNode complete(final T value, final AbstractASTNode partial) {
        if(!(partial instanceof PartialASTNode)) {
            throw new IllegalStateException("Cannot complete non-partial AST Node: " + partial.getClass());
        }
        return ((PartialASTNode<?, T>)partial).complete(value);
    }

    AbstractASTNode completeOptional(AbstractASTNode partial) {
        while(partial instanceof PartialASTNode) {
            partial = ((PartialASTNode)partial).complete(null);
        }
        return partial;
    }

    /**
     * Pops all nodes from the value stack which are assignable
     * from a specific class
     *
     * @param type The type of nodes to pop
     * @return A list of the nodes in stack order
     */
    <T extends AbstractASTNode> List<T> popAll(final Class<T> type) {
        final List<T> items = new ArrayList<T>();
        while(!getContext().getValueStack().isEmpty() &&
                peek().getClass().isAssignableFrom(type)) {
            items.add((T)pop());
        }

        return items;
    }

    /**
     * Similar to {@link #popAll(Class)} except that the
     * order of nodes is reversed
     *
     * @param type The type of nodes to pop
     * @return A list of the nodes in reverse stack order
     */
    <T extends AbstractASTNode> List<T> popAllR(final Class<T> type) {
        final List<T> items = popAll(type);
        Collections.reverse(items);
        return items;
    }

    /**
     * Whitespace handling
     */
    Rule WS() {
        //return Optional(FirstOf(Xml_S(), Comment()));
        return Optional(Xml_S());
    }


    /* XPath Rules */


    /**
     * [1] XPath ::= Expr
     */
    Rule XPath() {
        return Expr();
    }

    /**
     * [2] Expr ::= ExprSingle ("," ExprSingle)*
     */
    Rule Expr() {
        return Sequence(ExprSingle(), ZeroOrMore(Sequence(',', WS(), ExprSingle())));
    }

    /**
     * [3] ExprSingle ::=   ForExpr
     *                      | QuantifiedExpr
     *                      | IfExpr
     *                      | OrExpr
     */
    Rule ExprSingle() {
        return FirstOf(
                ForExpr(),
                QuantifiedExpr(),
                IfExpr(),
                OrExpr()
        );
    }

    /**
     * [4] ForExpr ::= SimpleForClause "return" ExprSingle
     */
    Rule ForExpr() {
        return Sequence(SimpleForClause(), "return", WS(), ExprSingle());
    }

    /**
     * [5] SimpleForClause ::= "for" "$" VarName "in" ExprSingle ("," "$" VarName "in" ExprSingle)*
     */
    Rule SimpleForClause() {
        return Sequence(
                "for", WS(), '$', WS(), VarName(), "in", WS(), ExprSingle(),
                    ZeroOrMore(',', WS(), '$', WS(), VarName(), "in", WS(), ExprSingle())
        );
    }

    /**
     * [6] QuantifiedExpr ::= ("some" | "every") "$" VarName "in" ExprSingle ("," "$" VarName "in" ExprSingle)* "satisfies" ExprSingle
     */
    Rule QuantifiedExpr() {
        return Sequence(
                FirstOf("some", "every"), WS(), '$', WS(), VarName(), "in", WS(), ExprSingle(),
                    ZeroOrMore(',', WS(), '$', WS(), VarName(), "in", WS(), ExprSingle()),
                        "satisfies", WS(), ExprSingle()
        );
    }

    /**
     * [7] IfExpr ::= "if" "(" Expr ")" "then" ExprSingle "else" ExprSingle
     */
    Rule IfExpr() {
        return Sequence(
                "if", WS(), '(', WS(), Expr(), ')', WS(), "then", WS(), ExprSingle(), "else", WS(), ExprSingle()
        );
    }

    /**
     * [8] OrExpr ::= AndExpr ( "or" AndExpr )*
     */
    Rule OrExpr() {
        return Sequence(AndExpr(), ZeroOrMore(Sequence("or", WS(), AndExpr())));
    }

    /**
     * [9] AndExpr ::= ComparisonExpr ( "and" ComparisonExpr )*
     */
    Rule AndExpr() {
        return Sequence(ComparisonExpr(), ZeroOrMore(Sequence("and", WS(), ComparisonExpr())));
    }

    /**
     * [10] ComparisonExpr ::=  RangeExpr ( (ValueComp
     *                          | GeneralComp
     *                          | NodeComp) RangeExpr )?
     */
    Rule ComparisonExpr() {
        return Sequence(
                RangeExpr(),
                Optional(Sequence(
                        FirstOf(ValueComp(), NodeComp(), GeneralComp()),
                        RangeExpr()
                ))
        );
    }

    /**
     * [11] RangeExpr ::= AdditiveExpr ( "to" AdditiveExpr )?
     */
    Rule RangeExpr() {
        return Sequence(AdditiveExpr(), Optional(Sequence("to", WS(), AdditiveExpr())));
    }

    /**
     * [12] AdditiveExpr ::= MultiplicativeExpr ( ("+" | "-") MultiplicativeExpr )*
     */
    Rule AdditiveExpr() {
        return Sequence(MultiplicativeExpr(), ZeroOrMore(Sequence(FirstOf('+', '-'), WS(), MultiplicativeExpr())));
    }

    /**
     * [13] MultiplicativeExpr ::= UnionExpr ( ("*" | "div" | "idiv" | "mod") UnionExpr )*
     */
    Rule MultiplicativeExpr() {
        return Sequence(UnionExpr(), ZeroOrMore(Sequence(FirstOf('*', "idiv", "div", "mod"), WS(), UnionExpr())));
    }

    /**
     * [14] UnionExpr ::= IntersectExceptExpr ( ("union" | "|") IntersectExceptExpr )*
     */
    Rule UnionExpr() {
        return Sequence(IntersectExceptExpr(), ZeroOrMore(Sequence(FirstOf("union", '|'), WS(), IntersectExceptExpr())));
    }

    /**
     * [15] IntersectExceptExpr ::= InstanceofExpr ( ("intersect" | "except") InstanceofExpr )*
     */
    Rule IntersectExceptExpr() {
        return Sequence(InstanceofExpr(), ZeroOrMore(Sequence(FirstOf("intersect", "except"), WS(), InstanceofExpr())));
    }

    /**
     * [16] InstanceofExpr ::= TreatExpr ( "instance" "of" SequenceType )?
     */
    Rule InstanceofExpr() {
        return Sequence(TreatExpr(), Optional(Sequence("instance", WS(), "of", WS(), SequenceType())));

    }

    /**
     * [17] TreatExpr ::= CastableExpr ( "treat" "as" SequenceType )?
     */
    Rule TreatExpr() {
        return Sequence(CastableExpr(), Optional(Sequence("treat", WS(), "as", WS(), SequenceType())));
    }

    /**
     * [18] CastableExpr ::= CastExpr ( "castable" "as" SingleType )?
     */
    Rule CastableExpr() {
        return Sequence(CastExpr(), Optional(Sequence("castable", WS(), "as", WS(), SingleType())));
    }

    /**
     * [19] CastExpr ::= UnaryExpr ( "cast" "as" SingleType )?
     */
    Rule CastExpr() {
        return Sequence(UnaryExpr(), Optional(Sequence("cast", WS(), "as", WS(), SingleType())));

    }

    /**
     * [20] UnaryExpr ::= ("-" | "+")* ValueExpr
     */
    Rule UnaryExpr() {
        return Sequence(ZeroOrMore('-', '+'), WS(), ValueExpr());
    }

    /**
     * [21] ValueExpr ::= PathExpr
     */
    Rule ValueExpr() {
        return PathExpr();
    }

    /**
     * [22] GeneralComp ::= "=" | "!=" | "<" | "<=" | ">" | ">="
     */
    Rule GeneralComp() {
        return Sequence(FirstOf("<=", "!=", ">=", '<', '=', '>'), WS());
    }

    /**
     * [23] ValueComp ::= "eq" | "ne" | "lt" | "le" | "gt" | "ge"
     */
    Rule ValueComp() {
        return Sequence(FirstOf("eq", "ne", "lt", "le", "gt", "ge"), WS());
    }

    /**
     * [24] NodeComp ::= "is" | "<<" | ">>"
     */
    Rule NodeComp() {
        return Sequence(FirstOf("is", "<<", ">>"), WS());
    }

    /**
     * [25] PathExpr ::=    ("/" RelativePathExpr?)
     *                      | ("//" RelativePathExpr)
     *                      | RelativePathExpr
     */
    Rule PathExpr() {
        return FirstOf(
                Sequence("//", WS(), RelativePathExpr()),
                Sequence('/', WS(), Optional(RelativePathExpr())),
                RelativePathExpr()
        );
    }

    /**
     * [26] RelativePathExpr ::=    StepExpr (("/" | "//") StepExpr)*
     */
    Rule RelativePathExpr() {
        return Sequence(
                StepExpr(),
                ZeroOrMore(
                        Sequence(
                                FirstOf("//", '/'), WS(),
                                StepExpr()
                        )
                )
        );
    }

    /**
     * [27] StepExpr ::= FilterExpr | AxisStep
     */
    Rule StepExpr() {
        return FirstOf(FilterExpr(), AxisStep());
    }

    /**
     * [28] AxisStep ::= (ReverseStep | ForwardStep) PredicateList
     */
    Rule AxisStep() {
        return Sequence(FirstOf(ReverseStep(), ForwardStep()), PredicateList());
    }

    /**
     * [29] ForwardStep ::= (ForwardAxis NodeTest) | AbbrevForwardStep
     */
    Rule ForwardStep() {
        return FirstOf(
                Sequence(
                        ForwardAxis(), push(new PartialStep((Axis)pop())),
                        NodeTest(), push(complete(pop(), pop()))
                ),
                AbbrevForwardStep()
        );
    }

    /**
     * [30] ForwardAxis ::= ("child" "::")
     *                      | ("descendant" "::")
     *                      | ("attribute" "::")
     *                      | ("self" "::")
     *                      | ("descendant-or-self" "::")
     *                      | ("following-sibling" "::")
     *                      | ("following" "::")
     *                      | ("namespace" "::")
     */
    Rule ForwardAxis() {
        return Sequence(FirstOf(
                "child",
                "attribute",
                "self",
                "descendant-or-self",
                "descendant",
                "following-sibling",
                "following",
                "namespace"), push(Axis.fromSyntax(match())), WS(),
                "::", WS());
    }

    /**
     * [31] AbbrevForwardStep ::= "@"? NodeTest
     */
    Rule AbbrevForwardStep() {
        return FirstOf(
                Sequence('@', WS(), NodeTest(), push(new Step(new Axis(Axis.Direction.ATTRIBUTE), (NodeTest)pop()))),
                Sequence(NodeTest(), push(new Step(new Axis(Axis.Direction.CHILD), (NodeTest)pop())))
        );
    }

    /**
     * [32] ReverseStep ::= (ReverseAxis NodeTest) | AbbrevReverseStep
     */
    Rule ReverseStep() {
        return FirstOf(
                Sequence(
                        ReverseAxis(), push(new PartialStep((Axis)pop())),
                        NodeTest(), push(complete(pop(), pop()))
                ),
                AbbrevReverseStep()
        );
    }

    /**
     * [33] ReverseAxis ::= ("parent" "::")
     *                      | ("ancestor" "::")
     *                      | ("preceding-sibling" "::")
     *                      | ("preceding" "::")
     *                      | ("ancestor-or-self" "::")
     */
    Rule ReverseAxis() {
        return Sequence(FirstOf(
                "parent",
                "ancestor-or-self",
                "ancestor",
                "preceding-sibling",
                "preceding"), push(Axis.fromSyntax(match())), WS(),
                "::", WS());
    }

    /**
     * [34] AbbrevReverseStep ::= ".."
     */
    Rule AbbrevReverseStep() {
        return Sequence("..", push(new Step(new Axis(Axis.Direction.PARENT), new AnyKindTest())), WS());
    }

    /**
     * [35] NodeTest ::= KindTest | NameTest
     */
    Rule NodeTest() {
        return FirstOf(KindTest(), NameTest());
    }

    /**
     * [36] NameTest ::= QName | Wildcard
     */
    Rule NameTest() {
        return Sequence(FirstOf(Wildcard(), QName()), push(new NameTest((QNameW)pop())));
    }

    /**
     * [37] Wildcard ::=    "*"
     *                      | (NCName ":" "*")
     *                      | ("*" ":" NCName)      //ws: explicit
     */
    Rule Wildcard() {
        return FirstOf(
                Sequence(NCName(), push(new QNameW(match(), QNameW.WILDCARD)), ':', '*'),
                Sequence('*', ':', NCName(), push(new QNameW(QNameW.WILDCARD, match()))),
                Sequence('*', push(new QNameW(QNameW.WILDCARD)))
        );
    }

    /**
     * [38] FilterExpr ::= PrimaryExpr PredicateList
     */
    Rule FilterExpr() {
        return Sequence(PrimaryExpr(), push(new PartialFilterExpr((PrimaryExpr)pop())), PredicateList(), push(complete(pop(), pop())));
    }

    /**
     * [39] PredicateList ::= Predicate*
     */
    Rule PredicateList() {
        return Sequence(ZeroOrMore(Predicate()), push(new PredicateList(popAllR(Predicate.class))));
    }

    /**
     * [40] Predicate ::= "[" Expr "]"
     */
    Rule Predicate() {
        return Sequence(
                '[', WS(),
                Expr(), push(new Predicate(pop())),
                ']', WS()
        );
    }

    /**
     * [41] PrimaryExpr ::= Literal | VarRef | ParenthesizedExpr | ContextItemExpr | FunctionCall
     */
    Rule PrimaryExpr() {
        return FirstOf(
                Literal(),
                VarRef(),
                ParenthesizedExpr(),
                ContextItemExpr(),
                FunctionCall()
        );
    }

    /**
     * [42] Literal ::= NumericLiteral | StringLiteral
     */
    Rule Literal() {
        return FirstOf(NumericLiteral(), StringLiteral());
    }

    /**
     * [43] NumericLiteral ::= IntegerLiteral | DecimalLiteral | DoubleLiteral
     */
    Rule NumericLiteral() {
        return FirstOf(IntegerLiteral(), DecimalLiteral(), DoubleLiteral());
    }

    /**
     * [44] VarRef ::= "$" VarName
     */
    Rule VarRef() {
        return Sequence('$', WS(), VarName(), push(new VarRef((QNameW)pop())));
    }

    /**
     * [45] VarName ::= QName
     */
    Rule VarName() {
        return QName();
    }

    /**
     * [46] ParenthesizedExpr ::= "(" Expr? ")"
     */
    Rule ParenthesizedExpr() {
        return Sequence(
                '(', WS(),
                Optional(Expr()),
                ')', WS()
        );
    }

    /**
     * [47] ContextItemExpr ::= "."
     */
    Rule ContextItemExpr() {
        return Sequence('.', WS());
    }

    /**
     * [48] FunctionCall ::= QName "(" (ExprSingle ("," ExprSingle)*)? ")"      //xgs:reserved-function-names
     */
    Rule FunctionCall() {
        //final Var<List<ExprSingle>> arguments = new Var<List<ExprSingle>>(new ArrayList<ExprSingle>());
        //TODO(AR) should be ExprSingle?
        final Var<List<AbstractASTNode>> arguments = new Var<List<AbstractASTNode>>(new ArrayList<AbstractASTNode>());

        //TODO(AR) we need to actually extract the arguments from the parser and add them to the list

        return Sequence(
               QName(), push(new PartialFunctionCall((QNameW)pop())),
                '(', WS(), Optional(Sequence(ExprSingle(), ACTION(arguments.get().add(pop())), ZeroOrMore(Sequence(',', WS(), ExprSingle(), ACTION(arguments.get().add(pop())))))), ')', WS(),
                push(complete(arguments.get(), pop()))
        );
    }

    /**
     * [49] SingleType ::= AtomicType "?"?      //gn: parens
     */
    Rule SingleType() {
        return Sequence(AtomicType(), Optional(Sequence('?', WS())));
    }

    /**
     * [50] SequenceType ::=    ("empty-sequence" "(" ")")
     *                          | (ItemType OccurrenceIndicator?)
     */
    Rule SequenceType() {
        return FirstOf(Sequence("empty-sequence", WS(), '(', WS(), ')', WS()), Sequence(ItemType(), Optional(OccurrenceIndicator())));
    }

    /**
     * [51] OccurrenceIndicator ::= "?" | "*" | "+"
     */
    Rule OccurrenceIndicator() {
        return Sequence(FirstOf('?', '*', '+'), WS());
    }

    /**
     * [52] ItemType ::= KindTest | ("item" "(" ")") | AtomicType
     */
    Rule ItemType() {
        return FirstOf(KindTest(), Sequence("item", WS(), '(', WS(), ')', WS()), AtomicType());
    }

    /**
     * [53] AtomicType ::= QName
     */
    Rule AtomicType() {
        return QName();
    }

    /**
     * [54] KindTest ::=    DocumentTest
     *                      | ElementTest
     *                      | AttributeTest
     *                      | SchemaElementTest
     *                      | SchemaAttributeTest
     *                      | PITest
     *                      | CommentTest
     *                      | TextTest
     *                      | AnyKindTest
     */
    Rule KindTest() {
        return FirstOf(
                DocumentTest(),
                ElementTest(),
                AttributeTest(),
                SchemaElementTest(),
                SchemaAttributeTest(),
                PITest(),
                CommentTest(),
                TextTest(),
                AnyKindTest()
        );
    }

    /**
     * [55] AnyKindTest ::= "node" "(" ")"
     */
    Rule AnyKindTest() {
        return Sequence("node", push(new AnyKindTest()), WS(), '(', WS(), ')', WS());
    }

    /**
     * [56] DocumentTest ::= "document-node" "(" (ElementTest | SchemaElementTest)? ")"
     */
    Rule DocumentTest() {
        return Sequence("document-node", push(new PartialDocumentTest()), WS(), '(', WS(), Optional(FirstOf(Sequence(ElementTest(), push(complete(Either.Left(pop()), pop()))), Sequence(SchemaElementTest(), push(complete(Either.Right(pop()), pop()))))), ')', WS());
    }

    /**
     * [57] TextTest ::= "text" "(" ")"
     */
    Rule TextTest() {
        return Sequence("text", push(TextTest.instance()), WS(), '(', WS(), ')', WS());
    }

    /**
     * [58] CommentTest ::= "comment" "(" ")"
     */
    Rule CommentTest() {
        return Sequence("comment", push(new CommentTest()), WS(), '(', WS(), ')', WS());
    }

    /**
     * [59] PITest ::= "processing-instruction" "(" (NCName | StringLiteral)? ")"
     */
    Rule PITest() {
        //TODO(AR) do we need to differentiate between the NCName and the StringLiteral, instead of treading both as java.lang.String?

        return Sequence("processing-instruction", push(new PartialPITest()), WS(), '(', WS(), Optional(FirstOf(Sequence(NCName(), push(complete(match(), pop()))), Sequence(StringLiteral(), push(complete(((StringLiteral)pop()).getValue(), pop()))))), ')', WS(), push(completeOptional(pop())));
    }

    /**
     * [60] AttributeTest ::= "attribute" "(" (AttribNameOrWildcard ("," TypeName)?)? ")"
     */
    Rule AttributeTest() {
        return Sequence("attribute", push(new PartialAttributeTest()), WS(), '(', WS(), Optional(Sequence(AttribNameOrWildcard(), push(complete(pop(), pop())), Optional(Sequence(',', WS(), TypeName(), push(complete(pop(), pop())))))), ')', WS(), push(completeOptional(pop())));
    }

    /**
     * [61] AttribNameOrWildcard ::= AttributeName | "*"
     */
    Rule AttribNameOrWildcard() {
        return FirstOf(AttributeName(), Sequence('*', push(new QNameW(QNameW.WILDCARD)), WS()));
    }

    /**
     * [62] SchemaAttributeTest ::= "schema-attribute" "(" AttributeDeclaration ")"
     */
    Rule SchemaAttributeTest() {
        return Sequence("schema-attribute", WS(), '(', WS(), AttributeDeclaration(), push(new SchemaAttributeTest((QNameW)pop())), ')', WS());
    }

    /**
     * [63] AttributeDeclaration ::= AttributeName
     */
    Rule AttributeDeclaration() {
        return AttributeName();
    }

    /**
     * [64] ElementTest ::= "element" "(" (ElementNameOrWildcard ("," TypeName "?"?)?)? ")"
     */
    Rule ElementTest() {
        return Sequence("element", push(new PartialElementTest()), WS(), '(', WS(), Optional(Sequence(ElementNameOrWildcard(), push(complete(pop(), pop())), Optional(Sequence(',', WS(), TypeName(), push(complete(pop(), pop())), Optional(Sequence('?', push(complete(Boolean.TRUE, pop())), WS())))))), ')', WS(), push(completeOptional(pop())));
    }

    /**
     * [65] ElementNameOrWildcard ::= ElementName | "*"
     */
    Rule ElementNameOrWildcard() {
        return FirstOf(ElementName(), Sequence('*', push(new QNameW(QNameW.WILDCARD)), WS()));
    }

    /**
     * [66] SchemaElementTest ::= "schema-element" "(" ElementDeclaration ")"
     */
    Rule SchemaElementTest() {
        return Sequence("schema-element", WS(), '(', WS(), ElementDeclaration(), push(new SchemaElementTest((QNameW)pop())), ')', WS());
    }

    /**
     * [67] ElementDeclaration ::= ElementName
     */
    Rule ElementDeclaration() {
        return ElementName();
        //return ElementName().label("ElementDeclaration"); //TODO(AR) can we introduce rule wrappers with labels?
    }

    /**
     * [68] AttributeName ::= QName
     */
    Rule AttributeName() {
        return QName();
    }

    /**
     * [69] ElementName ::= QName
     */
    Rule ElementName() {
        return QName();
    }

    /**
     * [70] TypeName ::= QName
     */
    Rule TypeName() {
        return QName();
    }

    /**
     * [71] IntegerLiteral ::= Digits
     */
    Rule IntegerLiteral() {
        return Sequence(Digits(), push(new IntegerLiteral(match())), WS());
    }

    /**
     * [72] DecimalLiteral ::= ("." Digits) | (Digits "." [0-9]*)   //ws: explicit
     */
    Rule DecimalLiteral() {
        return Sequence(
                FirstOf(
                        Sequence('.', Digits(), push(new DecimalLiteral("0." + match()))),
                        Sequence(Digits(), push(new PartialDecimalLiteral(match())), '.', ZeroOrMore(CharRange('0', '9')), push(complete(match(), pop())))
                ),
        WS());
    }

    /**
     * [73] DoubleLiteral ::= (("." Digits) | (Digits ("." [0-9]*)?)) [eE] [+-]? Digits     //ws: explicit
     */
    Rule DoubleLiteral() {
        return Sequence(
                FirstOf(
                        Sequence(push(new PartialDoubleLiteral("0")), '.', Digits(), push(complete(match(), pop()))),
                        Sequence(Digits(), push(new PartialDoubleLiteral(match())), Sequence(Optional(Sequence('.', ZeroOrMore(CharRange('0', '9')))), push(complete(match().isEmpty() ? null : match().substring(1), pop()))))
                ),
                IgnoreCase('e'),
                Sequence(Optional(FirstOf('+', '-')), push(complete(match() , pop()))),
                Digits(), push(complete(match(), pop())),
                WS()
        );
    }

    /**
     * [74] StringLiteral ::= ('"' (EscapeQuot | [^"])* '"') | ("'" (EscapeApos | [^'])* "'")	//ws: explicit
     */
    Rule StringLiteral() {
        return FirstOf(
                Sequence("\"", Sequence(ZeroOrMore(FirstOf(EscapeQuot(), NoneOf("\""))), push(new StringLiteral(match().replaceAll("\"\"", "\"")))), "\"", WS()),
                Sequence("'", Sequence(ZeroOrMore(FirstOf(EscapeApos(), NoneOf("'"))), push(new StringLiteral(match().replaceAll("''", "'")))), "'", WS())
        );
    }

    /**
     * [75] EscapeQuot ::= '""'
     */
    Rule EscapeQuot() {
        return Sequence("\"\"", WS());
    }

    /**
     * [76] EscapeApos ::= "''"
     */
    Rule EscapeApos() {
        return Sequence("''", WS());
    }

    /**
     * [77] Comment ::= "(:" (CommentContents | Comment)* ":)"      //ws: explicit
     */
    Rule Comment() {
        return Sequence("(:", WS(), ZeroOrMore(CommentContents(), Comment()), ":)", WS()); //TODO(AR) do we have this right
    }

    /**
     * [78] QName ::= [http://www.w3.org/TR/REC-xml-names/#NT-QName]Names   //xgs: xml-version
     */
    Rule QName() {
        return Sequence(XmlNames_QName(), WS());
    }

    /**
     * [79] NCName ::= [http://www.w3.org/TR/REC-xml-names/#NT-NCName]Names     //xgs: xml-version
     */
    Rule NCName() {
        return XmlNames_NCName();
    }

    /**
     * [81] Digits ::= [0-9]+
     */
    Rule Digits() {
        return OneOrMore(CharRange('0', '9'));
    }

    /**
     * [82] CommentContents ::= (Char+ - (Char* ('(:' | ':)') Char*))
     */
    Rule CommentContents() {
        return TestNot(FirstOf("(:", ":)")); //TODO(AR) do we have this right?
    }


    /**
     * [7] QName ::=    PartialPrefixedName
     *                  | UnprefixedName
     */
    Rule XmlNames_QName() {
        return FirstOf(XmlNames_PrefixedName(), XmlNames_UnprefixedName());
    }

    /**
     * [8] PartialPrefixedName ::= Prefix ':' LocalPart
     */
    Rule XmlNames_PrefixedName() {
        return Sequence(XmlNames_Prefix(), push(new PartialPrefixedName(match())), ':', XmlNames_LocalPart(), push(complete(match(), pop())));
    }

    /**
     * [9] UnprefixedName ::= LocalPart
     */
    Rule XmlNames_UnprefixedName() {
        return Sequence(XmlNames_LocalPart(), push(new QNameW(match())));
    }

    /**
     * [10] Prefix ::= NCName
     */
    Rule XmlNames_Prefix() {
        return XmlNames_NCName();
    }

    /**
     * [11] LocalPart ::= NCName
     */
    Rule XmlNames_LocalPart() {
        return XmlNames_NCName();
    }

    /**
     * [4] NCName ::= [https://www.w3.org/TR/REC-xml/#NT-Name]Name - (Char* ':' Char*)  // An XML Name, minus the ":"
     */
    Rule XmlNames_NCName() {
        return XmlNames_Name_minusColon();
    }

    /**
     * Modified from <a href="https://www.w3.org/TR/xml/">Extensible Markup Language (XML) 1.0 (Fifth Edition)</a>
     *
     * Same as [https://www.w3.org/TR/REC-xml/#NT-Name]Name but with the ':' character removed!
     *
     * [_5] Name ::= NameStartChar_minusColon (NameChar)*
     */
    Rule XmlNames_Name_minusColon() {
        return Sequence(XmlNames_NameStartChar_minusColon(), ZeroOrMore(XmlNames_NameChar()));
    }

    /**
     * Modified from <a href="https://www.w3.org/TR/xml/">Extensible Markup Language (XML) 1.0 (Fifth Edition)</a>
     *
     * Same as [https://www.w3.org/TR/xml/#NT-NameStartChar]NameStartChar but with the ':' character removed!
     *
     * [_4] NameStartChar ::=   [A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF]
     *                          | [#x370-#x37D] | [#x37F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F]
     *                          | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD]
     *                          | [#x10000-#xEFFFF]
     */
    Rule XmlNames_NameStartChar_minusColon() {
        return FirstOf(
                CharRange('A', 'Z'),
                '_',
                CharRange('a', 'z'),
                CharRange('\u00C0', '\u00D6'),
                CharRange('\u00D8', '\u00F6'),
                CharRange('\u00F8', '\u02FF'),
                CharRange('\u0370', '\u037D'),
                CharRange('\u037F', '\u1FFF'),
                CharRange('\u200C', '\u200D'),
                CharRange('\u2070', '\u218F'),
                CharRange('\u2C00', '\u2FEF'),
                CharRange('\u3001', '\uD7FF'),
                CharRange('\uF900', '\uFDCF'),
                CharRange('\uFDF0', '\uFFFD') //,
//                CharRange('\u10000', '\uEFFFF')
        );
    }

    /**
     * Modified from <a href="https://www.w3.org/TR/xml/">Extensible Markup Language (XML) 1.0 (Fifth Edition)</a>
     *
     * Same as [https://www.w3.org/TR/xml/#NT-NameStartChar]NameChar but with the ':' character removed
     *
     * [_4a] NameChar ::= NameStartChar_minusColon | "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]
     */
    Rule XmlNames_NameChar() {
        return FirstOf(
                XmlNames_NameStartChar_minusColon(),
                '-',
                '.',
                CharRange('0', '9'),
                '\u00B7',
                CharRange('\u0300', '\u036F'),
                CharRange('\u203F', '\u2040')
        );
    }

    /**
     * Same as [https://www.w3.org/TR/xml/#NT-S]S
     *
     * [3] S ::= (#x20 | #x9 | #xD | #xA)+
     */
    Rule Xml_S () {
        return OneOrMore(AnyOf(new char[] {0x20, 0x9, 0xD, 0xA}));
    }
}
