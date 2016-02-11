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
 * Created by aretter on 11/02/2016.
 */
public class UnaryExpr extends AbstractOperand {
    private final String signs;
    private final ValueExpr valueExpr;

    public UnaryExpr(final String signs, final ValueExpr valueExpr) {
        this.signs = signs;
        this.valueExpr = valueExpr;
    }

    @Override
    protected String describe() {
        return "UnaryExpr(" + signs + valueExpr + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof UnaryExpr) {
            final UnaryExpr other = (UnaryExpr)obj;
            return other.signs.equals(signs)
                    && other.valueExpr.equals(valueExpr);
        }

        return false;
    }
}
