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

import com.evolvedbinary.xpath.parser.ast.ElementTest;
import com.evolvedbinary.xpath.parser.ast.QNameW;
import org.jetbrains.annotations.Nullable;

/**
 * Created by aretter on 30/01/2016.
 */
public class PartialElementTest extends AbstractPartialASTNode<PartialElementTest.PartialElementTest1, QNameW> {

    @Override
    public PartialElementTest1 complete(@Nullable final QNameW name) {
        return new PartialElementTest1(name);
    }

    @Override
    protected String describe() {
        return "ElementTest(?, ?, ?)";
    }


    public class PartialElementTest1 extends AbstractPartialASTNode<PartialElementTest1.PartialElementTest2, QNameW> {
        @Nullable final QNameW name;

        public PartialElementTest1(final QNameW name) {
            this.name = name;
        }

        @Override
        public PartialElementTest2 complete(@Nullable final QNameW typeName){
            return new PartialElementTest2(typeName);
        }

        @Override
        protected String describe() {
            return "ElementTest(" + (name == null ? "" : name) + ", ?, ?)";
        }


        public class PartialElementTest2 extends AbstractPartialASTNode<ElementTest, Boolean> {
            @Nullable final QNameW typeName;

            public PartialElementTest2(final QNameW typeName) {
                this.typeName = typeName;
            }

            @Override
            public ElementTest complete(@Nullable final Boolean optionalType) {
                return new ElementTest(name, typeName, optionalType);
            }

            @Override
            protected String describe() {
                return "ElementTest(" + (name == null ? "" : name) + ", " + (typeName == null ? "" : typeName) + ", ?)";
            }
        }
    }
}
