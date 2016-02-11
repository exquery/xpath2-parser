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
 * Created by aretter on 31/01/2016.
 */
public class StringLiteral extends Literal {
    private final String value;

    public StringLiteral(final String value) {
        this.value = value;
    }

    @Override
    protected String describe() {
        return "StringLiteral(" + value + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof StringLiteral) {
            return ((StringLiteral)obj).value.equals(value);
        }

        return false;
    }

    public String getValue() {
        return value;
    }
}
