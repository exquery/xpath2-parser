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
import com.evolvedbinary.xpath.parser.ast.UnionExpr;

import java.util.List;

/**
 * Created by aretter on 12/02/2016.
 */
public class PartialUnionExpr extends AbstractPartialASTNode<UnionExpr, List<AbstractOperand>> {
    private final AbstractOperand operand;

    public PartialUnionExpr(final AbstractOperand operand) {
        this.operand = operand;
    }


    @Override
    protected String describe() {
        return "UnionExpr(" + operand + "?...)";
    }

    @Override
    public UnionExpr complete(final List<AbstractOperand> unionOps) {
        return new UnionExpr(operand, unionOps);
    }
}
