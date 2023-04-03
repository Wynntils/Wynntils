/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat.transcoder;

import com.wynntils.utils.colors.CustomColor;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

public final class CodedStyle {
    private static final String STYLE_PREFIX = "§";

    private final CodedStringPart owner;

    private final CustomColor color;
    private final boolean bold;
    private final boolean italic;
    private final boolean underlined;
    private final boolean strikethrough;
    private final boolean obfuscated;
    private final ClickEvent clickEvent;
    private final HoverEvent hoverEvent;

    private Style styleCache;

    private CodedStyle(
            CodedStringPart owner,
            CustomColor color,
            boolean bold,
            boolean italic,
            boolean underlined,
            boolean strikethrough,
            boolean obfuscated,
            ClickEvent clickEvent,
            HoverEvent hoverEvent) {
        this.owner = owner;
        this.color = color;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.obfuscated = obfuscated;
        this.clickEvent = clickEvent;
        this.hoverEvent = hoverEvent;
    }

    static CodedStyle fromStyle(Style style, CodedStringPart owner, CodedStringPart partBefore) {
        Style inheritedStyle;

        if (partBefore == null) {
            inheritedStyle = style;
        } else {
            // This changes properties that are null, as-in, inherting from the previous style.
            inheritedStyle = style.applyTo(partBefore.getCodedStyle().getStyle());

            // We don't want to inherit these properties.
            inheritedStyle.withClickEvent(style.getClickEvent());
            inheritedStyle.withHoverEvent(style.getHoverEvent());
            inheritedStyle.withInsertion(style.getInsertion());
            inheritedStyle.withFont(style.getFont());
        }

        return new CodedStyle(
                owner,
                inheritedStyle.getColor() == null
                        ? CustomColor.NONE
                        : CustomColor.fromInt(inheritedStyle.getColor().getValue()),
                inheritedStyle.isBold(),
                inheritedStyle.isItalic(),
                inheritedStyle.isUnderlined(),
                inheritedStyle.isStrikethrough(),
                inheritedStyle.isObfuscated(),
                inheritedStyle.getClickEvent(),
                inheritedStyle.getHoverEvent());
    }

    public String asString() {
        // Rules of converting a Style to a String:
        // Every style is prefixed with a §.
        // 0. Every style string is fully qualified, meaning that it contains all the formatting, and reset if needed.
        // 1. Style color is converted to a color segment.
        //    A color segment is the prefix and the chatFormatting char.
        //    If this is a custom color, a hex color code is used.
        //    Example: §#FF0000 or §1
        // 2. Formatting is converted the same way as in the Style class.
        // 3. Click events are wrapped in square brackets, and is reprenseted as an id.
        //    The parent of this style's owner is responsible for keeping track of click events.
        //    Example: §[1] -> (1st click event)
        // 4. Hover events are wrapped in angle brackets, and is represented as an id.
        //    The parent of this style's owner is responsible for keeping track of hover events.
        //    Example: §<1> -> (1st hover event)

        StringBuilder styleString = new StringBuilder();

        CodedStringPart partBefore = owner.getPartBefore();

        if (partBefore != null) {
            if (!this.isCompatibleWith(partBefore.getCodedStyle())) {
                styleString.append(STYLE_PREFIX).append(ChatFormatting.RESET.getChar());
            }
        }

        // 1. Color
        if (color != CustomColor.NONE) {
            Optional<ChatFormatting> chatFormatting = Arrays.stream(ChatFormatting.values())
                    .filter(ChatFormatting::isColor)
                    .filter(c -> c.getColor() == color.asInt())
                    .findFirst();

            if (chatFormatting.isPresent()) {
                styleString.append(STYLE_PREFIX).append(chatFormatting.get().getChar());
            } else {
                styleString.append(STYLE_PREFIX).append(color.toHexString());
            }
        }

        // 2. Formatting
        if (bold) {
            styleString.append(STYLE_PREFIX).append(ChatFormatting.BOLD.getChar());
        }
        if (italic) {
            styleString.append(STYLE_PREFIX).append(ChatFormatting.ITALIC.getChar());
        }
        if (underlined) {
            styleString.append(STYLE_PREFIX).append(ChatFormatting.UNDERLINE.getChar());
        }
        if (strikethrough) {
            styleString.append(STYLE_PREFIX).append(ChatFormatting.STRIKETHROUGH.getChar());
        }
        if (obfuscated) {
            styleString.append(STYLE_PREFIX).append(ChatFormatting.OBFUSCATED.getChar());
        }

        // 3. Click event
        if (clickEvent != null) {
            styleString
                    .append(STYLE_PREFIX)
                    .append("[")
                    .append(owner.getParent().addClickEvent(clickEvent))
                    .append("]");
        }

        // 4. Hover event
        if (hoverEvent != null) {
            styleString
                    .append(STYLE_PREFIX)
                    .append("<")
                    .append(owner.getParent().addHoverEvent(hoverEvent))
                    .append(">");
        }

        return styleString.toString();
    }

    public Style getStyle() {
        if (styleCache != null) {
            return styleCache;
        }

        Style reconstructedStyle = Style.EMPTY
                .withBold(bold)
                .withItalic(italic)
                .withUnderlined(underlined)
                .withStrikethrough(strikethrough)
                .withObfuscated(obfuscated)
                .withClickEvent(clickEvent)
                .withHoverEvent(hoverEvent);

        if (color != CustomColor.NONE) {
            reconstructedStyle = reconstructedStyle.withColor(color.asInt());
        }

        styleCache = reconstructedStyle;

        return reconstructedStyle;
    }

    private boolean isCompatibleWith(CodedStyle other) {
        // A style is compatible with another style if:
        // 1. If the color is non-null in this style, it is the same as the other style or other style's color is null.
        // 2. If the other formatting is false, this formatting can be false or true.
        // 3. If the other formatting is true, this formatting must be true.

        if (color != CustomColor.NONE && other.color != CustomColor.NONE) {
            if (!color.equals(other.color)) {
                return false;
            }
        }

        if (color == CustomColor.NONE && other.color != CustomColor.NONE) {
            return false;
        }

        if (!bold && other.bold) {
            return false;
        }

        if (!italic && other.italic) {
            return false;
        }

        if (!underlined && other.underlined) {
            return false;
        }

        if (!strikethrough && other.strikethrough) {
            return false;
        }

        if (!obfuscated && other.obfuscated) {
            return false;
        }

        return true;
    }

    void invalidateCache() {
        styleCache = null;
    }

    @Override
    public String toString() {
        return "CodedStyle{" + "color="
                + color + ", bold="
                + bold + ", italic="
                + italic + ", underlined="
                + underlined + ", strikethrough="
                + strikethrough + ", obfuscated="
                + obfuscated + ", clickEvent="
                + clickEvent + ", hoverEvent="
                + hoverEvent + '}';
    }
}
