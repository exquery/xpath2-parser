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

import com.evolvedbinary.functional.Either;
import org.jetbrains.annotations.Nullable;

/**
 * Created by aretter on 30/01/2016.
 */
public class DocumentTest extends KindTest {
    @Nullable final Either<ElementTest, SchemaElementTest> elementTest;

    public DocumentTest(final Either<ElementTest, SchemaElementTest> elementTest) {
        super(Kind.DOCUMENT);
        this.elementTest = elementTest;
    }

    @Override
    protected String describeParams() {
        return elementTest == null ? "" : elementTest.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof DocumentTest) {
            final DocumentTest other = (DocumentTest)obj;
            return (other.elementTest == null && this.elementTest == null) ||
                    other.elementTest.equals(this.elementTest);
        }

        return false;
    }
}
