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
import com.evolvedbinary.xpath.parser.ast.*;
import org.junit.Test;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static org.parboiled.errors.ErrorUtils.printParseErrors;

public class XPathParserTest {

    private final static NameTest WILDCARD = new NameTest(new QNameW(QNameW.WILDCARD));

    private final static AtomicType XS_INT = new AtomicType(new QNameW("xs", "int"));
    private final static AtomicType XS_INTEGER = new AtomicType(new QNameW("xs", "integer"));
    private final static AtomicType XS_STRING = new AtomicType(new QNameW("xs", "string"));

    final boolean DEBUG = true;
    final XPathParser parser = Parboiled.createParser(XPathParser.class, Boolean.TRUE);

    @Test
    public void parseQName() {
        assertEquals(new QNameW("a"), parse("a", parser.QName()));
        assertNotEquals(new QNameW("a"), parse("b", parser.QName()));

        assertEquals(new QNameW("ns", "a"), parse("ns:a", parser.QName()));
        assertNotEquals(new QNameW("a"), parse("ns:a", parser.QName()));
    }

    @Test
    public void parseWildcard() {
        assertEquals(new QNameW(QNameW.WILDCARD), parse("*", parser.Wildcard()));
        assertEquals(new QNameW("ns", QNameW.WILDCARD), parse("ns:*", parser.Wildcard()));
        assertEquals(new QNameW(QNameW.WILDCARD, "a"), parse("*:a", parser.Wildcard()));
    }

    @Test
    public void parseNameTest() {
        assertEquals(new NameTest(new QNameW(QNameW.WILDCARD)), parse("*", parser.NameTest()));
        assertEquals(new NameTest(new QNameW("ns", QNameW.WILDCARD)), parse("ns:*", parser.NameTest()));
        assertEquals(new NameTest(new QNameW(QNameW.WILDCARD, "a")), parse("*:a", parser.NameTest()));
    }

    @Test
    public void parseElementTest() {
        assertEquals(new ElementTest(), parse("element()", parser.ElementTest()));
        assertEquals(new ElementTest(new QNameW("a")), parse("element(a)", parser.ElementTest()));
        assertEquals(new ElementTest(new QNameW("ns", "a")), parse("element(ns:a)", parser.ElementTest()));
        assertEquals(new ElementTest(new QNameW(QNameW.WILDCARD)), parse("element(*)", parser.ElementTest()));

        assertEquals(new ElementTest(new QNameW("a"), new QNameW("xs", "string")), parse("element(a, xs:string)", parser.ElementTest()));
        assertEquals(new ElementTest(new QNameW("a"), new QNameW("xs", "string"), true), parse("element(a, xs:string?)", parser.ElementTest()));
    }

    @Test
    public void parseSchemaElementTest() {
        assertEquals(new SchemaElementTest(new QNameW("a")), parse("schema-element(a)", parser.SchemaElementTest()));
        assertEquals(new SchemaElementTest(new QNameW("ns", "a")), parse("schema-element(ns:a)", parser.SchemaElementTest()));
    }

    @Test
    public void parseDocumentTest() {
        assertEquals(new DocumentTest(Either.<ElementTest, SchemaElementTest>Left(new ElementTest(new QNameW("a")))), parse("document-node(element(a))", parser.DocumentTest()));
        assertEquals(new DocumentTest(Either.<ElementTest, SchemaElementTest>Right(new SchemaElementTest(new QNameW("a")))), parse("document-node(schema-element(a))", parser.DocumentTest()));
    }

    @Test
    public void parseTextTest() {
        assertEquals(TextTest.instance(), parse("text()", parser.TextTest()));
    }

    @Test
    public void parseAttributeTest() {
        assertEquals(new AttributeTest(), parse("attribute()", parser.AttributeTest()));
        assertEquals(new AttributeTest(new QNameW("a")), parse("attribute(a)", parser.AttributeTest()));
        assertEquals(new AttributeTest(new QNameW("ns", "a")), parse("attribute(ns:a)", parser.AttributeTest()));
        assertEquals(new AttributeTest(new QNameW(QNameW.WILDCARD)), parse("attribute(*)", parser.AttributeTest()));

        assertEquals(new AttributeTest(new QNameW("a"), new QNameW("xs", "string")), parse("attribute(a, xs:string)", parser.AttributeTest()));
    }

    @Test
    public void parseSchemaAttributeTest() {
        assertEquals(new SchemaAttributeTest(new QNameW("a")), parse("schema-attribute(a)", parser.SchemaAttributeTest()));
        assertEquals(new SchemaAttributeTest(new QNameW("ns", "a")), parse("schema-attribute(ns:a)", parser.SchemaAttributeTest()));
    }

    @Test
    public void parsePITest() {
        assertEquals(new PITest(), parse("processing-instruction()", parser.PITest()));
        assertEquals(new PITest("a"), parse("processing-instruction(a)", parser.PITest()));
        assertEquals(new PITest("name"), parse("processing-instruction('name')", parser.PITest()));
    }

    @Test
    public void parseCommentTest() {
        assertEquals(CommentTest.instance(), parse("comment()", parser.CommentTest()));
    }

    @Test
    public void parseAnyKindTest() {
        assertEquals(AnyKindTest.instance(), parse("node()", parser.AnyKindTest()));
    }

    @Test
    public void parseStringLiteral() {
        assertEquals(new StringLiteral(""), parse("\"\"", parser.StringLiteral()));
        assertEquals(new StringLiteral(""), parse("''", parser.StringLiteral()));
        assertEquals(new StringLiteral("some string"), parse("\"some string\"", parser.StringLiteral()));
        assertEquals(new StringLiteral("some string"), parse("'some string'", parser.StringLiteral()));
        assertEquals(new StringLiteral("some 'string'"), parse("\"some 'string'\"", parser.StringLiteral()));
        assertEquals(new StringLiteral("some \"string\""), parse("'some \"string\"'", parser.StringLiteral()));
        assertEquals(new StringLiteral("some \"string\""), parse("\"some \"\"string\"\"\"", parser.StringLiteral()));
        assertEquals(new StringLiteral("some 'string'"), parse("'some ''string'''", parser.StringLiteral()));
    }

    @Test
    public void parseFowardAxis() {
        assertEquals(Axis.CHILD, parse("child::", parser.ForwardAxis()));
        assertEquals(Axis.DESCENDANT, parse("descendant::", parser.ForwardAxis()));
        assertEquals(Axis.ATTRIBUTE, parse("attribute::", parser.ForwardAxis()));
        assertEquals(Axis.SELF, parse("self::*", parser.ForwardAxis()));
        assertEquals(Axis.DESCENDANT_OR_SELF, parse("descendant-or-self::", parser.ForwardAxis()));
        assertEquals(Axis.FOLLOWING_SIBLING, parse("following-sibling::", parser.ForwardAxis()));
        assertEquals(Axis.FOLLOWING, parse("following::", parser.ForwardAxis()));
        assertEquals(Axis.NAMESPACE, parse("namespace::", parser.ForwardAxis()));
    }

    @Test
    public void parseAbbrevForwardStep() {
        assertEquals(new Step(Axis.CHILD, new ElementTest()), parse("element()", parser.AbbrevForwardStep()));
        assertEquals(new Step(Axis.ATTRIBUTE, WILDCARD), parse("@*", parser.AbbrevForwardStep()));
    }

    @Test
    public void parseForwardStep() {
        assertEquals(new Step(Axis.CHILD, WILDCARD), parse("child::*", parser.ForwardStep()));
        assertEquals(new Step(Axis.CHILD, new ElementTest()), parse("element()", parser.AbbrevForwardStep()));
        assertEquals(new Step(Axis.ATTRIBUTE, WILDCARD), parse("@*", parser.ForwardStep()));
    }

    @Test
    public void parseReverseAxis() {
        assertEquals(Axis.PARENT, parse("parent::", parser.ReverseAxis()));
        assertEquals(Axis.ANCESTOR_OR_SELF, parse("ancestor-or-self::", parser.ReverseAxis()));
        assertEquals(Axis.ANCESTOR, parse("ancestor::", parser.ReverseAxis()));
        assertEquals(Axis.PRECEDING_SIBLING, parse("preceding-sibling::", parser.ReverseAxis()));
        assertEquals(Axis.PRECEDING, parse("preceding::", parser.ReverseAxis()));
    }

    @Test
    public void parseAbbrevReverseStep() {
        assertEquals(new Step(Axis.PARENT, AnyKindTest.instance()), parse("..", parser.AbbrevReverseStep()));
    }

    @Test
    public void parseReverseStep() {
        assertEquals(new Step(Axis.PARENT, WILDCARD), parse("parent::*", parser.ReverseStep()));
        assertEquals(new Step(Axis.PARENT, AnyKindTest.instance()), parse("..", parser.ReverseStep()));
    }

    @Test
    public void parseIntegerLiteral() {
        assertEquals(new IntegerLiteral("85899345888589934588"), parse("85899345888589934588", parser.IntegerLiteral()));
    }

    @Test
    public void parseDecimalLiteral() {
        assertEquals(new DecimalLiteral("12345678901234567890.123"), parse("12345678901234567890.123", parser.DecimalLiteral()));
        assertEquals(new DecimalLiteral("1.12345678901234567890"), parse("1.12345678901234567890", parser.DecimalLiteral()));
        assertEquals(new DecimalLiteral(".1234"), parse(".1234", parser.DecimalLiteral()));
        assertEquals(new DecimalLiteral("1234."), parse("1234.", parser.DecimalLiteral()));
    }

    @Test
    public void parseDoubleLiteral() {
        assertEquals(new DoubleLiteral("1E10"), parse("1E10", parser.DoubleLiteral()));
        assertEquals(new DoubleLiteral("1E+10"), parse("1E+10", parser.DoubleLiteral()));
        assertEquals(new DoubleLiteral("1E-10"), parse("1E-10", parser.DoubleLiteral()));
        assertEquals(new DoubleLiteral("1e10"), parse("1e10", parser.DoubleLiteral()));
        assertEquals(new DoubleLiteral("1e+10"), parse("1e+10", parser.DoubleLiteral()));
        assertEquals(new DoubleLiteral("1e-10"), parse("1e-10", parser.DoubleLiteral()));
        assertEquals(new DoubleLiteral("1.12E10"), parse("1.12E10", parser.DoubleLiteral()));
        assertEquals(new DoubleLiteral("1.12E+10"), parse("1.12E+10", parser.DoubleLiteral()));
        assertEquals(new DoubleLiteral("1.12E-10"), parse("1.12E-10", parser.DoubleLiteral()));
        assertEquals(new DoubleLiteral("1.12e10"), parse("1.12e10", parser.DoubleLiteral()));
        assertEquals(new DoubleLiteral("1.12e+10"), parse("1.12e+10", parser.DoubleLiteral()));
        assertEquals(new DoubleLiteral("1.12e-10"), parse("1.12e-10", parser.DoubleLiteral()));
    }

    @Test
    public void parseNumericLiteral() {
        assertEquals(new IntegerLiteral("123"), parse("123", parser.NumericLiteral()));
        assertEquals(new DecimalLiteral("1.23"), parse("1.23", parser.NumericLiteral()));
        assertEquals(new DoubleLiteral("1.23E2"), parse("1.23E2", parser.NumericLiteral()));
    }

    @Test
    public void parseVarRef() {
        assertEquals(new VarRef(new QNameW("a")), parse("$a", parser.VarRef()));
        assertEquals(new VarRef(new QNameW("ns", "a")), parse("$ns:a", parser.VarRef()));
    }

    @Test
    public void parsePredicate() {
        assertEquals(new Predicate(new Expr(Arrays.asList(new ValueExpr(new FilterExpr(new IntegerLiteral("1"), PredicateList.EMPTY))))), parse("[1]", parser.Predicate()));
        assertEquals(new Predicate(new Expr(Arrays.asList(new ValueExpr(new FilterExpr(new VarRef(new QNameW("a")), PredicateList.EMPTY))))), parse("[$a]", parser.Predicate()));
        assertEquals(new Predicate(new Expr(Arrays.asList(new ValueExpr(new FilterExpr(new FunctionCall(new QNameW("true"), Collections.<AbstractASTNode>emptyList()), PredicateList.EMPTY))))), parse("[true()]", parser.Predicate()));
    }

    @Test
    public void parsePredicateList() {
        assertEquals(new PredicateList(Arrays.asList(new Predicate(new Expr(Arrays.asList(new ValueExpr(new FilterExpr(new IntegerLiteral("1"), PredicateList.EMPTY))))))), parse("[1]", parser.PredicateList()));
        assertEquals(
                new PredicateList(Arrays.asList(
                        new Predicate(new Expr(Arrays.asList(new ValueExpr(new FilterExpr(new IntegerLiteral("1"), PredicateList.EMPTY))))),
                        new Predicate(new Expr(Arrays.asList(new ValueExpr(new FilterExpr(new IntegerLiteral("2"), PredicateList.EMPTY))))),
                        new Predicate(new Expr(Arrays.asList(new ValueExpr(new FilterExpr(new IntegerLiteral("3"), PredicateList.EMPTY)))))
                )),
                parse("[1][2][3]", parser.PredicateList())
        );
    }

    @Test
    public void parseFunctionCall() {
        assertEquals(new FunctionCall(new QNameW("true"), Collections.<AbstractASTNode>emptyList()), parse("true()", parser.FunctionCall()));
        assertEquals(new FunctionCall(new QNameW("false"), Collections.<AbstractASTNode>emptyList()), parse("false()", parser.FunctionCall()));
        assertEquals(new FunctionCall(new QNameW("local", "hello"), Arrays.asList(new ValueExpr(new FilterExpr(new StringLiteral("world"), PredicateList.EMPTY)))), parse("local:hello(\"world\")", parser.FunctionCall()));
        assertEquals(new FunctionCall(new QNameW("local", "hello"), Arrays.asList(new ValueExpr(new FilterExpr(new StringLiteral("world"), PredicateList.EMPTY)), new ValueExpr(new FilterExpr(new StringLiteral("again"), PredicateList.EMPTY)))), parse("local:hello(\"world\", \"again\")", parser.FunctionCall()));
        assertEquals(new FunctionCall(new QNameW("other"), Arrays.asList(new ValueExpr(new FilterExpr(new VarRef(new QNameW("a")), PredicateList.EMPTY)))), parse("other($a)", parser.FunctionCall()));
    }

    @Test
    public void parseContextItemExpr() {
        assertEquals(ContextItemExpr.instance(), parse(".", parser.ContextItemExpr()));
    }

    @Test
    public void parseFilterExpr() {
        assertEquals(new FilterExpr(ContextItemExpr.instance(), PredicateList.EMPTY), parse(".", parser.FilterExpr()));
        assertEquals(
                new FilterExpr(
                        ContextItemExpr.instance(),
                        new PredicateList(Arrays.asList(
                                new Predicate(new Expr(Arrays.asList(new ValueExpr(new AxisStep(
                                        new Step(Axis.CHILD, new NameTest(new QNameW("a"))),
                                        PredicateList.EMPTY))
                                )))
                        ))
                ),
                parse(".[a]", parser.FilterExpr())
        );

        //TODO(AR) most likely more tests needed here
    }

    @Test
    public void parseAxisStep() {
        assertEquals(
                new AxisStep(
                        new Step(Axis.CHILD, new NameTest(new QNameW("a"))),
                        new PredicateList(Arrays.asList(
                                new Predicate(new Expr(Arrays.asList(new ValueExpr(new FilterExpr(new IntegerLiteral("1"), PredicateList.EMPTY))))),
                                new Predicate(new Expr(Arrays.asList(new ValueExpr(new FilterExpr(new IntegerLiteral("2"), PredicateList.EMPTY))))),
                                new Predicate(new Expr(Arrays.asList(new ValueExpr(new FilterExpr(new IntegerLiteral("3"), PredicateList.EMPTY))))))
                        )
                ),
                parse("a[1][2][3]", parser.AxisStep())
        );

        assertEquals(
                new AxisStep(
                        new Step(Axis.CHILD, new NameTest(new QNameW("a"))),
                        new PredicateList(Arrays.asList(
                                new Predicate(new Expr(Arrays.asList(new ValueExpr(new FilterExpr(new FunctionCall(new QNameW("true"), Collections.<AbstractASTNode>emptyList()), PredicateList.EMPTY))))),
                                new Predicate(new Expr(Arrays.asList(new ValueExpr(new FilterExpr(new FunctionCall(new QNameW("false"), Collections.<AbstractASTNode>emptyList()), PredicateList.EMPTY)))))))
                ),
                parse("a[true()][false()]", parser.AxisStep())
        );
    }

    @Test
    public void parseUnaryExpr() {
        assertEquals(new UnaryExpr("+-", new ValueExpr(new FilterExpr(new IntegerLiteral("1"), PredicateList.EMPTY))), parse("+-1", parser.UnaryExpr()));
        assertEquals(new UnaryExpr("--", new ValueExpr(new FilterExpr(new IntegerLiteral("1"), PredicateList.EMPTY))), parse("--1", parser.UnaryExpr()));
        assertEquals(new ValueExpr(new FilterExpr(new IntegerLiteral("1"), PredicateList.EMPTY)), parse("1", parser.UnaryExpr()));
    }

    @Test
    public void parseAtomicType() {
        assertEquals(XS_STRING, parse("xs:string", parser.AtomicType()));
        assertEquals(XS_INTEGER, parse("xs:integer", parser.AtomicType()));
    }

    @Test
    public void parseSingleType() {
        assertEquals(new SingleType(XS_STRING, false), parse("xs:string", parser.SingleType()));
        assertEquals(new SingleType(XS_STRING, true), parse("xs:string?", parser.SingleType()));
    }

    @Test
    public void parseCastExpr() {
        assertEquals(new CastExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SingleType(XS_INT, false)), parse("-123 cast as xs:int", parser.CastExpr()));
        assertEquals(new CastExpr(new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY)), new SingleType(XS_INT, false)), parse("123 cast as xs:int", parser.CastExpr()));
        assertEquals(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), parse("-123", parser.CastExpr()));
        assertEquals(new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY)), parse("123", parser.CastExpr()));
    }

    @Test
    public void parseCastableExpr() {
        assertEquals(new CastableExpr(new CastExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SingleType(XS_INT, false)), new SingleType(XS_INTEGER, false)), parse("-123 cast as xs:int castable as xs:integer", parser.CastableExpr()));
        assertEquals(new CastableExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SingleType(XS_INT, true)), parse("-123 castable as xs:int?", parser.CastableExpr()));
        assertEquals(new CastableExpr(new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY)), new SingleType(XS_STRING, false)), parse("123 castable as xs:string", parser.CastableExpr()));
        assertEquals(new CastExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SingleType(XS_INT, false)), parse("-123 cast as xs:int", parser.CastableExpr()));
        assertEquals(new CastExpr(new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY)), new SingleType(XS_INT, false)), parse("123 cast as xs:int", parser.CastableExpr()));
        assertEquals(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), parse("-123", parser.CastableExpr()));
        assertEquals(new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY)), parse("123", parser.CastableExpr()));
    }

    @Test
    public void parseOccurrenceIndicator() {
        assertEquals(OccurrenceIndicator.ZERO_OR_ONE, parse("?", parser.OccurrenceIndicator()));
        assertEquals(OccurrenceIndicator.ZERO_OR_MORE, parse("*", parser.OccurrenceIndicator()));
        assertEquals(OccurrenceIndicator.ONE_OR_MORE, parse("+", parser.OccurrenceIndicator()));
    }

    @Test
    public void parseItemType() {
        assertEquals(ItemTypeItem.instance(), parse("item()", parser.ItemType()));
        assertEquals(AnyKindTest.instance(), parse("node()", parser.ItemType()));
        assertEquals(XS_STRING, parse("xs:string", parser.ItemType()));
    }

    @Test
    public void parseSequenceType() {
        assertEquals(SequenceType.EMPTY_SEQUENCE, parse("empty-sequence()", parser.SequenceType()));
        assertEquals(new SequenceType(ItemTypeItem.instance(), null), parse("item()", parser.SequenceType()));
        assertEquals(new SequenceType(ItemTypeItem.instance(), OccurrenceIndicator.ONE_OR_MORE), parse("item()+", parser.SequenceType()));
        assertEquals(new SequenceType(XS_STRING, null), parse("xs:string", parser.SequenceType()));
        assertEquals(new SequenceType(XS_STRING, OccurrenceIndicator.ZERO_OR_MORE), parse("xs:string*", parser.SequenceType()));
    }

    @Test
    public void parseTreatExpr() {
        assertEquals(new TreatExpr(new CastableExpr(new CastExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SingleType(XS_INT, false)), new SingleType(XS_INTEGER, false)), new SequenceType(XS_STRING, OccurrenceIndicator.ZERO_OR_MORE)), parse("-123 cast as xs:int castable as xs:integer treat as xs:string*", parser.TreatExpr()));
        assertEquals(new TreatExpr(new CastExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SingleType(XS_INT, false)), new SequenceType(XS_STRING, null)), parse("-123 cast as xs:int treat as xs:string", parser.TreatExpr()));
        assertEquals(new TreatExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SequenceType(XS_INTEGER, OccurrenceIndicator.ZERO_OR_ONE)), parse("-123 treat as xs:integer?", parser.TreatExpr()));
        assertEquals(new TreatExpr(new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY)), new SequenceType(XS_INTEGER, OccurrenceIndicator.ONE_OR_MORE)), parse("123 treat as xs:integer+", parser.TreatExpr()));
        assertEquals(new CastableExpr(new CastExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SingleType(XS_INT, false)), new SingleType(XS_INTEGER, false)), parse("-123 cast as xs:int castable as xs:integer", parser.TreatExpr()));
        assertEquals(new CastableExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SingleType(XS_INT, true)), parse("-123 castable as xs:int?", parser.TreatExpr()));
        assertEquals(new CastableExpr(new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY)), new SingleType(XS_STRING, false)), parse("123 castable as xs:string", parser.TreatExpr()));
        assertEquals(new CastExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SingleType(XS_INT, false)), parse("-123 cast as xs:int", parser.TreatExpr()));
        assertEquals(new CastExpr(new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY)), new SingleType(XS_INT, false)), parse("123 cast as xs:int", parser.TreatExpr()));
        assertEquals(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), parse("-123", parser.TreatExpr()));
        assertEquals(new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY)), parse("123", parser.TreatExpr()));
    }

    @Test
    public void parseInstanceOfExpr() {
        assertEquals(new InstanceOfExpr(new TreatExpr(new CastableExpr(new CastExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SingleType(XS_INT, false)), new SingleType(XS_INTEGER, false)), new SequenceType(XS_STRING, OccurrenceIndicator.ZERO_OR_MORE)), new SequenceType(XS_STRING, OccurrenceIndicator.ZERO_OR_MORE)), parse("-123 cast as xs:int castable as xs:integer treat as xs:string* instance of xs:string*", parser.InstanceofExpr()));
        assertEquals(new InstanceOfExpr(new CastableExpr(new CastExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SingleType(XS_INT, false)), new SingleType(XS_INTEGER, false)), new SequenceType(XS_STRING, OccurrenceIndicator.ZERO_OR_MORE)), parse("-123 cast as xs:int castable as xs:integer instance of xs:string*", parser.InstanceofExpr()));
        assertEquals(new InstanceOfExpr(new CastExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SingleType(XS_INT, false)), new SequenceType(XS_INTEGER, null)), parse("-123 cast as xs:int instance of xs:integer", parser.InstanceofExpr()));
        assertEquals(new InstanceOfExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SequenceType(XS_INTEGER, OccurrenceIndicator.ZERO_OR_ONE)), parse("-123 instance of xs:integer?", parser.InstanceofExpr()));
        assertEquals(new InstanceOfExpr(new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY)), new SequenceType(XS_INT, OccurrenceIndicator.ONE_OR_MORE)), parse("123 instance of xs:int+", parser.InstanceofExpr()));
        assertEquals(new TreatExpr(new CastableExpr(new CastExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SingleType(XS_INT, false)), new SingleType(XS_INTEGER, false)), new SequenceType(XS_STRING, OccurrenceIndicator.ZERO_OR_MORE)), parse("-123 cast as xs:int castable as xs:integer treat as xs:string*", parser.InstanceofExpr()));
        assertEquals(new TreatExpr(new CastExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SingleType(XS_INT, false)), new SequenceType(XS_STRING, null)), parse("-123 cast as xs:int treat as xs:string", parser.InstanceofExpr()));
        assertEquals(new TreatExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SequenceType(XS_INTEGER, OccurrenceIndicator.ZERO_OR_ONE)), parse("-123 treat as xs:integer?", parser.InstanceofExpr()));
        assertEquals(new TreatExpr(new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY)), new SequenceType(XS_INTEGER, OccurrenceIndicator.ONE_OR_MORE)), parse("123 treat as xs:integer+", parser.InstanceofExpr()));
        assertEquals(new CastableExpr(new CastExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SingleType(XS_INT, false)), new SingleType(XS_INTEGER, false)), parse("-123 cast as xs:int castable as xs:integer", parser.InstanceofExpr()));
        assertEquals(new CastableExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SingleType(XS_INT, true)), parse("-123 castable as xs:int?", parser.InstanceofExpr()));
        assertEquals(new CastableExpr(new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY)), new SingleType(XS_STRING, false)), parse("123 castable as xs:string", parser.InstanceofExpr()));
        assertEquals(new CastExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SingleType(XS_INT, false)), parse("-123 cast as xs:int", parser.InstanceofExpr()));
        assertEquals(new CastExpr(new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY)), new SingleType(XS_INT, false)), parse("123 cast as xs:int", parser.InstanceofExpr()));
        assertEquals(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), parse("-123", parser.InstanceofExpr()));
        assertEquals(new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY)), parse("123", parser.InstanceofExpr()));
    }

    @Test
    public void parseIntersectExceptExpr() {
        assertEquals(
                new IntersectExceptExpr(
                        new ValueExpr(new FilterExpr(new VarRef(new QNameW("a")), PredicateList.EMPTY)),
                        Arrays.asList(new IntersectExceptExpr.IntersectExceptOp(IntersectExceptExpr.IntersectExcept.INTERSECT, new ValueExpr(new FilterExpr(new VarRef(new QNameW("b")), PredicateList.EMPTY))))
                ),
                parse("$a intersect $b", parser.IntersectExceptExpr())
        );

        assertEquals(
                new IntersectExceptExpr(
                        new ValueExpr(new FilterExpr(new VarRef(new QNameW("a")), PredicateList.EMPTY)),
                        Arrays.asList(new IntersectExceptExpr.IntersectExceptOp(IntersectExceptExpr.IntersectExcept.EXCEPT, new ValueExpr(new FilterExpr(new VarRef(new QNameW("b")), PredicateList.EMPTY))))
                ),
                parse("$a except $b", parser.IntersectExceptExpr())
        );

        assertEquals(
                new IntersectExceptExpr(
                        new InstanceOfExpr(new TreatExpr(new CastableExpr(new CastExpr(new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY))), new SingleType(XS_INT, false)), new SingleType(XS_INTEGER, false)), new SequenceType(XS_STRING, OccurrenceIndicator.ZERO_OR_MORE)), new SequenceType(XS_STRING, OccurrenceIndicator.ZERO_OR_MORE)),
                        Arrays.asList(new IntersectExceptExpr.IntersectExceptOp(IntersectExceptExpr.IntersectExcept.INTERSECT, new ValueExpr(new FilterExpr(new VarRef(new QNameW("b")), PredicateList.EMPTY))))
                ),
                parse("-123 cast as xs:int castable as xs:integer treat as xs:string* instance of xs:string* intersect $b", parser.IntersectExceptExpr())
        );
    }

    @Test
    public void parseUnionExpr() {
        assertEquals(
                new UnionExpr(
                        new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("a"))), PredicateList.EMPTY)),
                        Arrays.asList(
                            new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("b"))), PredicateList.EMPTY)),
                            new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("c"))), PredicateList.EMPTY))
                        )
                ),
                parse("a union b union c", parser.UnionExpr())
        );

        assertEquals(
                new UnionExpr(
                    new IntersectExceptExpr(
                        new ValueExpr(new FilterExpr(new VarRef(new QNameW("a")), PredicateList.EMPTY)),
                        Arrays.asList(new IntersectExceptExpr.IntersectExceptOp(IntersectExceptExpr.IntersectExcept.EXCEPT, new ValueExpr(new FilterExpr(new VarRef(new QNameW("b")), PredicateList.EMPTY))))
                    ),
                    Arrays.asList(new ValueExpr(new FilterExpr(new VarRef(new QNameW("c")), PredicateList.EMPTY)))
                ),
                parse("$a except $b union $c", parser.UnionExpr())
        );

        assertEquals(new ValueExpr(new FilterExpr(new IntegerLiteral("123"), PredicateList.EMPTY)), parse("123", parser.UnionExpr()));
    }

    @Test
    public void parseMultiplicativeExpr() {
        assertEquals(
                new MultiplicativeExpr(
                    new ValueExpr(new FilterExpr(new IntegerLiteral("1"), PredicateList.EMPTY)),
                    Arrays.asList(
                            new MultiplicativeExpr.MultiplicativeOp(MultiplicativeExpr.Multiplicative.MULTIPLY, new ValueExpr(new FilterExpr(new IntegerLiteral("2"), PredicateList.EMPTY))),
                            new MultiplicativeExpr.MultiplicativeOp(MultiplicativeExpr.Multiplicative.DIVIDE, new ValueExpr(new FilterExpr(new IntegerLiteral("3"), PredicateList.EMPTY)))
                    )
                ),
                parse("1 * 2 div 3", parser.MultiplicativeExpr())
        );

        assertEquals(
                new MultiplicativeExpr(
                        new CastExpr(new ValueExpr(new FilterExpr(new DecimalLiteral("1.1"), PredicateList.EMPTY)), new SingleType(XS_INTEGER, false)),
                        Arrays.asList(
                                new MultiplicativeExpr.MultiplicativeOp(MultiplicativeExpr.Multiplicative.INTEGER_DIVIDE, new ValueExpr(new FilterExpr(new IntegerLiteral("3"), PredicateList.EMPTY)))
                        )
                ),
                parse("1.1 cast as xs:integer idiv 3", parser.MultiplicativeExpr())
        );
    }

    @Test
    public void parseAdditiveExpr() {
        assertEquals(
                new AdditiveExpr(
                        new ValueExpr(new FilterExpr(new DecimalLiteral("1.2"), PredicateList.EMPTY)),
                        Arrays.asList(
                                new AdditiveExpr.AdditiveOp(AdditiveExpr.Additive.ADD, new ValueExpr(new FilterExpr(new IntegerLiteral("1"), PredicateList.EMPTY))),
                                new AdditiveExpr.AdditiveOp(AdditiveExpr.Additive.SUBTRACT, new ValueExpr(new FilterExpr(new DoubleLiteral("1.9E2"), PredicateList.EMPTY)))
                        )
                ),
                parse("1.2 + 1 - 1.9E2", parser.AdditiveExpr())
        );

        assertEquals(
                new AdditiveExpr(
                        new ValueExpr(new FilterExpr(new DecimalLiteral("1.2"), PredicateList.EMPTY)),
                        Arrays.asList(
                                new AdditiveExpr.AdditiveOp(AdditiveExpr.Additive.ADD, new MultiplicativeExpr(
                                        new ValueExpr(new FilterExpr(new IntegerLiteral("1"), PredicateList.EMPTY)),
                                        Arrays.asList(
                                                new MultiplicativeExpr.MultiplicativeOp(MultiplicativeExpr.Multiplicative.MULTIPLY, new UnaryExpr("-", new ValueExpr(new FilterExpr(new IntegerLiteral("3"), PredicateList.EMPTY))))
                                        )
                                ))
                        )
                ),
                parse("1.2 + 1 * -3", parser.AdditiveExpr())
        );
    }

    @Test
    public void parseRangeExpr() {
        assertEquals(
                new RangeExpr(
                        new ValueExpr(new FilterExpr(new IntegerLiteral("1"), PredicateList.EMPTY)),
                        new ValueExpr(new FilterExpr(new IntegerLiteral("3"), PredicateList.EMPTY))
                ),
                parse("1 to 3", parser.RangeExpr())
        );

        assertEquals(
                new RangeExpr(
                        new ValueExpr(new FilterExpr(new IntegerLiteral("1"), PredicateList.EMPTY)),
                        new AdditiveExpr(
                                new ValueExpr(new FilterExpr(new IntegerLiteral("3"), PredicateList.EMPTY)),
                                Arrays.asList(
                                        new AdditiveExpr.AdditiveOp(AdditiveExpr.Additive.ADD, new CastExpr(new ValueExpr(new FilterExpr(new IntegerLiteral("9"), PredicateList.EMPTY)), new SingleType(XS_INTEGER, false)))
                                )
                        )
                ),
                parse("1 to 3 + 9 cast as xs:integer", parser.RangeExpr())
        );
    }

    @Test
    public void parseValueComp() {
        assertEquals(ValueComp.EQUAL, parse("eq", parser.ValueComp()));
        assertEquals(ValueComp.NOT_EQUAL, parse("ne", parser.ValueComp()));
        assertEquals(ValueComp.LESS_THAN, parse("lt", parser.ValueComp()));
        assertEquals(ValueComp.LESS_THAN_OR_EQUAL, parse("le", parser.ValueComp()));
        assertEquals(ValueComp.GREATER_THAN, parse("gt", parser.ValueComp()));
        assertEquals(ValueComp.GREATER_THAN_OR_EQUAL, parse("ge", parser.ValueComp()));
    }

    @Test
    public void parseNodeComp() {
        assertEquals(NodeComp.IS, parse("is", parser.NodeComp()));
        assertEquals(NodeComp.PRECEDES, parse("<<", parser.NodeComp()));
        assertEquals(NodeComp.FOLLOWS, parse(">>", parser.NodeComp()));
    }

    @Test
    public void parseGeneralComp() {
        assertEquals(GeneralComp.EQUAL, parse("=", parser.GeneralComp()));
        assertEquals(GeneralComp.NOT_EQUAL, parse("!=", parser.GeneralComp()));
        assertEquals(GeneralComp.LESS_THAN, parse("<", parser.GeneralComp()));
        assertEquals(GeneralComp.LESS_THAN_OR_EQUAL, parse("<=", parser.GeneralComp()));
        assertEquals(GeneralComp.GREATER_THAN, parse(">", parser.GeneralComp()));
        assertEquals(GeneralComp.GREATER_THAN_OR_EQUAL, parse(">=", parser.GeneralComp()));
    }

    @Test
    public void parseComparisonExpr() {
        assertEquals(
                new ComparisonExpr(
                        new ValueExpr(new FilterExpr(new IntegerLiteral("1"), PredicateList.EMPTY)),
                        ValueComp.EQUAL,
                        new ValueExpr(new FilterExpr(new IntegerLiteral("2"), PredicateList.EMPTY))
                ),
                parse("1 eq 2", parser.ComparisonExpr())
        );

        assertEquals(
                new ComparisonExpr(
                        new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("b"))), PredicateList.EMPTY)),
                        GeneralComp.EQUAL,
                        new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("a"))), PredicateList.EMPTY))
                ),
                parse("b = a", parser.ComparisonExpr())
        );

        assertEquals(
                new ComparisonExpr(
                        new ValueExpr(new FilterExpr(new ParenthesizedExpr(new Expr(Arrays.asList(
                                new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("a"))), PredicateList.EMPTY)),
                                new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("b"))), PredicateList.EMPTY)),
                                new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("c"))), PredicateList.EMPTY))
                        ))), PredicateList.EMPTY)),
                        GeneralComp.EQUAL,
                        new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("a"))), PredicateList.EMPTY))
                ),
                parse("(a, b, c) = a", parser.ComparisonExpr())
        );

        assertEquals(
                new ComparisonExpr(
                        new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("a"))), PredicateList.EMPTY)),
                        NodeComp.PRECEDES,
                        new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("b"))), PredicateList.EMPTY))
                ),
                parse("a << b", parser.ComparisonExpr())
        );
    }

    @Test
    public void parseParenthesizedExpr() {
        assertEquals(null, parse("()", parser.ParenthesizedExpr()));
        assertEquals(new ParenthesizedExpr(new Expr(Arrays.asList(new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("a"))), PredicateList.EMPTY))))), parse("(a)", parser.ParenthesizedExpr()));
        assertEquals(
                new ParenthesizedExpr(new Expr(Arrays.asList(new RangeExpr(
                        new ValueExpr(new FilterExpr(new IntegerLiteral("1"), PredicateList.EMPTY)),
                        new ValueExpr(new FilterExpr(new IntegerLiteral("10"), PredicateList.EMPTY))
                )))),
                parse("(1 to 10)", parser.ParenthesizedExpr())
        );
        assertEquals(
                new ParenthesizedExpr(
                        new Expr(Arrays.asList(
                                new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("a"))), PredicateList.EMPTY)),
                                new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("b"))), PredicateList.EMPTY))
                        ))
                ),
                parse("(a, b)", parser.ParenthesizedExpr())
        );

        assertEquals(
                new ParenthesizedExpr(
                        new Expr(Arrays.asList(
                                new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("a"))), PredicateList.EMPTY)),
                                new ValueExpr(new FilterExpr(new ParenthesizedExpr(new Expr(Arrays.asList(
                                        new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("b"))), PredicateList.EMPTY)),
                                        new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("c"))), PredicateList.EMPTY))
                                ))), PredicateList.EMPTY))
                        ))
                ),
                parse("(a, (b, c))", parser.ParenthesizedExpr())
        );
    }

    @Test
    public void parseAndExpr() {
        assertEquals(
                new AndExpr(
                        new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("a"))), PredicateList.EMPTY)),
                        Arrays.asList(
                                new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("b"))), PredicateList.EMPTY))
                        )
                ),
                parse("a and b", parser.AndExpr())
        );

        assertEquals(
                new AndExpr(
                        new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("a"))), PredicateList.EMPTY)),
                        Arrays.asList(
                                new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("b"))), PredicateList.EMPTY)),
                                new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("c"))), PredicateList.EMPTY))
                        )
                ),
                parse("a and b and c", parser.AndExpr())
        );
    }

    @Test
    public void parseOrExpr() {
        assertEquals(
                new OrExpr(
                        new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("a"))), PredicateList.EMPTY)),
                        Arrays.asList(
                                new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("b"))), PredicateList.EMPTY))
                        )
                ),
                parse("a or b", parser.OrExpr())
        );

        assertEquals(
                new OrExpr(
                        new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("a"))), PredicateList.EMPTY)),
                        Arrays.asList(
                                new AndExpr(
                                        new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("b"))), PredicateList.EMPTY)),
                                        Arrays.asList(
                                                new ValueExpr(new AxisStep(new Step(Axis.CHILD, new NameTest(new QNameW("c"))), PredicateList.EMPTY))
                                        )
                                )
                        )
                ),
                parse("a or b and c", parser.OrExpr())
        );
    }

    private ASTNode parse(final String xpath) {
        return parse(xpath, parser.XPath());
    }

    private ASTNode parse(final String xpath, final Rule rule) {
        final ParseRunner<ASTNode> parseRunner = new RecoveringParseRunner(rule);
        final ParsingResult<ASTNode> result = parseRunner.run(xpath);

        if(DEBUG) {
            final String parseTreePrintOut = ParseTreeUtils.printNodeTree(result);
            System.out.println(parseTreePrintOut);
        }

        if(result.hasErrors()) {
            final String errors = printParseErrors(result);
            fail(errors);
        }

        return result.parseTreeRoot.getValue();
    }
}
