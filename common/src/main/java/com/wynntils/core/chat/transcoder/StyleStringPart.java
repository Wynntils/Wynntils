/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat.transcoder;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class StyleStringPart {
    private final String text;
    private final PartStyle style;

    private final StyleString parent;

    StyleStringPart(String text, Style style, StyleString parent, Style parentStyle) {
        this.text = text;
        this.parent = parent;

        // Must be done last
        this.style = PartStyle.fromStyle(style, this, parentStyle);
    }

    public String getString(PartStyle previousStyle, PartStyle.StyleType type) {
        return style.asString(previousStyle, type) + text;
    }

    public StyleString getParent() {
        return parent;
    }

    public PartStyle getPartStyle() {
        return style;
    }

    public MutableComponent getComponent() {
        MutableComponent component = Component.literal(text).withStyle(style.getStyle());

        return component;
    }

    @Override
    public String toString() {
        return "CodedStringPart[" + "text=" + text + ", " + "style=" + style + ']';
    }
}
