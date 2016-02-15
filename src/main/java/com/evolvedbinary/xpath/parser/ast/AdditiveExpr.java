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
public class AdditiveExpr extends AbstractOperandWithOps<AdditiveExpr.AdditiveOp> {

    public enum Additive {
        ADD('+'),
        SUBTRACT('-');
        private final char syntax;
        Additive(final char syntax) {
            this.syntax = syntax;
        }

        public static Additive fromSyntax(final char syntax) {
            for(final Additive additive : Additive.values()) {
                if(additive.syntax == syntax) {
                    return additive;
                }
            }
            throw new IllegalArgumentException("No such Additive: '" + syntax + "'");
        }

        public char getSyntax() {
            return syntax;
        }
    }

    public static class AdditiveOp {
        public final Additive additive;
        public final AbstractOperand operand;

        public AdditiveOp(final Additive additive, final AbstractOperand operand) {
            this.additive = additive;
            this.operand = operand;
        }

        @Override
        public boolean equals(final Object obj) {
            if(obj != null && obj instanceof AdditiveOp) {
                final AdditiveOp other = (AdditiveOp)obj;
                return other.additive == additive
                        && other.operand.equals(operand);
            }

            return false;
        }
    }

    public AdditiveExpr(final AbstractOperand operand, final List<AdditiveOp> additiveOps) {
        super(operand, additiveOps);
    }

    @Override
    protected void describeOp(final StringBuilder builder, final AdditiveOp additiveOp) {
        builder
                .append(" ")
                .append(additiveOp.additive)
                .append(" ")
                .append(additiveOp.operand);
    }
}
