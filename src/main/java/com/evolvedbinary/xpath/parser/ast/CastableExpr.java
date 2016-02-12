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
public class CastableExpr extends AbstractOperand {
    private final AbstractOperand operand;
    private final SingleType type;

    public CastableExpr(final AbstractOperand operand, final SingleType type) {
        this.operand = operand;
        this.type = type;
    }

    @Override
    protected String describe() {
        return "CastableExpr(" + operand + " castable as " + type + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof CastableExpr) {
            final CastableExpr other = (CastableExpr)obj;
            return other.operand.equals(operand)
                    && other.type.equals(type);
        }

        return false;
    }
}
