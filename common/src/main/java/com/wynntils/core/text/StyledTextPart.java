/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text;

import com.wynntils.utils.wynn.WynnUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class StyledTextPart {
    private final String text;
    private final PartStyle style;

    private final StyledText parent;

    public StyledTextPart(String text, Style style, StyledText parent, Style parentStyle) {
        this.parent = parent;
        this.text = text;

        // Must be done last
        this.style = PartStyle.fromStyle(style, this, parentStyle);
    }

    StyledTextPart(StyledTextPart part, StyledText parent) {
        this.text = part.text;
        this.style = new PartStyle(part.style, this);
        this.parent = parent;
    }

    private StyledTextPart(StyledTextPart part, PartStyle style, StyledText parent) {
        this.text = part.text;
        this.style = style;
        this.parent = parent;
    }

    // This factory is used to create a StyledTextPart from a component that has formatting codes
    // It is separate from the constructor because this only needs to be applied in cases there the text could have
    // formatting codes
    static List<StyledTextPart> fromCodedString(String codedString, Style style, StyledText parent, Style parentStyle) {
        // When we have a style, but the text has formatting codes,
        // we need to apply the formatting codes to the style
        // This means that the actual style applies first; then the formatting codes
        List<StyledTextPart> parts = new ArrayList<>();

        Style currentStyle = style;
        StringBuilder currentString = new StringBuilder();

        boolean nextIsFormatting = false;

        for (char current : codedString.toCharArray()) {
            if (nextIsFormatting) {
                nextIsFormatting = false;

                ChatFormatting formatting = ChatFormatting.getByCode(current);

                if (formatting == null) {
                    currentString.append(ChatFormatting.PREFIX_CODE);
                    currentString.append(current);
                    continue;
                }

                // We already had some text with the current style
                // Append it before modifying the style
                if (!currentString.isEmpty()) {
                    // We might have lost an event, so we need to add it back
                    currentStyle =
                            currentStyle.withClickEvent(style.getClickEvent()).withHoverEvent(style.getHoverEvent());

                    parts.add(new StyledTextPart(currentString.toString(), currentStyle, null, parentStyle));

                    // reset string
                    // style is not reset, because we want to keep the formatting
                    currentString = new StringBuilder();
                }

                // Color formatting resets the style
                if (formatting.isColor()) {
                    currentStyle = Style.EMPTY.withColor(formatting);
                    continue;
                }

                currentStyle = currentStyle.applyFormat(formatting);

                continue;
            }

            if (current == ChatFormatting.PREFIX_CODE) {
                nextIsFormatting = true;
                continue;
            }

            currentString.append(current);
        }

        // Check if we have some text left
        if (!currentString.isEmpty()) {
            // We might have lost an event, so we need to add it back
            currentStyle = currentStyle.withClickEvent(style.getClickEvent()).withHoverEvent(style.getHoverEvent());
            parts.add(new StyledTextPart(currentString.toString(), currentStyle, null, parentStyle));
        }

        return parts;
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

    public StyledTextPart withStyle(PartStyle style) {
        return new StyledTextPart(this, style, parent);
    }

    public StyledTextPart withStyle(Function<PartStyle, PartStyle> function) {
        return withStyle(function.apply(style));
    }

    public MutableComponent getComponent() {
        return Component.literal(text).withStyle(style.getStyle());
    }

    StyledTextPart asNormalized() {
        return new StyledTextPart(WynnUtils.normalizeBadString(text), style.getStyle(), parent, null);
    }

    StyledTextPart stripLeading() {
        return new StyledTextPart(text.stripLeading(), style.getStyle(), parent, null);
    }

    StyledTextPart stripTrailing() {
        return new StyledTextPart(text.stripTrailing(), style.getStyle(), parent, null);
    }

    boolean isEmpty() {
        return text.isEmpty();
    }

    boolean isBlank() {
        return text.isBlank();
    }

    public int length() {
        return text.length();
    }

    @Override
    public String toString() {
        return "StyledTextPart[" + "text=" + text + ", " + "style=" + style + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StyledTextPart that = (StyledTextPart) o;
        return Objects.equals(text, that.text) && Objects.equals(style, that.style);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, style);
    }
}
