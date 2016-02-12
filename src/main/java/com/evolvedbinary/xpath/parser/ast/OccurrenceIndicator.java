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
