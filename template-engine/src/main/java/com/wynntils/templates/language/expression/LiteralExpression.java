package com.wynntils.templates.language.expression;

public class LiteralExpression implements Expression {
    private String stringValue;
    private double numberValue;

    public LiteralExpression(String stringValue) {
        this.stringValue = stringValue;
    }

    public LiteralExpression(double numberValue) {
        this.numberValue = numberValue;
    }

    public boolean hasStringValue() {
        return stringValue != null;
    }

    public boolean hasNumberValue() {
        return !hasStringValue();
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public double getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(double numberValue) {
        this.numberValue = numberValue;
    }
}
