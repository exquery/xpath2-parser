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
 * Created by aretter on 28/01/2016.
 */
public enum Axis implements ASTNode {
    CHILD("child"),
    PARENT("parent"),
    PRECEDING_SIBLING("preceding-sibling"),
    PRECEDING("preceding"),
    ANCESTOR_OR_SELF("ancestor-or-self"),
    ANCESTOR("ancestor"),
    SELF("self"),
    DESCENDANT_OR_SELF("descendant-or-self"),
    DESCENDANT("descendant"),
    FOLLOWING_SIBLING("following-sibling"),
    FOLLOWING("following"),
    NAMESPACE("namespace"),
    ATTRIBUTE("attribute");

    final String syntax;
    Axis(final String syntax) {
        this.syntax = syntax;
    }

    public final static Axis fromSyntax(final String syntax) {
        for(final Axis step: values()) {
            if(step.syntax.equals(syntax)) {
                return step;
            }
        }
        throw new IllegalArgumentException("No such axis: '" + syntax + "'");
    }

    public String getSyntax() {
        return syntax;
    }
}
