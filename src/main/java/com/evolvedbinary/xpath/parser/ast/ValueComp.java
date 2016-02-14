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
 * Created by aretter on 13/02/2016.
 */
public class ValueComp extends AbstractASTNode implements Comparison  {

    private enum ValueCompOperator {
        EQUAL("eq"),
        NOT_EQUAL("ne"),
        LESS_THAN("lt"),
        LESS_THAN_OR_EQUAL("le"),
        GREATER_THAN("gt"),
        GREATER_THAN_OR_EQUAL("ge");

        private final String syntax;
        ValueCompOperator(final String syntax) {
            this.syntax = syntax;
        }
    }

    public final static ValueComp EQUAL = new ValueComp(ValueCompOperator.EQUAL);
    public final static ValueComp NOT_EQUAL = new ValueComp(ValueCompOperator.NOT_EQUAL);
    public final static ValueComp LESS_THAN = new ValueComp(ValueCompOperator.LESS_THAN);
    public final static ValueComp LESS_THAN_OR_EQUAL = new ValueComp(ValueCompOperator.LESS_THAN_OR_EQUAL);
    public final static ValueComp GREATER_THAN = new ValueComp(ValueCompOperator.GREATER_THAN);
    public final static ValueComp GREATER_THAN_OR_EQUAL = new ValueComp(ValueCompOperator.GREATER_THAN_OR_EQUAL);

    public static ValueComp fromSyntax(final String syntax) {
        for(final ValueCompOperator valueCompOp : ValueCompOperator.values()) {
            if(valueCompOp.syntax.equals(syntax)) {
                return new ValueComp(valueCompOp);
            }
        }
        throw new IllegalArgumentException("No such value comparison: '" + syntax + "'");
    }

    @Override
    public String getSyntax() {
        return operator.syntax;
    }

    private final ValueCompOperator operator;
    private ValueComp(final ValueCompOperator operator) {
        this.operator = operator;
    }

    @Override
    protected String describe() {
        return "ValueComp(" + operator + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof ValueComp) {
            return ((ValueComp)obj).operator == operator;
        }

        return false;
    }
}
