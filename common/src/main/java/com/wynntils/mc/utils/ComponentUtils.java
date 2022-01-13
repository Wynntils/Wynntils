/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.utils;

import java.util.Arrays;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class ComponentUtils {
    public static String getUnformatted(Component msg) {
        return msg.getString();
    }

    public static String fromComponent(Component component) {
        StringBuilder result = new StringBuilder();
        Style oldStyle = Style.EMPTY;

        component.visit(
                (style, string) -> {
                    if (!oldStyle.isEmpty()) {
                        StringBuilder different =
                                getToAdd(oldStyle, style); // Try to get difference to add
                        if (different != null) {
                            result.append(different);
                            return Optional.empty();
                        }

                        result.append(ChatFormatting.RESET);
                    }

                    if (style.getColor() != null) {
                        Optional<ChatFormatting> color = getChatFormatting(style.getColor());
                        color.ifPresent(result::append);
                    }

                    if (style.isBold()) result.append(ChatFormatting.BOLD);
                    if (style.isItalic()) result.append(ChatFormatting.ITALIC);
                    if (style.isUnderlined()) result.append(ChatFormatting.UNDERLINE);
                    if (style.isStrikethrough()) result.append(ChatFormatting.STRIKETHROUGH);
                    if (style.isObfuscated()) result.append(ChatFormatting.OBFUSCATED);

                    return Optional.empty(); // dont break
                },
                Style.EMPTY);

        return result.toString();
    }

    private static StringBuilder getToAdd(Style oldStyle, Style newStyle) {
        int oldColorInt =
                Optional.ofNullable(oldStyle.getColor()).map(TextColor::getValue).orElse(-1);
        int newColorInt =
                Optional.ofNullable(newStyle.getColor()).map(TextColor::getValue).orElse(-1);

        StringBuilder add = new StringBuilder();

        if (oldColorInt != newColorInt) return null;

        if (oldStyle.isBold() && !newStyle.isBold()) return null;
        if (!oldStyle.isBold() && newStyle.isBold()) add.append(ChatFormatting.BOLD);

        if (oldStyle.isItalic() && !newStyle.isItalic()) return null;
        if (!oldStyle.isItalic() && newStyle.isItalic()) add.append(ChatFormatting.ITALIC);

        if (oldStyle.isUnderlined() && !newStyle.isUnderlined()) return null;
        if (!oldStyle.isUnderlined() && newStyle.isUnderlined())
            add.append(ChatFormatting.UNDERLINE);

        if (oldStyle.isStrikethrough() && !newStyle.isStrikethrough()) return null;
        if (!oldStyle.isStrikethrough() && newStyle.isStrikethrough())
            add.append(ChatFormatting.STRIKETHROUGH);

        if (oldStyle.isObfuscated() && !newStyle.isObfuscated()) return null;
        if (!oldStyle.isObfuscated() && newStyle.isObfuscated())
            add.append(ChatFormatting.OBFUSCATED);

        return add;
    }

    public static Optional<ChatFormatting> getChatFormatting(TextColor textColor) {
        return Arrays.stream(ChatFormatting.values())
                .filter(c -> c.isColor() && textColor.getValue() == c.getColor())
                .findFirst();
    }
}
