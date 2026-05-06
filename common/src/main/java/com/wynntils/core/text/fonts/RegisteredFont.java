/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text.fonts;

public abstract class RegisteredFont {
    private final String key;
    private FontEntry fontEntry;

    protected RegisteredFont(String key) {
        this.key = key;
    }

    public final String key() {
        return key;
    }

    public final FontEntry fontEntry() {
        return fontEntry;
    }

    final void setFontEntry(FontEntry fontEntry) {
        this.fontEntry = fontEntry;
    }
}
