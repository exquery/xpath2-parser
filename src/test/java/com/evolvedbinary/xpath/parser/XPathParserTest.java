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

import javax.swing.text.AbstractDocument;

import java.util.Arrays;
import java.util.Collections;

import static com.evolvedbinary.functional.Either.Right;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static org.parboiled.errors.ErrorUtils.printParseErrors;

public class XPathParserTest {

    private final static NameTest WILDCARD = new NameTest(new QNameW(QNameW.WILDCARD));

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
        assertEquals(new CommentTest(), parse("comment()", parser.CommentTest()));
    }

    @Test
    public void parseAnyKindTest() {
        assertEquals(new AnyKindTest(), parse("node()", parser.AnyKindTest()));
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
        assertEquals(new Axis(Axis.Direction.CHILD), parse("child::", parser.ForwardAxis()));
        assertEquals(new Axis(Axis.Direction.DESCENDANT), parse("descendant::", parser.ForwardAxis()));
        assertEquals(new Axis(Axis.Direction.ATTRIBUTE), parse("attribute::", parser.ForwardAxis()));
        assertEquals(new Axis(Axis.Direction.SELF), parse("self::*", parser.ForwardAxis()));
        assertEquals(new Axis(Axis.Direction.DESCENDANT_OR_SELF), parse("descendant-or-self::", parser.ForwardAxis()));
        assertEquals(new Axis(Axis.Direction.FOLLOWING_SIBLING), parse("following-sibling::", parser.ForwardAxis()));
        assertEquals(new Axis(Axis.Direction.FOLLOWING), parse("following::", parser.ForwardAxis()));
        assertEquals(new Axis(Axis.Direction.NAMESPACE), parse("namespace::", parser.ForwardAxis()));
    }

    @Test
    public void parseAbbrevForwardStep() {
        assertEquals(new Step(new Axis(Axis.Direction.CHILD), new ElementTest()), parse("element()", parser.AbbrevForwardStep()));
        assertEquals(new Step(new Axis(Axis.Direction.ATTRIBUTE), WILDCARD), parse("@*", parser.AbbrevForwardStep()));
    }

    @Test
    public void parseForwardStep() {
        assertEquals(new Step(new Axis(Axis.Direction.CHILD), WILDCARD), parse("child::*", parser.ForwardStep()));
        assertEquals(new Step(new Axis(Axis.Direction.CHILD), new ElementTest()), parse("element()", parser.AbbrevForwardStep()));
        assertEquals(new Step(new Axis(Axis.Direction.ATTRIBUTE), WILDCARD), parse("@*", parser.ForwardStep()));
    }

    @Test
    public void parseReverseAxis() {
        assertEquals(new Axis(Axis.Direction.PARENT), parse("parent::", parser.ReverseAxis()));
        assertEquals(new Axis(Axis.Direction.ANCESTOR_OR_SELF), parse("ancestor-or-self::", parser.ReverseAxis()));
        assertEquals(new Axis(Axis.Direction.ANCESTOR), parse("ancestor::", parser.ReverseAxis()));
        assertEquals(new Axis(Axis.Direction.PRECEDING_SIBLING), parse("preceding-sibling::", parser.ReverseAxis()));
        assertEquals(new Axis(Axis.Direction.PRECEDING), parse("preceding::", parser.ReverseAxis()));
    }

    @Test
    public void parseAbbrevReverseStep() {
        assertEquals(new Step(new Axis(Axis.Direction.PARENT), new AnyKindTest()), parse("..", parser.AbbrevReverseStep()));
    }

    @Test
    public void parseReverseStep() {
        assertEquals(new Step(new Axis(Axis.Direction.PARENT), WILDCARD), parse("parent::*", parser.ReverseStep()));
        assertEquals(new Step(new Axis(Axis.Direction.PARENT), new AnyKindTest()), parse("..", parser.ReverseStep()));
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
    public void parseVarRef() {
        assertEquals(new VarRef(new QNameW("a")), parse("$a", parser.VarRef()));
        assertEquals(new VarRef(new QNameW("ns", "a")), parse("$ns:a", parser.VarRef()));
    }

    @Test
    public void parsePredicate() {
        assertEquals(new Predicate(new FilterExpr(new IntegerLiteral("1"), PredicateList.EMPTY)), parse("[1]", parser.Predicate()));
        assertEquals(new Predicate(new FilterExpr(new VarRef(new QNameW("a")), PredicateList.EMPTY)), parse("[$a]", parser.Predicate()));
        assertEquals(new Predicate(new FilterExpr(new FunctionCall(new QNameW("true"), Collections.<AbstractASTNode>emptyList()), PredicateList.EMPTY)), parse("[true()]", parser.Predicate()));
    }

    @Test
    public void parsePredicateList() {
        assertEquals(new PredicateList(Arrays.asList(new Predicate(new FilterExpr(new IntegerLiteral("1"), PredicateList.EMPTY)))), parse("[1]", parser.PredicateList()));
        assertEquals(
                new PredicateList(Arrays.asList(
                        new Predicate(new FilterExpr(new IntegerLiteral("1"), PredicateList.EMPTY)),
                        new Predicate(new FilterExpr(new IntegerLiteral("2"), PredicateList.EMPTY)),
                        new Predicate(new FilterExpr(new IntegerLiteral("3"), PredicateList.EMPTY))
                )),
                parse("[1][2][3]", parser.PredicateList())
        );
    }

    @Test
    public void parseFunctionCall() {
        assertEquals(new FunctionCall(new QNameW("true"), Collections.<AbstractASTNode>emptyList()), parse("true()", parser.FunctionCall()));
        assertEquals(new FunctionCall(new QNameW("false"), Collections.<AbstractASTNode>emptyList()), parse("false()", parser.FunctionCall()));
        assertEquals(new FunctionCall(new QNameW("local", "hello"), Arrays.<AbstractASTNode>asList(new FilterExpr(new StringLiteral("world"), PredicateList.EMPTY))), parse("local:hello(\"world\")", parser.FunctionCall()));
        assertEquals(new FunctionCall(new QNameW("local", "hello"), Arrays.<AbstractASTNode>asList(new FilterExpr(new StringLiteral("world"), PredicateList.EMPTY), new FilterExpr(new StringLiteral("again"), PredicateList.EMPTY))), parse("local:hello(\"world\", \"again\")", parser.FunctionCall()));
        assertEquals(new FunctionCall(new QNameW("other"), Arrays.<AbstractASTNode>asList(new FilterExpr(new VarRef(new QNameW("a")), PredicateList.EMPTY))), parse("other($a)", parser.FunctionCall()));
    }

    @Test
    public void parseContextItemExpr() {
        assertEquals(ContextItemExpr.instance(), parse(".", parser.ContextItemExpr()));
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
