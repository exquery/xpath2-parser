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
 * Created by aretter on 15/02/2016.
 */
public class IfExpr extends AbstractASTNode implements ExprSingle {
    private final Expr testExpression;
    private final ASTNode thenExpression; //TODO(AR) ideally should be ExprSingle... but as we simplify the AST, so we have to use ASTNode
    private final ASTNode elseExpression; //TODO(AR) ideally should be ExprSingle... but as we simplify the AST, so we have to use ASTNode

    public IfExpr(final Expr testExpression, final ASTNode thenExpression, final ASTNode elseExpression) {
        this.testExpression = testExpression;
        this.thenExpression = thenExpression;
        this.elseExpression = elseExpression;
    }

    @Override
    protected String describe() {
        return "IfExpr(" + testExpression + " then " + thenExpression + " else " + elseExpression + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof IfExpr) {
            final IfExpr other = (IfExpr)obj;
            return other.testExpression.equals(testExpression)
                    && other.thenExpression.equals(thenExpression)
                    && other.elseExpression.equals(elseExpression);
        }

        return false;
    }
}
