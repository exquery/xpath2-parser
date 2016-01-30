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

import com.evolvedbinary.xpath.parser.ast.AttributeTest;
import com.evolvedbinary.xpath.parser.ast.QNameW;
import org.jetbrains.annotations.Nullable;

/**
 * Created by aretter on 30/01/2016.
 */
public class PartialAttributeTest extends AbstractPartialASTNode<PartialAttributeTest.PartialAttributeTest1, QNameW> {

    @Override
    public PartialAttributeTest1 complete(@Nullable final QNameW name) {
        return new PartialAttributeTest1(name);
    }

    @Override
    protected String describe() {
        return "AttributeTest(?, ?)";
    }


    public class PartialAttributeTest1 extends AbstractPartialASTNode<AttributeTest, QNameW> {
        @Nullable final QNameW name;

        public PartialAttributeTest1(final QNameW name) {
            this.name = name;
        }

        @Override
        public AttributeTest complete(@Nullable final QNameW typeName){
            return new AttributeTest(name, typeName);
        }

        @Override
        protected String describe() {
            return "AttributeTest(" + (name == null ? "" : name) + ", ?)";
        }
    }
}
