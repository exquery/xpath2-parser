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
 * Similar to a QName but where the prefix or localPart
 * may be a WILDCARD i.e. "*"
 */
public class QNameW extends AbstractASTNode {
    public final static String WILDCARD = "*";

    @Nullable private final String prefix;
    private final String localPart;

    public QNameW(final String localPart) {
        this(null, localPart);
    }

    public QNameW(final String prefix, final String localPart) {
        this.prefix = prefix;
        this.localPart = localPart;
    }

    @Override
    public final String describe() {
        if(prefix != null) {
            return "QNameW(" + prefix + ":" + localPart + ")";
        } else {
            return "QNameW(" + localPart + ")";
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof QNameW) {
            final QNameW other = (QNameW)obj;

            return other.localPart.equals(this.localPart) &&
                    (other.prefix == null ? "" : other.prefix).equals(this.prefix == null ? "" : this.prefix);
        }

        return false;
    }
}
