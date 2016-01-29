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

import org.parboiled.Action;
import org.parboiled.BaseParser;
import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.Label;
import org.parboiled.annotations.SkipNode;
import org.parboiled.annotations.SuppressNode;

import com.evolvedbinary.xpath.parser.ast.*;

@BuildParseTree
public class XPathParser extends BaseParser<Object> {

    private final boolean enableActions;

    public XPathParser(final Boolean enableActions) {
        this.enableActions = enableActions;
    }

    @Override
    public boolean push(Object value) {
        if(enableActions) {
            return super.push(value);
        } else {
            return true;
        }
    }

    @Override
    public Object pop() {
        if(enableActions) {
            return super.pop();
        } else {
            return null;
        }
    }

    @Override
    public Object peek() {
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
    }

    /** RULES BELOW **/

    /**
     * [2] Expr ::= ExprSingle ("," ExprSingle)*
     */
    Rule Expr() {
        return Sequence(ExprSingle(), ZeroOrMore(Sequence(',', ExprSingle())));
    }

    /**
     * [3] ExprSingle ::=   ForExpr
     *                      | QuantifiedExpr
     *                      | IfExpr
     *                      | OrExpr
     */
    Rule ExprSingle() {
        return OrExpr();
    }

    /**
     * [8] OrExpr ::= AndExpr ( "or" AndExpr )*
     */
    Rule OrExpr() {
        return Sequence(AndExpr(), ZeroOrMore(Sequence("or", AndExpr())));
    }

    /**
     * [9] AndExpr ::= ComparisonExpr ( "and" ComparisonExpr )*
     */
    Rule AndExpr() {
        return Sequence(ComparisonExpr(), ZeroOrMore(Sequence("and", ComparisonExpr())));
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
        return Sequence(AdditiveExpr(), Optional(Sequence("to", AdditiveExpr())));
    }

    /**
     * [12] AdditiveExpr ::= MultiplicativeExpr ( ("+" | "-") MultiplicativeExpr )*
     */
    Rule AdditiveExpr() {
        return Sequence(MultiplicativeExpr(), ZeroOrMore(Sequence(FirstOf('+', '-'), MultiplicativeExpr())));
    }

    /**
     * [13] MultiplicativeExpr ::= UnionExpr ( ("*" | "div" | "idiv" | "mod") UnionExpr )*
     */
    Rule MultiplicativeExpr() {
        return Sequence(UnionExpr(), ZeroOrMore(Sequence(FirstOf('*', "idiv", "div", "mod"), UnionExpr())));
    }

    /**
     * [14] UnionExpr ::= IntersectExceptExpr ( ("union" | "|") IntersectExceptExpr )*
     */
    Rule UnionExpr() {
        return Sequence(IntersectExceptExpr(), ZeroOrMore(Sequence(FirstOf("union", '|'), IntersectExceptExpr())));
    }

    /**
     * [15] IntersectExceptExpr ::= InstanceofExpr ( ("intersect" | "except") InstanceofExpr )*
     */
    Rule IntersectExceptExpr() {
        return Sequence(InstanceofExpr(), ZeroOrMore(Sequence(FirstOf("intersect", "except"), InstanceofExpr())));
    }

    /**
     * [16] InstanceofExpr ::= TreatExpr ( "instance" "of" SequenceType )?
     */
    Rule InstanceofExpr() {
        return Sequence(TreatExpr(), Optional(Sequence("instance", "of", SequenceType())));

    }

    /**
     * [17] TreatExpr ::= CastableExpr ( "treat" "as" SequenceType )?
     */
    Rule TreatExpr() {
        return Sequence(CastableExpr(), Optional(Sequence("treat", "as", SequenceType())));
    }

    /**
     * [18] CastableExpr ::= CastExpr ( "castable" "as" SingleType )?
     */
    Rule CastableExpr() {
        return Sequence(CastExpr(), Optional(Sequence("castable", "as", SingleType())));
    }

    /**
     * [19] CastExpr ::= UnaryExpr ( "cast" "as" SingleType )?
     */
    Rule CastExpr() {
        return Sequence(UnaryExpr(), Optional(Sequence("cast", "as", SingleType())));

    }

    /**
     * [20] UnaryExpr ::= ("-" | "+")* ValueExpr
     */
    Rule UnaryExpr() {
        return Sequence(ZeroOrMore('-', '+'), ValueExpr());
    }

    /**
     * [21] ValueExpr ::= PathExpr
     */
    Rule ValueExpr() {
        return PathExpr();
        //TODO
        //return Sequence("1", push("HELLLLLLLLLLLOOOO"));
        //return Sequence(RelativePathExpr(), push("hello world"));
    }

    /**
     * [22] GeneralComp ::= "=" | "!=" | "<" | "<=" | ">" | ">="
     */
    Rule GeneralComp() {
        return FirstOf("<=", "!=", ">=", '<', '=', '>');
    }

    /**
     * [23] ValueComp ::= "eq" | "ne" | "lt" | "le" | "gt" | "ge"
     */
    Rule ValueComp() {
        return FirstOf("eq", "ne", "lt", "le", "gt", "ge");
    }

    /**
     * [24] NodeComp ::= "is" | "<<" | ">>"
     */
    Rule NodeComp() {
        return FirstOf("is", "<<", ">>");
    }

    /**
     * [25] PathExpr ::=    ("/" RelativePathExpr?)
     *                      | ("//" RelativePathExpr)
     *                      | RelativePathExpr
     */
    Rule PathExpr() {
        return FirstOf(
                Sequence("//", RelativePathExpr(), new Action() {
                    @Override
                    public boolean run(final Context context) {
                        final Path path = (Path) pop();
                        if(path != null) {
                            final AxisStep head = path.pop();
                            head.setStep(Step.DESCENDANT_OR_SELF);
                            path.push(head);
                        }
                        return push(path);
                    }
                }),
                Sequence('/', Optional(RelativePathExpr())),
                RelativePathExpr()
        );
    }

    /**
     * [26] RelativePathExpr ::=    StepExpr (("/" | "//") StepExpr)*
     */
    Rule RelativePathExpr() {
        return Sequence(
                StepExpr(), new Action() {
                    @Override
                    public boolean run(final Context context) {
                        if (peek() instanceof AxisStep) {
                            final AxisStep step = (AxisStep) pop();
                            return push(new Path(step));
                        } else {
                            //TODO(AR) don't understand why this happens!
                            return dup();
                        }
                    }
                },
                ZeroOrMore(
                        Sequence(
                                FirstOf("//", '/'), push("!" + match()),
                                StepExpr(), new Action() {
                                    @Override
                                    public boolean run(Context context) {
                                        final AxisStep step = (AxisStep)pop();
                                        final String slashes = (String)pop();
                                        if(slashes != null && slashes.equals("!//")) {
                                            step.setStep(Step.DESCENDANT_OR_SELF);
                                        }
                                        final Path path = (Path)pop();
                                        if(path != null) {
                                            path.add(step);
                                        }
                                        return push(path);
                                    }
                                }
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
                        ForwardAxis(), push(new AxisStep((Step) pop())),
                        NodeTest(), new Action() {
                            @Override
                            public boolean run(final Context context) {
                                final XTest test = (XTest) pop();
                                final AxisStep step = (AxisStep) pop();
                                step.setTest(test);
                                return push(step);
                            }
                        }
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
                "self",
                "descendant-or-self",
                "descendant",
                "following-sibling",
                "following",
                "namespace"), push(Step.fromSyntax(match())),
                "::");
    }

    /**
     * [31] AbbrevForwardStep ::= "@"? NodeTest
     */
    Rule AbbrevForwardStep() {
        return Sequence(
                Optional('@'), push("!@" + matchLength()),
                NodeTest(), new Action() {
                    @Override
                    public boolean run(final Context context) {
                        final XTest test = (XTest) pop();
                        final AxisStep step = new AxisStep(Step.CHILD, test);
                        final String maybeAttributeTest = (String) pop();
                        if (maybeAttributeTest != null && maybeAttributeTest.equals("!@1")) {
                            step.setTest(new AttributeTest(test));
                        }
                        return push(step);
                    }
                });
    }

    /**
     * [32] ReverseStep ::= (ReverseAxis NodeTest) | AbbrevReverseStep
     */
    Rule ReverseStep() {
        return FirstOf(
                Sequence(
                        ReverseAxis(), push(new AxisStep((Step)pop())),
                        NodeTest(), new Action() {
                            @Override
                            public boolean run(final Context context) {
                                final XTest test = (XTest) pop();
                                final AxisStep step = (AxisStep) pop();
                                step.setTest(test);
                                return push(step);
                            }
                        }
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
                "preceding-sibling",
                "preceding",
                "parent",
                "ancestor-or-self",
                "ancestor"), push(Step.fromSyntax(match())),
                "::");
    }

    /**
     * [34] AbbrevReverseStep ::= ".."
     */
    Rule AbbrevReverseStep() {
        return Sequence("..", push(new AxisStep(Step.PARENT)));
    }

    /**
     * [35] NodeTest ::= KindTest | NameTest
     */
    Rule NodeTest() {
        return Sequence(FirstOf(KindTest(), NameTest()), push(new NameTest((QName)pop())));
    }

    /**
     * [36] NameTest ::= QName | Wildcard
     */
    Rule NameTest() {
        return FirstOf(Wildcard(), QName());
    }

    /**
     * [37] Wildcard ::=    "*"
     *                      | (NCName ":" "*")
     *                      | ("*" ":" NCName)
     */
    Rule Wildcard() {
        return FirstOf(
                Sequence(NCName(), push(new QName(match(), QName.WILDCARD)), ':', '*'),
                Sequence('*', ':', NCName(), push(new QName(QName.WILDCARD, match()))),
                Sequence('*', push(new QName(QName.WILDCARD)))
        );
    }

    /**
     * [38] FilterExpr ::= PrimaryExpr PredicateList
     */
    Rule FilterExpr() {
        return Sequence(PrimaryExpr(), PredicateList());
    }

    /**
     * [39] PredicateList ::= Predicate*
     */
    Rule PredicateList() {
        return ZeroOrMore(Predicate());
    }

    /**
     * [40] Predicate ::= "[" Expr "]"
     */
    Rule Predicate() {
        return Sequence(
                '[',
                Expr(), new Action() {
                    @Override
                    public boolean run(Context context) {
                        return true;
                    }
                },
                ']'
        );
    }

    /**
     * [41] PrimaryExpr ::= Literal | VarRef | ParenthesizedExpr | ContextItemExpr | FunctionCall
     */
    Rule PrimaryExpr() {
        return FirstOf(
                Literal(),
                ParenthesizedExpr(),
                ContextItemExpr()
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
     * [46] ParenthesizedExpr ::= "(" Expr? ")"
     */
    Rule ParenthesizedExpr() {
        return Sequence(
                '(',
                Optional(Expr()),
                ')'
        );
    }

    /**
     * [47] ContextItemExpr ::= "."
     */
    Rule ContextItemExpr() {
        return fromCharLiteral('.');
    }

    /**
     * [49] SingleType ::= AtomicType "?"?
     */
    Rule SingleType() {
        return Sequence(AtomicType(), Optional('?'));
    }

    /**
     * [50] SequenceType ::=    ("empty-sequence" "(" ")")
     *                          | (ItemType OccurrenceIndicator?)
     */
    Rule SequenceType() {
        return FirstOf(Sequence("empty-sequence", '(', ')'), Sequence(ItemType(), Optional(OccurrenceIndicator())));
    }

    /**
     * [51] OccurrenceIndicator ::= "?" | "*" | "+"
     */
    Rule OccurrenceIndicator() {
        return FirstOf('?', '*', '+');
    }

    /**
     * [52] ItemType ::= KindTest | ("item" "(" ")") | AtomicType
     */
    Rule ItemType() {
        return FirstOf(KindTest(), Sequence("item", '(', ')'), AtomicType());
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
        return Sequence("node", '(', ')');
    }

    /**
     * [56] DocumentTest ::= "document-node" "(" (ElementTest | SchemaElementTest)? ")"
     */
    Rule DocumentTest() {
        return Sequence("document-node", '(', Optional(FirstOf(ElementTest(), SchemaElementTest())), ')');
    }

    /**
     * [57] TextTest ::= "text" "(" ")"
     */
    Rule TextTest() {
        return Sequence("text", '(', ')');
    }

    /**
     * [58] CommentTest ::= "comment" "(" ")"
     */
    Rule CommentTest() {
        return Sequence("comment", '(', ')');
    }

    /**
     * [59] PITest ::= "processing-instruction" "(" (NCName | StringLiteral)? ")"
     */
    Rule PITest() {
        return Sequence("processing-instruction", '(', Optional(FirstOf(NCName(), StringLiteral())), ')');
    }

    /**
     * [60] AttributeTest ::= "attribute" "(" (AttribNameOrWildcard ("," TypeName)?)? ")"
     */
    Rule AttributeTest() {
        return Sequence("attribute", '(', Optional(Sequence(AttribNameOrWildcard(), Optional(Sequence(',', TypeName())))), ')');
    }

    /**
     * [61] AttribNameOrWildcard ::= AttributeName | "*"
     */
    Rule AttribNameOrWildcard() {
        return FirstOf(AttributeName(), '*');
    }

    /**
     * [62] SchemaAttributeTest ::= "schema-attribute" "(" AttributeDeclaration ")"
     */
    Rule SchemaAttributeTest() {
        return Sequence("schema-attribute", '(', AttributeDeclaration(), ')');
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
        return Sequence("element", '(', Optional(Sequence(ElementNameOrWildcard(), Optional(Sequence(',', TypeName(), Optional('?'))))), ')');
    }

    /**
     * [65] ElementNameOrWildcard ::= ElementName | "*"
     */
    Rule ElementNameOrWildcard() {
        return FirstOf(ElementName(), '*');
    }

    /**
     * [66] SchemaElementTest ::= "schema-element" "(" ElementDeclaration ")"
     */
    Rule SchemaElementTest() {
        return Sequence("schema-element", '(', ElementDeclaration(), ')');
    }

    /**
     * [67] ElementDeclaration ::= ElementName
     */
    Rule ElementDeclaration() {
        return ElementName();
        //return ElementName().label("ElementDeclaration");
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
        return Digits();
    }

    /**
     * [72] DecimalLiteral ::= ("." Digits) | (Digits "." [0-9]*)   ws: explicit
     */
    Rule DecimalLiteral() {
        return FirstOf(Sequence('.', Digits()), Sequence(Digits(), '.', ZeroOrMore(CharRange('0', '9'))));
    }

    /**
     * [73] DoubleLiteral ::= (("." Digits) | (Digits ("." [0-9]*)?)) [eE] [+-]? Digits     ws: explicit
     */
    Rule DoubleLiteral() {
        return Sequence(FirstOf(Sequence('.', Digits()), Sequence(Digits(), Optional(Sequence('.', ZeroOrMore(CharRange('0', '9')))))), IgnoreCase('e'), Optional(FirstOf('+', '-')), Digits());
    }

    /**
     * [74] StringLiteral ::= ('"' (EscapeQuot | [^"])* '"') | ("'" (EscapeApos | [^'])* "'")	ws: explicit
     */
    Rule StringLiteral() {
        return FirstOf(
                Sequence("\"", ZeroOrMore(FirstOf(EscapeQuot(), NoneOf("\""))), "\""),
                Sequence("'", ZeroOrMore(FirstOf(EscapeApos(), NoneOf("'"))), "'")
        );
    }

    /**
     * [75] EscapeQuot ::= '""'
     */
    Rule EscapeQuot() {
        return String("\"\"");
    }

    /**
     * [76] EscapeApos ::= "''"
     */
    Rule EscapeApos() {
        return String("''");
    }

    /**
     * [78] QName ::= [http://www.w3.org/TR/REC-xml-names/#NT-QName]Names   xgs: xml-version
     */
    Rule QName() {
        return XmlNames_QName();
    }

    /**
     * [79] NCName ::= [http://www.w3.org/TR/REC-xml-names/#NT-NCName]Names     xgs: xml-version
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
     * [7] QName ::=    PrefixedName
     *                  | UnprefixedName
     */
    Rule XmlNames_QName() {
        return FirstOf(XmlNames_PrefixedName(), XmlNames_UnprefixedName());
    }

    /**
     * [8] PrefixedName ::= Prefix ':' LocalPart
     */
    Rule XmlNames_PrefixedName() {
        return Sequence(XmlNames_Prefix(), ':', XmlNames_LocalPart(), push(new QName(pop().toString(), pop().toString())));
    }

    /**
     * [9] UnprefixedName ::= LocalPart
     */
    Rule XmlNames_UnprefixedName() {
        return Sequence(XmlNames_LocalPart(), push(new QName(match())));
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
}
