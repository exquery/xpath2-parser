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

import org.jetbrains.annotations.Nullable;

/**
 * Created by aretter on 11/02/2016.
 */
public class SequenceType extends AbstractASTNode {
    public final static SequenceType EMPTY_SEQUENCE = new SequenceType(null, null);

    @Nullable final ItemType itemType;
    @Nullable final OccurrenceIndicator occurrenceIndicator;

    public SequenceType(final ItemType itemType, final OccurrenceIndicator occurrenceIndicator) {
        this.itemType = itemType;
        this.occurrenceIndicator = occurrenceIndicator;
    }

    @Override
    protected String describe() {
        if(itemType == null && occurrenceIndicator == null) {
            return "empty-sequence()";
        } else {
            return "SequenceType(" + itemType + (occurrenceIndicator == null ? "" : occurrenceIndicator) + ")";
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof SequenceType) {
            final SequenceType other = (SequenceType)obj;
            if(other.itemType == null && itemType == null) {
                if(other.occurrenceIndicator == null && occurrenceIndicator == null) {
                    return true;
                } else if(other.occurrenceIndicator != null && occurrenceIndicator != null) {
                    return other.occurrenceIndicator.equals(occurrenceIndicator);
                }
            } else if(other.itemType != null && itemType != null) {
                if(other.itemType.equals(itemType)) {
                    if(other.occurrenceIndicator == null && occurrenceIndicator == null) {
                        return true;
                    } else if(other.occurrenceIndicator != null && occurrenceIndicator != null) {
                        return other.occurrenceIndicator.equals(occurrenceIndicator);
                    }
                }
            }
        }

        return false;
    }
}
