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

import java.util.Arrays;
import java.util.List;

/**
 * Created by aretter on 12/02/2016.
 */
public class IntersectExceptExpr extends AbstractOperandWithOps<IntersectExceptExpr.IntersectExceptOp> {

    public enum IntersectExcept {
        INTERSECT("intersect"),
        EXCEPT("except");

        private final String syntax;
        IntersectExcept(final String syntax) {
            this.syntax = syntax;
        }

        public static IntersectExcept fromSyntax(final String syntax) {
            for(final IntersectExcept intersectExcept: IntersectExcept.values()) {
                if(intersectExcept.syntax.equals(syntax)) {
                    return intersectExcept;
                }
            }
            throw new IllegalArgumentException("No such IntersectExcept: '" + syntax + "'");
        }

        public String getSyntax() {
            return syntax;
        }
    }

    public static class IntersectExceptOp {
        public final IntersectExcept intersectExcept;
        public final AbstractOperand operand;

        public IntersectExceptOp(final IntersectExcept intersectExcept, final AbstractOperand operand) {
            this.intersectExcept = intersectExcept;
            this.operand = operand;
        }

        @Override
        public boolean equals(final Object obj) {
            if(obj != null && obj instanceof IntersectExceptOp) {
                final IntersectExceptOp other = (IntersectExceptOp)obj;
                return other.intersectExcept == intersectExcept
                        && other.operand.equals(operand);
            }

            return false;
        }
    }

    public IntersectExceptExpr(final AbstractOperand operand, final List<IntersectExceptOp> interceptExceptOps) {
        super(operand, interceptExceptOps);
    }

    public IntersectExceptExpr(final AbstractOperand operand, final IntersectExceptOp... interceptExceptOps) {
        super(operand, Arrays.asList(interceptExceptOps));
    }

    @Override
    protected void describeOp(final StringBuilder builder, final IntersectExceptOp intersectExceptOp) {
            builder
                    .append(" ")
                    .append(intersectExceptOp.intersectExcept)
                    .append(" ")
                    .append(intersectExceptOp.operand);
    }
}
