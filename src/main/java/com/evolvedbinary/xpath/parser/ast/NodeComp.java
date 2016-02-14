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
package com.evolvedbinary.xpath.parser.ast;

/**
 * Created by aretter on 13/02/2016.
 */
public class NodeComp extends AbstractASTNode implements Comparison  {

    private enum NodeCompOperator {
        IS("is"),
        PRECEDES("<<"),
        FOLLOWS(">>");

        private final String syntax;
        NodeCompOperator(final String syntax) {
            this.syntax = syntax;
        }
    }

    public final static NodeComp IS = new NodeComp(NodeCompOperator.IS);
    public final static NodeComp PRECEDES = new NodeComp(NodeCompOperator.PRECEDES);
    public final static NodeComp FOLLOWS = new NodeComp(NodeCompOperator.FOLLOWS);

    public static NodeComp fromSyntax(final String syntax) {
        for(final NodeCompOperator nodeCompOp : NodeCompOperator.values()) {
            if(nodeCompOp.syntax.equals(syntax)) {
                return new NodeComp(nodeCompOp);
            }
        }
        throw new IllegalArgumentException("No such node comparison: '" + syntax + "'");
    }

    @Override
    public String getSyntax() {
        return operator.syntax;
    }

    private final NodeCompOperator operator;
    private NodeComp(final NodeCompOperator operator) {
        this.operator = operator;
    }

    @Override
    protected String describe() {
        return "NodeComp(" + operator + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof NodeComp) {
            return ((NodeComp)obj).operator == operator;
        }

        return false;
    }
}
