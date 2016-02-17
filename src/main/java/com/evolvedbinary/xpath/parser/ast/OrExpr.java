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
 * Created by aretter on 15/02/2016.
 */
public class OrExpr extends AbstractOperandWithOps<AbstractOperand> implements ExprSingle {

    public OrExpr(final AbstractOperand operand, final List<? extends AbstractOperand> orOps) {
        super(operand, orOps);
    }

    public OrExpr(final AbstractOperand operand, final AbstractOperand... orOps) {
        super(operand, Arrays.asList(orOps));
    }

    @Override
    protected void describeOp(final StringBuilder builder, final AbstractOperand orOp) {
        builder
                .append(" or ")
                .append(orOp);
    }
}
