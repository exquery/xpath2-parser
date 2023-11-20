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

import java.util.Arrays;
import java.util.List;

/**
 * Created by aretter on 15/02/2016.
 */
public class RelativePathExpr extends AbstractASTNode {
    private final List<? extends StepExpr> steps;

    public RelativePathExpr(final List<? extends StepExpr> steps) {
        this.steps = steps;
    }

    public RelativePathExpr(final StepExpr... steps) {
        this.steps = Arrays.asList(steps);
    }

    public final List<? extends StepExpr> getSteps() {
        return steps;
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
        return "RelativePathExpr(" + builder.toString() + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof RelativePathExpr) {
            return ((RelativePathExpr)obj).steps.equals(steps);
        }

        return false;
    }
}
