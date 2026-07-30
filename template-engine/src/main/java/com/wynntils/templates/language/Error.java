package com.wynntils.templates.language;

public record Error(int row, int column, String name, String message) {
    @Override
    public String toString() {
        return "Error at row " + row + ", column " + column + ": " + name + " - " + message;
    }
}
