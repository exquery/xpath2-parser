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
 * Created by aretter on 17/02/2016.
 */
public class SimpleForClause extends AbstractASTNode {

    public static class RangeVariable {
        public final QNameW varName;
        public final ASTNode exprSingle;  //TODO(AR) ideally should be ExprSingle... but as we simplify the AST, so we have to use ASTNode

        public RangeVariable(final QNameW varName, final ASTNode exprSingle) {
            this.varName = varName;
            this.exprSingle = exprSingle;
        }

        @Override
        public boolean equals(final Object obj) {
            if(obj != null && obj instanceof RangeVariable) {
                final RangeVariable other = (RangeVariable)obj;
                return other.varName.equals(varName)
                        && other.exprSingle.equals(exprSingle);
            }

            return false;
        }
    }

    private final List<RangeVariable> rangeVariables;

    public SimpleForClause(final List<RangeVariable> rangeVariables) {
        this.rangeVariables = rangeVariables;
    }

    public SimpleForClause(final RangeVariable... rangeVariables) {
        this.rangeVariables = Arrays.asList(rangeVariables);
    }

    @Override
    protected String describe() {
        final StringBuilder builder = new StringBuilder();
        for(final RangeVariable rangeVariable : rangeVariables) {
            if(builder.length() > 0) {
                builder.append(", ");
            }
            builder
                    .append("$")
                    .append(rangeVariable.varName.toString())
                    .append(" in ")
                    .append(rangeVariable.exprSingle.toString());
        }
        return "SimpleForClause(" + builder.toString() + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof SimpleForClause) {
            return ((SimpleForClause)obj).rangeVariables.equals(rangeVariables);
        }

        return false;
    }
}
