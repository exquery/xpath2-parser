package com.evolvedbinary.xpath.parser.ast;

/**
 * Created by aretter on 11/02/2016.
 */
public class TextTest extends AbstractASTNode {
    private final static TextTest instance = new TextTest();
    private TextTest() {}

    public final static TextTest instance() {
        return instance;
    }

    @Override
    protected String describe() {
        return "TextTest";
    }
}
