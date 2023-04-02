/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat.transcoder;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class CodedStringPart {
    private final String text;
    private final CodedStyle style;

    private final CodedString parent;

    private Component componentCache;

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

    public Component getComponent() {
        if (componentCache != null) {
            return componentCache;
        }

        MutableComponent component = Component.literal(text).withStyle(style.getStyle());

        componentCache = component;

        return component;
    }

    void invalidateCache() {
        componentCache = null;
    }

    @Override
    public String toString() {
        return "CodedStringPart[" + "text=" + text + ", " + "style=" + style + ']';
    }
}
