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

import java.util.List;

/**
 * Created by aretter on 12/02/2016.
 */
public class MultiplicativeExpr extends AbstractOperandWithOps<MultiplicativeExpr.MultiplicativeOp> {

    public enum Multiplicative {
        MULTIPLY("*"),
        DIVIDE("div"),
        INTEGER_DIVIDE("idiv"),
        MODULUS("mod");
        private final String syntax;
        Multiplicative(final String syntax) {
            this.syntax = syntax;
        }

        public static Multiplicative fromSyntax(final String syntax) {
            for(final Multiplicative multiplicative : Multiplicative.values()) {
                if(multiplicative.syntax.equals(syntax)) {
                    return multiplicative;
                }
            }
            throw new IllegalArgumentException("No such Additive: '" + syntax + "'");
        }

        public String getSyntax() {
            return syntax;
        }
    }

    public static class MultiplicativeOp {
        public final Multiplicative multiplicative;
        public final AbstractOperand operand;

        public MultiplicativeOp(final Multiplicative multiplicative, final AbstractOperand operand) {
            this.multiplicative = multiplicative;
            this.operand = operand;
        }

        @Override
        public boolean equals(final Object obj) {
            if(obj != null && obj instanceof MultiplicativeOp) {
                final MultiplicativeOp other = (MultiplicativeOp)obj;
                return other.multiplicative == multiplicative
                        && other.operand.equals(operand);
            }

            return false;
        }
    }

    public MultiplicativeExpr(final AbstractOperand operand, final List<MultiplicativeOp> multiplicativeOps) {
        super(operand, multiplicativeOps);
    }

    @Override
    protected void describeOp(final StringBuilder builder, final MultiplicativeOp multiplicativeOp) {
            builder
                    .append(" ")
                    .append(multiplicativeOp.multiplicative)
                    .append(" ")
                    .append(multiplicativeOp.operand);
    }
}
