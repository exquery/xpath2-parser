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

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Occurs;

/**
 * Created by aretter on 11/02/2016.
 */
public class OccurrenceIndicator extends AbstractASTNode {
    public enum Occurrence {
        ZERO_OR_ONE('?'),
        ZERO_OR_MORE('*'),
        ONE_OR_MORE('+');

        final char syntax;
        Occurrence(final char syntax) {
            this.syntax = syntax;
        }
    }

    private final Occurrence occurrence;

    public OccurrenceIndicator(final Occurrence occurrence) {
        this.occurrence = occurrence;
    }

    public final static OccurrenceIndicator fromSyntax(final char syntax) {
        for(final Occurrence occurrence : Occurrence.values()) {
            if(occurrence.syntax == syntax) {
                return new OccurrenceIndicator(occurrence);
            }
        }
        throw new IllegalArgumentException("No such occurrence: '" + syntax + "'");
    }

    public char getSyntax() {
        return occurrence.syntax;
    }

    @Override
    protected String describe() {
        return "OccurrenceIndicator(" + getSyntax() + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof OccurrenceIndicator) {
            return ((OccurrenceIndicator)obj).occurrence == occurrence;
        }

        return false;
    }
}
