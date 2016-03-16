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
 * Created by aretter on 10/02/2016.
 */
public class Step extends AbstractASTNode {
    private final Axis axis;
    private final NodeTest nodeTest;

    public Step(final Axis axis, final NodeTest nodeTest) {
        this.axis = axis;
        this.nodeTest = nodeTest;
    }

    @Override
    protected String describe() {
        return "Step(" + axis.getSyntax() + "::" + nodeTest + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof Step) {
            final Step other = (Step)obj;
            return other.axis.equals(this.axis) &&
                    other.nodeTest.equals(this.nodeTest);
        }

        return false;
    }

    public Axis getAxis() {
        return axis;
    }

    public NodeTest getNodeTest() {
        return nodeTest;
    }
}
