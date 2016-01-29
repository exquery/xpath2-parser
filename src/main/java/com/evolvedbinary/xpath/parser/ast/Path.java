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

import java.util.ArrayDeque;

/**
 * Created by aretter on 28/01/2016.
 */
public class Path extends ASTNode {
    final ArrayDeque<AxisStep> steps = new ArrayDeque<AxisStep>();

    public Path(final AxisStep step) {
        steps.add(step);
    }

    public void add(final AxisStep step) {
        steps.add(step);
    }

    public AxisStep pop() {
        return steps.remove();
    }

    public void push(final AxisStep step) {
        steps.push(step);
    }

    @Override
    public final String describe() {
        return "Path(" + steps + ")";
    }
}
