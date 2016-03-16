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

import org.jetbrains.annotations.Nullable;

/**
 * Created by aretter on 30/01/2016.
 */
public class ElementTest extends KindTest {
    @Nullable final QNameW name;
    @Nullable QNameW typeName;
    @Nullable boolean optionalType;

    public ElementTest() {
        this(null);
    }

    public ElementTest(final QNameW name) {
        this(name, null);
    }

    public ElementTest(final QNameW name, final QNameW typeName) {
        this(name, typeName, false);
    }

    public ElementTest(final QNameW name, final QNameW typeName, final Boolean optionalType) {
        super(Kind.ELEMENT);
        this.name = name;
        this.typeName = typeName;
        this.optionalType = optionalType == null ? false : optionalType;
    }

    @Override
    protected String describeParams() {
        return (name == null ? "" : name.toString()) +
                (typeName == null ? "" : ", " + typeName.toString()) +
                (optionalType ? "?" : "");
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof ElementTest) {
            final ElementTest other = (ElementTest)obj;

            return
                ((this.name == null && other.name == null) || this.name.equals(other.name)) &&
                ((this.typeName == null && other.typeName == null) || this.typeName.equals(other.typeName)) &&
                this.optionalType == other.optionalType;
        }
        return false;
    }

    @Nullable
    public QNameW getName() {
        return name;
    }
}
