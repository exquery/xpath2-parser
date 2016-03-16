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
public class Axis extends AbstractASTNode {
    public enum Direction {
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
        Direction(final String syntax) {
            this.syntax = syntax;
        }
    }

    public final static Axis CHILD = new Axis(Direction.CHILD);
    public final static Axis PARENT = new Axis(Direction.PARENT);
    public final static Axis PRECEDING_SIBLING = new Axis(Direction.PRECEDING_SIBLING);
    public final static Axis PRECEDING = new Axis(Direction.PRECEDING);
    public final static Axis ANCESTOR_OR_SELF = new Axis(Direction.ANCESTOR_OR_SELF);
    public final static Axis ANCESTOR = new Axis(Direction.ANCESTOR);
    public final static Axis SELF = new Axis(Direction.SELF);
    public final static Axis DESCENDANT_OR_SELF = new Axis(Direction.DESCENDANT_OR_SELF);
    public final static Axis DESCENDANT = new Axis(Direction.DESCENDANT);
    public final static Axis FOLLOWING_SIBLING = new Axis(Direction.FOLLOWING_SIBLING);
    public final static Axis FOLLOWING = new Axis(Direction.FOLLOWING);
    public final static Axis NAMESPACE = new Axis(Direction.NAMESPACE);
    public final static Axis ATTRIBUTE = new Axis(Direction.ATTRIBUTE);

    private final Direction direction;
    private Axis(final Direction direction) {
        this.direction = direction;
    }

    public final static Axis fromSyntax(final String syntax) {
        for(final Direction direction: Direction.values()) {
            if(direction.syntax.equals(syntax)) {
                return new Axis(direction);
            }
        }
        throw new IllegalArgumentException("No such axis: '" + syntax + "'");
    }

    public String getName() {
        return direction.name();
    }

    public String getSyntax() {
        return direction.syntax;
    }

    @Override
    protected String describe() {
        return "Axis(" + getSyntax() + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof Axis) {
            return ((Axis)obj).direction == direction;
        }

        return false;
    }

    public Direction getDirection() {
        return direction;
    }
}
