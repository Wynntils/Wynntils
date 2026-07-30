/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.language.exception;

public class LexException extends LanguageException {
    private final int position;

    public LexException(int position, String message) {
        super(message);
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
