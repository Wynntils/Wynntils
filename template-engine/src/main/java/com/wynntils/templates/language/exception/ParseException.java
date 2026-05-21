package com.wynntils.templates.language.exception;


public class ParseException extends LanguageException {
    public final int position;
    public ParseException(int position, String message) {
        super(message);
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
