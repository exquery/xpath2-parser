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
import org.jetbrains.annotations.Nullable;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;

import com.evolvedbinary.xpath.parser.ast.*;
import org.parboiled.support.Var;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//TODO(AR) think about whether we can remove the use of org.parboiled.support.Var in favour of PartialASTNode, then we would have an immutable AST production
//TODO(AR) may be possible to replace some uses of Var with popAllR

@BuildParseTree
public class XPathParser extends BaseParser<ASTNode> {

    private final boolean enableActions;

    public XPathParser(final Boolean enableActions) {
        this.enableActions = enableActions;
    }

    @Override
    public boolean push(final ASTNode value) {
        if(enableActions) {
            return super.push(value);
        } else {
            return true;
        }
    }

    @Override
    public ASTNode pop() {
        if(enableActions) {
            return super.pop();
        } else {
            return null;
        }
    }

    @Override
    public ASTNode peek() {
        if(enableActions) {
            return super.peek();
        } else {
            return null;
        }
    }

    <T> ASTNode complete(final T value, final ASTNode partial) {
        if(!(partial instanceof PartialASTNode)) {
            throw new IllegalStateException("Cannot complete non-partial AST Node: " + partial.getClass());
        }
        return ((PartialASTNode<?, T>)partial).complete(value);
    }

    ASTNode completeOptional(ASTNode partial) {
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
    <T extends ASTNode> List<T> popAll(final Class<T> type) {
        final List<T> items = new ArrayList<T>();
        while(!getContext().getValueStack().isEmpty() &&
                type.isAssignableFrom(peek().getClass())) {
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
    <T extends ASTNode> List<T> popAllR(final Class<T> type) {
        final List<T> items = popAll(type);
        Collections.reverse(items);
        return items;
    }

    /**
     * Pops a node from the value stack if it is assignable
     * from a specific class
     *
     * @param type The type of node to pop
     * @return The node
     */
    <T extends ASTNode> T popIf(final Class<T> type) {
        if(!getContext().getValueStack().isEmpty() &&
                type.isAssignableFrom(peek().getClass())) {
            return (T)pop();
        }

        return null;
    }

    /**
     * Whitespace handling
     */
    Rule WS() {
        return Optional(FirstOf(Xml_S(), Comment()));
        //return Optional(Xml_S());
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
        final Var<List<ASTNode>> exprSingles = new Var<List<ASTNode>>(new ArrayList<ASTNode>());
        return Sequence(
                ExprSingle(), ACTION(exprSingles.get().add(pop())),
                ZeroOrMore(
                        Sequence(',', WS(), ExprSingle(), ACTION(exprSingles.get().add(pop())))
                ),
                push(new Expr(exprSingles.getAndClear()))
        );
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
        final Var<List<QuantifiedExpr.InClause>> inClauses = new Var<List<QuantifiedExpr.InClause>>(new ArrayList<QuantifiedExpr.InClause>());
        final Var<QNameW> varName = new Var<QNameW>();
        return Sequence(
                FirstOf("some", "every"), push(new PartialQuantifierExpr(QuantifiedExpr.Quantifier.fromSyntax(match()))),
                WS(),
                '$', WS(), VarName(), ACTION(varName.set((QNameW)pop())), "in", WS(), ExprSingle(), ACTION(inClauses.get().add(new QuantifiedExpr.InClause(varName.getAndClear(), pop()))),
                ZeroOrMore(',', WS(), '$', WS(), VarName(),  ACTION(varName.set((QNameW)pop())), "in", WS(), ExprSingle(), ACTION(inClauses.get().add(new QuantifiedExpr.InClause(varName.getAndClear(), pop())))), push(complete(inClauses.getAndClear(), pop())),
                "satisfies", WS(), ExprSingle(), push(complete(pop(), pop()))
        );
    }

    /**
     * [7] IfExpr ::= "if" "(" Expr ")" "then" ExprSingle "else" ExprSingle
     */
    Rule IfExpr() {
        return Sequence(
                "if", WS(), '(', WS(), Expr(), push(new PartialIfExpr((Expr)pop())), ')', WS(),
                "then", WS(), ExprSingle(), push(complete(pop(), pop())),
                "else", WS(), ExprSingle(), push(complete(pop(), pop()))
        );
    }

    /**
     * [8] OrExpr ::= AndExpr ( "or" AndExpr )*
     */
    Rule OrExpr() {
        final Var<List<AbstractOperand>> orOps = new Var<List<AbstractOperand>>(new ArrayList<AbstractOperand>());
        return FirstOf(
                Sequence(
                        AndExpr(), push(new PartialOrExpr((AbstractOperand)pop())),
                        OneOrMore(Sequence(
                                "or",
                                WS(),
                                AndExpr(), ACTION(orOps.get().add((AbstractOperand)pop()))
                        )), push(complete(orOps.getAndClear(), pop()))
                ),
                AndExpr()
        );
    }

    /**
     * [9] AndExpr ::= ComparisonExpr ( "and" ComparisonExpr )*
     */
    Rule AndExpr() {
        final Var<List<AbstractOperand>> andOps = new Var<List<AbstractOperand>>(new ArrayList<AbstractOperand>());
        return FirstOf(
                Sequence(
                        ComparisonExpr(), push(new PartialAndExpr((AbstractOperand)pop())),
                        OneOrMore(Sequence(
                                "and",
                                WS(),
                                ComparisonExpr(), ACTION(andOps.get().add((AbstractOperand)pop()))
                        )), push(complete(andOps.getAndClear(), pop()))
                ),
                ComparisonExpr()
        );
    }

    /**
     * [10] ComparisonExpr ::=  RangeExpr ( (ValueComp
     *                          | GeneralComp
     *                          | NodeComp) RangeExpr )?
     *
     * Value stack head either: ComparisonExpr / RangeExpr / AdditiveExpr / MultiplicativeExpr / UnionExpr / IntersectExceptExpr / InstanceOfExpr / TreatExpr / CastableExpr / CastExpr / UnaryExpr / ValueExpr
     */
    Rule ComparisonExpr() {
        return FirstOf(
                Sequence(
                        RangeExpr(), push(new PartialComparisonExpr((AbstractOperand)pop())),
                        FirstOf(ValueComp(), NodeComp(), GeneralComp()), push(complete(pop(), pop())),
                        RangeExpr(), push(complete(pop(), pop()))
                ),
                RangeExpr()
        );
    }

    /**
     * [11] RangeExpr ::= AdditiveExpr ( "to" AdditiveExpr )?
     *
     * Value stack head either: RangeExpr / AdditiveExpr / MultiplicativeExpr / UnionExpr / IntersectExceptExpr / InstanceOfExpr / TreatExpr / CastableExpr / CastExpr / UnaryExpr / ValueExpr
     */
    Rule RangeExpr() {
        return FirstOf(
                Sequence(
                        AdditiveExpr(), push(new PartialRangeExpr((AbstractOperand)pop())),
                        WS(),
                        "to",
                        WS(),
                        AdditiveExpr(), push(complete(pop(), pop()))
                ),
                AdditiveExpr()
        );
    }

    /**
     * [12] AdditiveExpr ::= MultiplicativeExpr ( ("+" | "-") MultiplicativeExpr )*
     *
     * Value stack head either: AdditiveExpr / MultiplicativeExpr / UnionExpr / IntersectExceptExpr / InstanceOfExpr / TreatExpr / CastableExpr / CastExpr / UnaryExpr / ValueExpr
     */
    Rule AdditiveExpr() {
        final Var<List<AdditiveExpr.AdditiveOp>> additiveOps = new Var<List<AdditiveExpr.AdditiveOp>>(new ArrayList<AdditiveExpr.AdditiveOp>());
        final Var<AdditiveExpr.Additive> additive = new Var<AdditiveExpr.Additive>();
        return FirstOf(
                Sequence(
                        MultiplicativeExpr(), push(new PartialAdditiveExpr((AbstractOperand)pop())),
                        OneOrMore(Sequence(
                                WS(),
                                FirstOf('+', '-'), ACTION(additive.set(AdditiveExpr.Additive.fromSyntax(match().charAt(0)))),
                                WS(),
                                MultiplicativeExpr(), ACTION(additiveOps.get().add(new AdditiveExpr.AdditiveOp(additive.getAndClear(), (AbstractOperand)pop())))
                        )), push(complete(additiveOps.getAndClear(), pop()))
                ),
                MultiplicativeExpr()
        );
    }

    /**
     * [13] MultiplicativeExpr ::= UnionExpr ( ("*" | "div" | "idiv" | "mod") UnionExpr )*
     *
     * Value stack head either: MultiplicativeExpr / UnionExpr / IntersectExceptExpr / InstanceOfExpr / TreatExpr / CastableExpr / CastExpr / UnaryExpr / ValueExpr
     */
    Rule MultiplicativeExpr() {
        final Var<List<MultiplicativeExpr.MultiplicativeOp>> multiplicativeOps = new Var<List<MultiplicativeExpr.MultiplicativeOp>>(new ArrayList<MultiplicativeExpr.MultiplicativeOp>());
        final Var<MultiplicativeExpr.Multiplicative> multiplicative = new Var<MultiplicativeExpr.Multiplicative>();
        return FirstOf(
                Sequence(
                        UnionExpr(), push(new PartialMultiplicativeExpr((AbstractOperand)pop())),
                        OneOrMore(Sequence(
                                WS(),
                                FirstOf('*', "idiv", "div", "mod"), ACTION(multiplicative.set(MultiplicativeExpr.Multiplicative.fromSyntax(match()))),
                                WS(),
                                UnionExpr(), ACTION(multiplicativeOps.get().add(new MultiplicativeExpr.MultiplicativeOp(multiplicative.getAndClear(), (AbstractOperand)pop())))
                        )), push(complete(multiplicativeOps.getAndClear(), pop()))
                ),
                UnionExpr()
        );
    }

    /**
     * [14] UnionExpr ::= IntersectExceptExpr ( ("union" | "|") IntersectExceptExpr )*
     *
     * Value stack head either: UnionExpr / IntersectExceptExpr / InstanceOfExpr / TreatExpr / CastableExpr / CastExpr / UnaryExpr / ValueExpr
     */
    Rule UnionExpr() {
        final Var<List<AbstractOperand>> unionOps = new Var<List<AbstractOperand>>(new ArrayList<AbstractOperand>());
        return FirstOf(
                Sequence(
                    IntersectExceptExpr(), push(new PartialUnionExpr((AbstractOperand)pop())),
                    OneOrMore(Sequence(
                            WS(),
                            FirstOf("union", '|'),
                            WS(),
                            IntersectExceptExpr(), ACTION(unionOps.get().add((AbstractOperand)pop()))
                    )), push(complete(unionOps.getAndClear(), pop()))
                ),
                IntersectExceptExpr()
        );
    }

    /**
     * [15] IntersectExceptExpr ::= InstanceofExpr ( ("intersect" | "except") InstanceofExpr )*
     *
     * Value stack head either: IntersectExceptExpr / InstanceOfExpr / TreatExpr / CastableExpr / CastExpr / UnaryExpr / ValueExpr
     */
    Rule IntersectExceptExpr() {
        final Var<List<IntersectExceptExpr.IntersectExceptOp>> intersectExceptOps = new Var<List<IntersectExceptExpr.IntersectExceptOp>>(new ArrayList<IntersectExceptExpr.IntersectExceptOp>());
        final Var<IntersectExceptExpr.IntersectExcept> intersectExcept = new Var<IntersectExceptExpr.IntersectExcept>();
        return FirstOf(
                Sequence(
                        InstanceofExpr(), push(new PartialIntersectExceptExpr((AbstractOperand)pop())),
                        OneOrMore(Sequence(
                                WS(),
                                FirstOf("intersect", "except"), ACTION(intersectExcept.set(IntersectExceptExpr.IntersectExcept.fromSyntax(match()))),
                                WS(),
                                InstanceofExpr(), ACTION(intersectExceptOps.get().add(new IntersectExceptExpr.IntersectExceptOp(intersectExcept.getAndClear(), (AbstractOperand)pop())))
                        )), push(complete(intersectExceptOps.getAndClear(), pop()))),
                InstanceofExpr()
        );
    }

    /**
     * [16] InstanceofExpr ::= TreatExpr ( "instance" "of" SequenceType )?
     *
     * Value stack head either: InstanceOfExpr / TreatExpr / CastableExpr / CastExpr / UnaryExpr / ValueExpr
     */
    Rule InstanceofExpr() {
        return Sequence(TreatExpr(), Optional(Sequence("instance", WS(), "of", WS(), SequenceType(), push(new InstanceOfExpr((AbstractOperand)pop(1), (SequenceType)pop(0))))));
    }

    /**
     * [17] TreatExpr ::= CastableExpr ( "treat" "as" SequenceType )?
     *
     * Value stack head either: TreatExpr / CastableExpr / CastExpr / UnaryExpr / ValueExpr
     */
    Rule TreatExpr() {
        return Sequence(CastableExpr(), Optional(Sequence("treat", WS(), "as", WS(), SequenceType(), push(new TreatExpr((AbstractOperand)pop(1), (SequenceType)pop(0))))));
    }

    /**
     * [18] CastableExpr ::= CastExpr ( "castable" "as" SingleType )?
     *
     * Value stack head either: CastableExpr / CastExpr / UnaryExpr / ValueExpr
     */
    Rule CastableExpr() {
        return Sequence(CastExpr(), Optional(Sequence("castable", WS(), "as", WS(), SingleType(), push(new CastableExpr((AbstractOperand)pop(1), (SingleType)pop())))));
    }

    /**
     * [19] CastExpr ::= UnaryExpr ( "cast" "as" SingleType )?
     *
     * Value stack head either: CastExpr / UnaryExpr / ValueExpr
     */
    Rule CastExpr() {
        return Sequence(UnaryExpr(), Optional(Sequence("cast", WS(), "as", WS(), SingleType(), push(new CastExpr((AbstractOperand)pop(1), (SingleType)pop())))));
    }

    /**
     * [20] UnaryExpr ::= ("-" | "+")* ValueExpr
     *
     * Value stack head either: UnaryExpr / ValueExpr
     */
    Rule UnaryExpr() {
        return FirstOf(
                Sequence(OneOrMore(FirstOf('-', '+')), push(new PartialUnaryExpr(match())), ValueExpr(), push(complete(pop(), pop()))),
                ValueExpr()
        );
    }

    /**
     * [21] ValueExpr ::= PathExpr
     */
    Rule ValueExpr() {
        return Sequence(PathExpr(), push(new ValueExpr(pop())));
    }

    /**
     * [22] GeneralComp ::= "=" | "!=" | "<" | "<=" | ">" | ">="
     */
    Rule GeneralComp() {
        return Sequence(
                FirstOf("<=", "!=", ">=", '<', '=', '>'), push(GeneralComp.fromSyntax(match())),
                WS()
        );
    }

    /**
     * [23] ValueComp ::= "eq" | "ne" | "lt" | "le" | "gt" | "ge"
     */
    Rule ValueComp() {
        return Sequence(
                FirstOf("eq", "ne", "lt", "le", "gt", "ge"), push(ValueComp.fromSyntax(match())),
                WS()
        );
    }

    /**
     * [24] NodeComp ::= "is" | "<<" | ">>"
     */
    Rule NodeComp() {
        return Sequence(
                FirstOf("is", "<<", ">>"), push(NodeComp.fromSyntax(match())),
                WS()
        );
    }

    /**
     * [25] PathExpr ::=    ("/" RelativePathExpr?)
     *                      | ("//" RelativePathExpr)
     *                      | RelativePathExpr
     */
    Rule PathExpr() {
        //Converts RelativePathExpr to PathExpr
        return FirstOf(
                Sequence(
                        "//",
                        WS(),
                        RelativePathExpr(), ACTION(push(relativePathToPath(PathExpr.SLASH_SLASH_ABBREV, (RelativePathExpr)pop())))
                ),
                Sequence(
                        '/',
                        WS(),
                        Optional(RelativePathExpr()), ACTION(push(relativePathToPath(PathExpr.SLASH_ABBREV, popIf(RelativePathExpr.class))))
                ),
                Sequence(RelativePathExpr(), ACTION(push(relativePathToPath(null, (RelativePathExpr)pop()))))
        );
    }

    /**
     * Converts a Relative Path Expression to a Path Expression
     *
     * @param initialStep An initial step or null, if this Path Expression is still relative and not absolute
     * @param relativePathExpr The RelativePathExpr or null if there is only an initial step, i.e. "/"
     *
     * @return The Path Expression
     * @throws IllegalArgumentException if both initialStep and relativePathExpr are null
     */
    PathExpr relativePathToPath(@Nullable final StepExpr initialStep, @Nullable final RelativePathExpr relativePathExpr) {
        if(initialStep == null && relativePathExpr == null) {
            throw new IllegalArgumentException("Must provide initial step or relative path expression");
        }

        final List<StepExpr> steps = new ArrayList<StepExpr>();
        if(initialStep != null) {
            steps.add(initialStep);
        }
        if(relativePathExpr != null) {
            steps.addAll(relativePathExpr.getSteps());
        }
        return new PathExpr(initialStep == null, steps);
    }

    /**
     * [26] RelativePathExpr ::=    StepExpr (("/" | "//") StepExpr)*
     */
    Rule RelativePathExpr() {
        return Sequence(
                StepExpr(),
                ZeroOrMore(
                        Sequence(
                                FirstOf(
                                        Sequence("//", push(AxisStep.SLASH_SLASH_ABBREV)),
                                        '/'
                                ),
                                WS(),
                                StepExpr()
                        )
                ),
                push(new RelativePathExpr(popAllR(StepExpr.class)))
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
        return Sequence(FirstOf(ReverseStep(), ForwardStep()), push(new PartialAxisStep((Step)pop())), PredicateList(), push(complete(pop(), pop())));
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
                Sequence('@', WS(), NodeTest(), push(new Step(Axis.ATTRIBUTE, (NodeTest)pop()))),
                Sequence(NodeTest(), push(new Step(Axis.CHILD, (NodeTest)pop())))
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
        return Sequence("..", push(new Step(Axis.PARENT, AnyKindTest.instance())), WS());
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
        return FirstOf(DoubleLiteral(), DecimalLiteral(), IntegerLiteral());
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
                Optional(Sequence(Expr(), push(new ParenthesizedExpr(pop())))),
                ')', WS()
        );
    }

    /**
     * [47] ContextItemExpr ::= "."
     */
    Rule ContextItemExpr() {
        return Sequence('.', push(ContextItemExpr.instance()), WS());
    }

    /**
     * [48] FunctionCall ::= QName "(" (ExprSingle ("," ExprSingle)*)? ")"      //xgs:reserved-function-names
     */
    Rule FunctionCall() {
        //TODO(AR) should be ExprSingle?
        final Var<List<ASTNode>> arguments = new Var<List<ASTNode>>(new ArrayList<ASTNode>());
        return Sequence(
               QName(), push(new PartialFunctionCall((QNameW)pop())),
                '(', WS(), Optional(Sequence(ExprSingle(), ACTION(arguments.get().add(pop())), ZeroOrMore(Sequence(',', WS(), ExprSingle(), ACTION(arguments.get().add(pop())))))), ')', WS(),
                push(complete(arguments.getAndClear(), pop()))
        );
    }

    /**
     * [49] SingleType ::= AtomicType "?"?      //gn: parens
     */
    Rule SingleType() {
        return FirstOf(
                Sequence(AtomicType(), WS(), '?', push(new SingleType((AtomicType)pop(), true)), WS()),
                Sequence(AtomicType(), push(new SingleType((AtomicType)pop(), false)))
        );
    }

    /**
     * [50] SequenceType ::=    ("empty-sequence" "(" ")")
     *                          | (ItemType OccurrenceIndicator?)
     */
    Rule SequenceType() {
        return FirstOf(
                Sequence("empty-sequence", push(SequenceType.EMPTY_SEQUENCE), WS(), '(', WS(), ')', WS()),
                Sequence(ItemType(), push(new PartialSequenceType((ItemType)pop())), Optional(Sequence(OccurrenceIndicator(), push(complete(pop(), pop())))), push(completeOptional(pop())))
        );
    }

    /**
     * [51] OccurrenceIndicator ::= "?" | "*" | "+"
     */
    Rule OccurrenceIndicator() {
        return Sequence(FirstOf('?', '*', '+'), push(OccurrenceIndicator.fromSyntax(match().charAt(0))), WS());
    }

    /**
     * [52] ItemType ::= KindTest | ("item" "(" ")") | AtomicType
     */
    Rule ItemType() {
        return FirstOf(
                KindTest(),
                Sequence("item", push(ItemTypeItem.instance()), WS(), '(', WS(), ')', WS()),
                AtomicType()
        );
    }

    /**
     * [53] AtomicType ::= QName
     */
    Rule AtomicType() {
        return Sequence(QName(), push(new AtomicType((QNameW)pop())));
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
        return Sequence("node", push(AnyKindTest.instance()), WS(), '(', WS(), ')', WS());
    }

    /**
     * [56] DocumentTest ::= "document-node" "(" (ElementTest | SchemaElementTest)? ")"
     */
    Rule DocumentTest() {
        return Sequence(
                "document-node", push(new PartialDocumentTest()),
                WS(), '(', WS(),
                Optional(FirstOf(
                        Sequence(ElementTest(), push(complete(Either.Left(pop()), pop()))),
                        Sequence(SchemaElementTest(), push(complete(Either.Right(pop()), pop())))
                )),
                ')', WS(), push(completeOptional(pop())));
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
        return Sequence("comment", push(CommentTest.instance()), WS(), '(', WS(), ')', WS());
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
                        Sequence('.', push(new PartialDoubleLiteral("0")), Digits(), push(complete(match(), pop()))),
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
        return Sequence("(:", WS(), ZeroOrMore(FirstOf(CommentContents(), Comment())), ":)", WS()); //TODO(AR) do we have this right
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
//        return Sequence(
//                OneOrMore(Xml_Char()),
//                OneOrMore(TestNot(Sequence(ZeroOrMore(Xml_Char()), FirstOf("(:", ":)"), ZeroOrMore(Xml_Char()))), ANY)
//        );

        return OneOrMore(TestNot(FirstOf("(:", ":)")), Xml_Char()); //TODO(AR) do we have this right?
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
    Rule Xml_S() {
        return OneOrMore(AnyOf(new char[] {0x20, 0x9, 0xD, 0xA}));
    }

    /**
     * Same as [https://www.w3.org/TR/xml/#NT-Char]Char
     *
     * [2] Char ::= #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
     */
    Rule Xml_Char() {
        return FirstOf(
            (char)0x9,
            (char)0xA,
            (char)0xD,
            CharRange('\u0020', '\uD7FF'),
            CharRange('\uE000', '\uFFFD')/*,
            CharRange('\u10000', '\u10FFFF')*/
        );
    }

    /**
     * Wraps any other rule to consume all input
     * End of input is signalled by a {@link org.parboiled.support.Chars#EOI}
     *
     * @param rule Any XPathParser rule
     *
     * @return The rule followed by an EOI rule
     */
    Rule withEOI(final Rule rule) {
        return Sequence(rule, EOI);
    }
}
