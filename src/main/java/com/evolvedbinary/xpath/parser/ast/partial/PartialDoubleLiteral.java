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
package com.evolvedbinary.xpath.parser.ast.partial;

import com.evolvedbinary.xpath.parser.ast.DoubleLiteral;
import org.jetbrains.annotations.Nullable;

/**
 * Created by aretter on 10/02/2016.
 */
public class PartialDoubleLiteral extends AbstractPartialASTNode<PartialDoubleLiteral.PartialDoubleLiteral1, String> {
    final String characteristic;

    public PartialDoubleLiteral(final String characteristic) {
        this.characteristic = characteristic;
    }

    @Override
    protected String describe() {
        return "DoubleLiteral(" + characteristic + ".?)e??";
    }

    @Override
    public PartialDoubleLiteral1 complete(@Nullable final String mantissa) {
        return new PartialDoubleLiteral1(mantissa);
    }

    public class PartialDoubleLiteral1 extends AbstractPartialASTNode<PartialDoubleLiteral1.PartialDoubleLiteral2, String> {
        @Nullable final String mantissa;

        public PartialDoubleLiteral1(final String mantissa) {
            this.mantissa = mantissa;
        }

        @Override
        protected String describe() {
            final String m = (mantissa == null ? ".0" : "." + mantissa);
            return "DoubleLiteral(" + characteristic + m + ")e??";
        }

        @Override
        public PartialDoubleLiteral1.PartialDoubleLiteral2 complete(final String exponentSign) {
            if(exponentSign == null || exponentSign.isEmpty()) {
                return new PartialDoubleLiteral2('+');
            } else {
                return new PartialDoubleLiteral2(exponentSign.charAt(0));
            }
        }

        public class PartialDoubleLiteral2 extends AbstractPartialASTNode<DoubleLiteral, String> {
            final char exponentSign;

            public PartialDoubleLiteral2(final char exponentSign) {
                this.exponentSign = exponentSign;
            }

            @Override
            protected String describe() {
                final String m = (mantissa == null ? ".0" : "." + mantissa);
                return "DoubleLiteral(" + characteristic + m + ")e" + exponentSign + "?";
            }

            @Override
            public DoubleLiteral complete(final String exponent) {
                final String m = (mantissa == null ? "" : "." + mantissa);
                return new DoubleLiteral(characteristic + m + "E" + exponentSign + exponent);
            }
        }
    }
}
