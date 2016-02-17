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

import java.util.Arrays;
import java.util.List;

/**
 * Created by aretter on 14/02/2016.
 */
public class Expr extends AbstractASTNode {
    private final List<? extends ASTNode> exprSingles;

    public Expr(final List<? extends ASTNode> exprSingles) {
        this.exprSingles = exprSingles;
    }

    public Expr(final ASTNode... exprSingles) {
        this.exprSingles = Arrays.asList(exprSingles);
    }

    @Override
    protected String describe() {
        final StringBuilder builder = new StringBuilder();
        for(final ASTNode exprSingle : exprSingles) {
            if(builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(exprSingle.toString());
        }

        return "Expr(" + builder.toString() + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof Expr) {
            return ((Expr)obj).exprSingles.equals(exprSingles);
        }

        return false;
    }
}
