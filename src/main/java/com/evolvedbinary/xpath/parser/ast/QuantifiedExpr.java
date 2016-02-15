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

import java.util.List;

/**
 * Created by aretter on 15/02/2016.
 */
public class QuantifiedExpr extends AbstractASTNode implements ExprSingle {

    public enum Quantifier {
        SOME,
        EVERY;

        public final static Quantifier fromSyntax(final String syntax) {
            for(final Quantifier quantifier : Quantifier.values()) {
                if(quantifier.name().toLowerCase().equals(syntax)) {
                    return quantifier;
                }
            }
            throw new IllegalArgumentException("No such Quantifier: '" + syntax + "'");
        }

        public String getSyntax() {
            return name().toLowerCase();
        }
    }

    public static class InClause {
        public final QNameW varName;
        public final ASTNode in; //TODO(AR) ideally should be ExprSingle... but as we simplify the AST, so we have to use ASTNode

        public InClause(final QNameW varName, final ASTNode in) {
            this.varName = varName;
            this.in = in;
        }

        @Override
        public boolean equals(final Object obj) {
            if(obj != null && obj instanceof InClause) {
                final InClause other = (InClause)obj;
                return other.varName.equals(varName)
                        && other.in.equals(in);
            }

            return false;
        }
    }

    private final Quantifier quantifier;
    private final List<InClause> inClauses;
    private final ASTNode satisfies; //TODO(AR) ideally should be ExprSingle... but as we simplify the AST, so we have to use ASTNode

    public QuantifiedExpr(final Quantifier quantifier, final List<InClause> inClauses, final ASTNode satisfies) {
        this.quantifier = quantifier;
        this.inClauses = inClauses;
        this.satisfies = satisfies;
    }

    @Override
    protected String describe() {
        final StringBuilder builder = new StringBuilder();
        for(final InClause inClause : inClauses) {
            if(builder.length() > 0) {
                builder.append(", ");
            }

            builder
                    .append("$")
                    .append(inClause.varName.toString())
                    .append(" in ")
                    .append(inClause.in.toString());
        }
        return "QuantifiedExpr(" + quantifier.getSyntax() + " " + builder.toString() + " satisfies " + satisfies.toString() + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof QuantifiedExpr) {
            final QuantifiedExpr other = (QuantifiedExpr)obj;
            return other.quantifier == quantifier
                    && other.inClauses.equals(inClauses)
                    && other.satisfies.equals(satisfies);
        }

        return false;
    }
}
