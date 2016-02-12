package com.evolvedbinary.xpath.parser.ast;

/**
 * Created by aretter on 11/02/2016.
 */
public class EmptySequenceType extends AbstractSequenceType {
    private final static EmptySequenceType instance = new EmptySequenceType();
    private EmptySequenceType() {}

    public final static EmptySequenceType instance() {
        return instance;
    }

    @Override
    protected String describe() {
        return "EmptySequenceType";
    }
}
