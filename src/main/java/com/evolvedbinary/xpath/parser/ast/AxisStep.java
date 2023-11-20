/*
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
 * Created by aretter on 28/01/2016.
 */
public class AxisStep extends AbstractASTNode implements StepExpr {
    private Step step;
    private PredicateList predicateList;

    public static AxisStep SLASH_SLASH_ABBREV = new AxisStep(new Step(Axis.DESCENDANT_OR_SELF, AnyKindTest.instance()), PredicateList.EMPTY);

    public AxisStep(final Step step, final PredicateList predicateList) {
        this.step = step;
        this.predicateList = predicateList;
    }

    @Override
    public final String describe() {
        return "AxisStep(" + step + ", " + predicateList + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof AxisStep) {
            final AxisStep other = (AxisStep)obj;
            return other.step.equals(step)
                    && other.predicateList.equals(predicateList);
        }

        return false;
    }

    public Step getStep() {
        return step;
    }

    public PredicateList getPredicateList() {
        return predicateList;
    }
}
