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

import com.evolvedbinary.xpath.parser.ast.ASTNode;
import com.evolvedbinary.xpath.parser.ast.QuantifiedExpr;

import java.util.List;

/**
 * Created by aretter on 15/02/2016.
 */
public class PartialQuantifierExpr extends AbstractPartialASTNode<PartialQuantifierExpr.PartialQuantifierExpr1, List<QuantifiedExpr.InClause>> {
    private final QuantifiedExpr.Quantifier quantifier;

    public PartialQuantifierExpr(final QuantifiedExpr.Quantifier quantifier) {
        this.quantifier = quantifier;
    }

    @Override
    public PartialQuantifierExpr.PartialQuantifierExpr1 complete(final List<QuantifiedExpr.InClause> inClauses) {
        return new PartialQuantifierExpr1(inClauses);
    }

    @Override
    protected String describe() {
        return "QuantifiedExpr(" + quantifier.getSyntax() + " ?..., ?)";
    }

    public class PartialQuantifierExpr1 extends AbstractPartialASTNode<QuantifiedExpr, ASTNode> {
        private final List<QuantifiedExpr.InClause> inClauses;

        public PartialQuantifierExpr1(final List<QuantifiedExpr.InClause> inClauses) {
            this.inClauses = inClauses;
        }

        @Override
        public QuantifiedExpr complete(final ASTNode satisfies) {
            return new QuantifiedExpr(quantifier, inClauses, satisfies);
        }

        @Override
        protected String describe() {
            final StringBuilder builder = new StringBuilder();
            for(final QuantifiedExpr.InClause inClause : inClauses) {
                if(builder.length() > 0) {
                    builder.append(", ");
                }

                builder
                        .append("$")
                        .append(inClause.varName.toString())
                        .append(" in ")
                        .append(inClause.in.toString());
            }
            return "QuantifiedExpr(" + quantifier.getSyntax() + " " + builder.toString() + ", ?)";
        }
    }
}
