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
 * Created by aretter on 30/01/2016.
 */
public abstract class KindTest extends AbstractASTNode implements NodeTest {
    private final Kind kind;

    public enum Kind {
        DOCUMENT("document-node"),
        ELEMENT("element"),
        ATTRIBUTE("attribute"),
        SCHEMA_ELEMENT("schema-element"),
        SCHEMA_ATTRIBUTE("schema-attribute"),
        PROCESSING_INSTRUCTION("processing-instruction"),
        COMMENT("comment"),
        TEXT("text"),
        ANY("node");

        final String xpathName;
        Kind(final String xpathName) {
            this.xpathName = xpathName;
        }
    }

    public KindTest(final Kind kind) {
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    protected String describe() {
        return kind.xpathName + "(" + describeParams() + ")";
    }

    protected String describeParams() {
        return "";
    }
}
