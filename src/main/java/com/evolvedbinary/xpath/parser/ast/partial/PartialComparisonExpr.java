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

import com.evolvedbinary.xpath.parser.ast.AbstractOperand;
import com.evolvedbinary.xpath.parser.ast.Comparison;
import com.evolvedbinary.xpath.parser.ast.ComparisonExpr;

/**
 * Created by aretter on 14/02/2016.
 */
public class PartialComparisonExpr extends AbstractPartialASTNode<PartialComparisonExpr.PartialComparisonExpr1, Comparison> {
    private final AbstractOperand left;

    public PartialComparisonExpr(final AbstractOperand left) {
        this.left = left;
    }

    @Override
    protected String describe() {
        return "ComparisonExpr(" + left + ", ?, ?)";
    }

    @Override
    public PartialComparisonExpr.PartialComparisonExpr1 complete(final Comparison comparison) {
        return new PartialComparisonExpr1(comparison);
    }

    public class PartialComparisonExpr1 extends AbstractPartialASTNode<ComparisonExpr, AbstractOperand> {
        private final Comparison comparison;

        public PartialComparisonExpr1(final Comparison comparison) {
            this.comparison = comparison;
        }

        @Override
        protected String describe() {
            return "ComparisonExpr(" + left + " " + comparison.getSyntax() + " ?)";
        }

        @Override
        public ComparisonExpr complete(final AbstractOperand right) {
            return new ComparisonExpr(left, comparison, right);
        }
    }
}
