package com.wynntils.templates.language.parts;

public class TemplateLiteralPart implements TemplatePart {

    private final String value;
    public TemplateLiteralPart(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
