/*
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

import com.evolvedbinary.xpath.parser.ast.ASTNode;
import com.evolvedbinary.xpath.parser.ast.Expr;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.Chars;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;

import java.io.PrintStream;

import static org.parboiled.errors.ErrorUtils.printParseErrors;

/**
 * Simple utility class showing how to use
 * the XPathParser
 *
 * Created by aretter on 16/03/2016.
 */
public class XPathUtil {

    public final static void main(final String args[]) {
        if(args.length != 1) {
            System.err.println("You must provide an XPath as an argument.");
            System.exit(-1);
        } else {
            parseXPath(args[0], System.out, System.err);
        }
    }

    /**
     * Parses an XPath Expression and generates an AST
     *
     * @param xpath The XPath to parse
     * @param out Either a print stream for receiving a debug print of the NodeTree generated by the parser, or null otherwise
     * @param err Either a print stream for receiving errors that occurred during parsing or null otherwise
     *
     * @return An {@link Expr} which is the root of the generated AST
     */
    public static Expr parseXPath(final String xpath, final PrintStream out, final PrintStream err) {
        final XPathParser parser = Parboiled.createParser(XPathParser.class, Boolean.TRUE);
        final ParseRunner<ASTNode> parseRunner = new RecoveringParseRunner<ASTNode>(parser.withEOI(parser.XPath()));
        final ParsingResult<ASTNode> result = parseRunner.run(xpath + Chars.EOI);

        if(out != null) {
            final String parseTreePrintOut = ParseTreeUtils.printNodeTree(result);
            out.print(parseTreePrintOut);
        }

        if(err != null) {
            final String errors = printParseErrors(result);
            err.print(errors);
        }

        return (Expr)result.parseTreeRoot.getValue();
    }
}
