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
public class GeneralComp extends AbstractASTNode implements Comparison  {

    private enum GeneralCompOperator {
        EQUAL("="),
        NOT_EQUAL("!="),
        LESS_THAN("<"),
        LESS_THAN_OR_EQUAL("<="),
        GREATER_THAN(">"),
        GREATER_THAN_OR_EQUAL(">=");

        private final String syntax;
        GeneralCompOperator(final String syntax) {
            this.syntax = syntax;
        }
    }

    public final static GeneralComp EQUAL = new GeneralComp(GeneralCompOperator.EQUAL);
    public final static GeneralComp NOT_EQUAL = new GeneralComp(GeneralCompOperator.NOT_EQUAL);
    public final static GeneralComp LESS_THAN = new GeneralComp(GeneralCompOperator.LESS_THAN);
    public final static GeneralComp LESS_THAN_OR_EQUAL = new GeneralComp(GeneralCompOperator.LESS_THAN_OR_EQUAL);
    public final static GeneralComp GREATER_THAN = new GeneralComp(GeneralCompOperator.GREATER_THAN);
    public final static GeneralComp GREATER_THAN_OR_EQUAL = new GeneralComp(GeneralCompOperator.GREATER_THAN_OR_EQUAL);

    public static GeneralComp fromSyntax(final String syntax) {
        for(final GeneralCompOperator generalCompOp : GeneralCompOperator.values()) {
            if(generalCompOp.syntax.equals(syntax)) {
                return new GeneralComp(generalCompOp);
            }
        }
        throw new IllegalArgumentException("No such general comparison: '" + syntax + "'");
    }

    @Override
    public String getSyntax() {
        return operator.syntax;
    }

    private final GeneralCompOperator operator;
    private GeneralComp(final GeneralCompOperator operator) {
        this.operator = operator;
    }

    @Override
    protected String describe() {
        return "GeneralComp(" + operator + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof GeneralComp) {
            return ((GeneralComp)obj).operator == operator;
        }

        return false;
    }
}
