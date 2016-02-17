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
public class PathExpr extends AbstractASTNode  {

    private static TreatExpr ROOT = new TreatExpr(
            new ValueExpr(new RelativePathExpr(new FilterExpr(new FunctionCall(new QNameW("fn", "root"), new ValueExpr(new RelativePathExpr(new AxisStep(new Step(Axis.SELF, AnyKindTest.instance()), PredicateList.EMPTY)))), PredicateList.EMPTY))),
            new SequenceType(new DocumentTest(null), null)
    );

    public static StepExpr SLASH_ABBREV = new FilterExpr(
            new ParenthesizedExpr(new Expr(
                    ROOT
            )),
            PredicateList.EMPTY
    );

    public static StepExpr SLASH_SLASH_ABBREV = new FilterExpr(
            new ParenthesizedExpr(new Expr(
                    ROOT,
                    new AxisStep(new Step(Axis.DESCENDANT_OR_SELF, AnyKindTest.instance()), PredicateList.EMPTY)
            )),
            PredicateList.EMPTY
    );

    private final List<? extends StepExpr> steps;
    private final boolean relative;

    public PathExpr(final boolean relative, final List<? extends StepExpr> steps) {
        this.relative = relative;
        this.steps = steps;
    }

    public PathExpr(final boolean relative, final StepExpr... steps) {
        this.relative = relative;
        this.steps = Arrays.asList(steps);
    }

    @Override
    protected String describe() {
        final StringBuilder builder = new StringBuilder();
        for(final StepExpr step : steps) {
            if(builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(step.toString());
        }
        return "PathExpr(" + builder.toString() + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof PathExpr) {
            final PathExpr other = (PathExpr)obj;
            return other.relative == relative
                    && other.steps.equals(steps);
        }

        return false;
    }
}
