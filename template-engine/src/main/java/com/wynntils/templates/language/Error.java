package com.wynntils.templates.language;

public record Error(int row, int column, String name, String message) {
}
