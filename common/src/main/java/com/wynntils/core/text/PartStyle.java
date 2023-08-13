/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text;

import com.wynntils.utils.colors.CustomColor;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

public final class PartStyle {
    private static final String STYLE_PREFIX = "§";

    private final StyledTextPart owner;

    private final CustomColor color;
    private final boolean obfuscated;
    private final boolean bold;
    private final boolean strikethrough;
    private final boolean underlined;
    private final boolean italic;
    private final ClickEvent clickEvent;
    private final HoverEvent hoverEvent;

    private PartStyle(
            StyledTextPart owner,
            CustomColor color,
            boolean obfuscated,
            boolean bold,
            boolean strikethrough,
            boolean underlined,
            boolean italic,
            ClickEvent clickEvent,
            HoverEvent hoverEvent) {
        this.owner = owner;
        this.color = color;
        this.obfuscated = obfuscated;
        this.bold = bold;
        this.strikethrough = strikethrough;
        this.underlined = underlined;
        this.italic = italic;
        this.clickEvent = clickEvent;
        this.hoverEvent = hoverEvent;
    }

    PartStyle(PartStyle partStyle, StyledTextPart owner) {
        this.owner = owner;
        this.color = partStyle.color;
        this.obfuscated = partStyle.obfuscated;
        this.bold = partStyle.bold;
        this.strikethrough = partStyle.strikethrough;
        this.underlined = partStyle.underlined;
        this.italic = partStyle.italic;
        this.clickEvent = partStyle.clickEvent;
        this.hoverEvent = partStyle.hoverEvent;
    }

    static PartStyle fromStyle(Style style, StyledTextPart owner, Style parentStyle) {
        Style inheritedStyle;

        if (parentStyle == null) {
            inheritedStyle = style;
        } else {
            // This changes properties that are null, as-in, inherting from the previous style.
            inheritedStyle = style.applyTo(parentStyle);

            // We don't want to inherit these properties.
            inheritedStyle = inheritedStyle
                    .withClickEvent(style.getClickEvent())
                    .withHoverEvent(style.getHoverEvent())
                    .withInsertion(style.getInsertion())
                    .withFont(style.getFont());
        }

        return new PartStyle(
                owner,
                inheritedStyle.getColor() == null
                        ? CustomColor.NONE
                        : CustomColor.fromInt(inheritedStyle.getColor().getValue()),
                inheritedStyle.isObfuscated(),
                inheritedStyle.isBold(),
                inheritedStyle.isStrikethrough(),
                inheritedStyle.isUnderlined(),
                inheritedStyle.isItalic(),
                inheritedStyle.getClickEvent(),
                inheritedStyle.getHoverEvent());
    }

    public String asString(PartStyle previousStyle, StyleType type) {
        // Rules of converting a Style to a String:
        // Every style is prefixed with a §.
        // 0. Every style string is fully qualified, meaning that it contains all the formatting, and reset if needed.
        // 1. Style color is converted to a color segment.
        //    A color segment is the prefix and the chatFormatting char.
        //    If this is a custom color, a hex color code is used.
        //    Example: §#FF0000 or §1
        // 2. Formatting is converted the same way as in the Style class.
        // 3. Click events are wrapped in square brackets, and is represented as an id.
        //    The parent of this style's owner is responsible for keeping track of click events.
        //    Example: §[1] -> (1st click event)
        // 4. Hover events are wrapped in angle brackets, and is represented as an id.
        //    The parent of this style's owner is responsible for keeping track of hover events.
        //    Example: §<1> -> (1st hover event)

        if (type == StyleType.NONE) return "";

        StringBuilder styleString = new StringBuilder();

        boolean skipFormatting = false;

        // If the color is the same as the previous style, we can try to construct a difference.
        // If colors don't match, the inserted color will reset the formatting, thus we need to include all formatting.
        // If the current color is NONE, we NEED to try to construct a difference,
        // since there will be no color formatting resetting the formatting afterwards.
        if (previousStyle != null && (color == CustomColor.NONE || previousStyle.color.equals(color))) {
            String differenceString = this.tryConstructDifference(previousStyle);

            if (differenceString != null) {
                styleString.append(differenceString);
                skipFormatting = true;
            } else {
                styleString.append(STYLE_PREFIX).append(ChatFormatting.RESET.getChar());
            }
        }

        if (!skipFormatting) {
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
            if (obfuscated) {
                styleString.append(STYLE_PREFIX).append(ChatFormatting.OBFUSCATED.getChar());
            }
            if (bold) {
                styleString.append(STYLE_PREFIX).append(ChatFormatting.BOLD.getChar());
            }
            if (strikethrough) {
                styleString.append(STYLE_PREFIX).append(ChatFormatting.STRIKETHROUGH.getChar());
            }
            if (underlined) {
                styleString.append(STYLE_PREFIX).append(ChatFormatting.UNDERLINE.getChar());
            }
            if (italic) {
                styleString.append(STYLE_PREFIX).append(ChatFormatting.ITALIC.getChar());
            }
        }

        if (type == StyleType.INCLUDE_EVENTS) {
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
        }

        return styleString.toString();
    }

    public Style getStyle() {
        Style reconstructedStyle = Style.EMPTY
                .withObfuscated(obfuscated)
                .withBold(bold)
                .withStrikethrough(strikethrough)
                .withUnderlined(underlined)
                .withItalic(italic)
                .withClickEvent(clickEvent)
                .withHoverEvent(hoverEvent);

        if (color != CustomColor.NONE) {
            reconstructedStyle = reconstructedStyle.withColor(color.asInt());
        }

        return reconstructedStyle;
    }

    public PartStyle withColor(ChatFormatting color) {
        if (!color.isColor()) {
            throw new IllegalArgumentException("ChatFormatting " + color + " is not a color!");
        }

        CustomColor newColor = CustomColor.fromInt(color.getColor());

        return new PartStyle(
                owner, newColor, obfuscated, bold, strikethrough, underlined, italic, clickEvent, hoverEvent);
    }

    public boolean isBold() {
        return bold;
    }

    public boolean isObfuscated() {
        return obfuscated;
    }

    public boolean isStrikethrough() {
        return strikethrough;
    }

    public boolean isUnderlined() {
        return underlined;
    }

    public boolean isItalic() {
        return italic;
    }

    public PartStyle withBold(boolean bold) {
        return new PartStyle(owner, color, obfuscated, bold, strikethrough, underlined, italic, clickEvent, hoverEvent);
    }

    public PartStyle withObfuscated(boolean obfuscated) {
        return new PartStyle(owner, color, obfuscated, bold, strikethrough, underlined, italic, clickEvent, hoverEvent);
    }

    public PartStyle withStrikethrough(boolean strikethrough) {
        return new PartStyle(owner, color, obfuscated, bold, strikethrough, underlined, italic, clickEvent, hoverEvent);
    }

    public PartStyle withUnderlined(boolean underlined) {
        return new PartStyle(owner, color, obfuscated, bold, strikethrough, underlined, italic, clickEvent, hoverEvent);
    }

    public PartStyle withItalic(boolean italic) {
        return new PartStyle(owner, color, obfuscated, bold, strikethrough, underlined, italic, clickEvent, hoverEvent);
    }

    public PartStyle withClickEvent(ClickEvent clickEvent) {
        return new PartStyle(owner, color, obfuscated, bold, strikethrough, underlined, italic, clickEvent, hoverEvent);
    }

    public PartStyle withHoverEvent(HoverEvent hoverEvent) {
        return new PartStyle(owner, color, obfuscated, bold, strikethrough, underlined, italic, clickEvent, hoverEvent);
    }

    private String tryConstructDifference(PartStyle oldStyle) {
        StringBuilder add = new StringBuilder();

        int oldColorInt = oldStyle.color.asInt();
        int newColorInt = this.color.asInt();

        if (oldColorInt == -1) {
            if (newColorInt != -1) {
                Arrays.stream(ChatFormatting.values())
                        .filter(c -> c.isColor() && newColorInt == c.getColor())
                        .findFirst()
                        .ifPresent(add::append);
            }
        } else if (oldColorInt != newColorInt) {
            return null;
        }

        if (oldStyle.obfuscated && !this.obfuscated) return null;
        if (!oldStyle.obfuscated && this.obfuscated) add.append(ChatFormatting.OBFUSCATED);

        if (oldStyle.bold && !this.bold) return null;
        if (!oldStyle.bold && this.bold) add.append(ChatFormatting.BOLD);

        if (oldStyle.strikethrough && !this.strikethrough) return null;
        if (!oldStyle.strikethrough && this.strikethrough) add.append(ChatFormatting.STRIKETHROUGH);

        if (oldStyle.underlined && !this.underlined) return null;
        if (!oldStyle.underlined && this.underlined) add.append(ChatFormatting.UNDERLINE);

        if (oldStyle.italic && !this.italic) return null;
        if (!oldStyle.italic && this.italic) add.append(ChatFormatting.ITALIC);

        return add.toString();
    }

    @Override
    public String toString() {
        return "PartStyle{" + "color="
                + color + ", bold="
                + bold + ", italic="
                + italic + ", underlined="
                + underlined + ", strikethrough="
                + strikethrough + ", obfuscated="
                + obfuscated + ", clickEvent="
                + clickEvent + ", hoverEvent="
                + hoverEvent + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartStyle partStyle = (PartStyle) o;
        return bold == partStyle.bold
                && italic == partStyle.italic
                && underlined == partStyle.underlined
                && strikethrough == partStyle.strikethrough
                && obfuscated == partStyle.obfuscated
                && Objects.equals(color, partStyle.color)
                && Objects.equals(clickEvent, partStyle.clickEvent)
                && Objects.equals(hoverEvent, partStyle.hoverEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, bold, italic, underlined, strikethrough, obfuscated, clickEvent, hoverEvent);
    }

    public enum StyleType {
        INCLUDE_EVENTS, // Includes click and hover events
        DEFAULT, // The most minimal way to represent a style
        NONE // No styling
    }
}
