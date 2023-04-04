/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text;

import com.wynntils.utils.wynn.WynnUtils;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class StyledTextPart {
    private final String text;
    private PartStyle style;

    private final StyledText parent;

    StyledTextPart(String text, Style style, StyledText parent, Style parentStyle) {
        this.parent = parent;

        // When we have a style, but the text has formatting codes,
        // we need to apply the formatting codes to the style
        StringBuilder textBuilder = new StringBuilder();
        Style textStyle = style;
        boolean formattingNext = false;
        for (char c : text.toCharArray()) {
            if (formattingNext) {
                formattingNext = false;
                ChatFormatting formatting = ChatFormatting.getByCode(c);

                // Color formatting resets the style
                if (formatting.isColor()) {
                    textStyle = Style.EMPTY.withColor(formatting);
                } else {
                    textStyle.applyFormat(formatting);
                }

                continue;
            }

            if (c == '§') {
                formattingNext = true;
                continue;
            }

            textBuilder.append(c);
        }

        this.text = textBuilder.toString();

        // We might have lost an event, so we need to add it back
        textStyle = textStyle.withClickEvent(style.getClickEvent()).withHoverEvent(style.getHoverEvent());

        // Must be done last
        this.style = PartStyle.fromStyle(textStyle, this, parentStyle);
    }

    StyledTextPart(StyledTextPart part, StyledText parent) {
        this.text = part.text;
        this.style = part.style;
        this.parent = parent;
    }

    public String getString(PartStyle previousStyle, PartStyle.StyleType type) {
        return style.asString(previousStyle, type) + text;
    }

    public StyledText getParent() {
        return parent;
    }

    public PartStyle getPartStyle() {
        return style;
    }

    public void setPartStyle(PartStyle style) {
        this.style = style;
    }

    public void setPartStyle(Function<PartStyle, PartStyle> function) {
        this.style = function.apply(style);
    }

    public MutableComponent getComponent() {
        MutableComponent component = Component.literal(text).withStyle(style.getStyle());

        return component;
    }

    StyledTextPart asNormalized() {
        return new StyledTextPart(WynnUtils.normalizeBadString(text), style.getStyle(), parent, null);
    }

    StyledTextPart trim() {
        return new StyledTextPart(text.trim(), style.getStyle(), parent, null);
    }

    boolean isEmpty() {
        return text.isEmpty();
    }

    boolean isBlank() {
        return text.isBlank();
    }

    @Override
    public String toString() {
        return "CodedStringPart[" + "text=" + text + ", " + "style=" + style + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StyledTextPart that = (StyledTextPart) o;
        return Objects.equals(text, that.text)
                && Objects.equals(style, that.style)
                && Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, style, parent);
    }
}
