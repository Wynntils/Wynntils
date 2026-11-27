/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.paths;

import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public record PathNoteStyle(
        Identifier font,
        CustomColor color,
        CustomColor shadowColor,
        boolean bold,
        boolean italic,
        boolean underlined,
        boolean strikethrough) {
    public static PathNoteStyle from(Component component) {
        Style style = component
                .visit((partStyle, text) -> text.isEmpty() ? Optional.empty() : Optional.of(partStyle), Style.EMPTY)
                .orElse(component.getStyle());
        Identifier font = style.getFont() instanceof FontDescription.Resource(Identifier id)
                ? id
                : Identifier.withDefaultNamespace("default");

        return new PathNoteStyle(
                font,
                style.getColor() == null
                        ? CommonColors.WHITE
                        : CustomColor.fromInt(0xff000000 | style.getColor().getValue()),
                style.getShadowColor() == null ? CommonColors.BLACK : CustomColor.fromARGBInt(style.getShadowColor()),
                style.isBold(),
                style.isItalic(),
                style.isUnderlined(),
                style.isStrikethrough());
    }

    public Component component(String text) {
        return Component.literal(text).withStyle(fullStyle());
    }

    public Component apply(Component original, String text) {
        if (!text.equals(original.getString())) {
            return Component.literal(text).withStyle(fullStyle().applyTo(original.getStyle()));
        }

        PathNoteStyle initial = from(original);
        if (equals(initial)) return original;

        return restyle(original, initial);
    }

    private Style fullStyle() {
        return Style.EMPTY
                .withFont(new FontDescription.Resource(font))
                .withColor(color.asInt())
                .withShadowColor(shadowColor.asInt())
                .withBold(bold)
                .withItalic(italic)
                .withUnderlined(underlined)
                .withStrikethrough(strikethrough);
    }

    private MutableComponent restyle(Component source, PathNoteStyle initial) {
        Style style = source.getStyle();
        if (!font.equals(initial.font())) {
            style = style.withFont(new FontDescription.Resource(font));
        }
        if (!color.equals(initial.color())) {
            style = style.withColor(color.asInt());
        }
        if (!shadowColor.equals(initial.shadowColor())) {
            style = style.withShadowColor(shadowColor.asInt());
        }
        if (bold != initial.bold()) {
            style = style.withBold(bold);
        }
        if (italic != initial.italic()) {
            style = style.withItalic(italic);
        }
        if (underlined != initial.underlined()) {
            style = style.withUnderlined(underlined);
        }
        if (strikethrough != initial.strikethrough()) {
            style = style.withStrikethrough(strikethrough);
        }

        MutableComponent result = MutableComponent.create(source.getContents()).withStyle(style);
        for (Component sibling : source.getSiblings()) {
            result.append(restyle(sibling, initial));
        }

        return result;
    }
}
