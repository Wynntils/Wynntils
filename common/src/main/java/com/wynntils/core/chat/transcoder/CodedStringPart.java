/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat.transcoder;

import net.minecraft.network.chat.Style;

public final class CodedStringPart {
    private final String text;
    private final CodedStyle style;

    private final CodedString parent;

    CodedStringPart(String text, Style style, CodedString parent) {
        this.text = text;
        this.style = CodedStyle.fromStyle(style, this);
        this.parent = parent;
    }

    public String getCoded() {
        return style.asString() + text;
    }

    public CodedString getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return "CodedStringPart[" + "text=" + text + ", " + "style=" + style + ']';
    }
}
