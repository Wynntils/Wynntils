/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text.fonts;

import com.wynntils.core.components.Managers;
import com.wynntils.core.text.fonts.type.BackgroundEdge;
import com.wynntils.core.text.fonts.wynnfonts.FancyFont;
import com.wynntils.utils.colors.CustomColor;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class WynnFont {
    private static final char NEGATIVE_SPACE = '\uE012';
    private static final char NEGATIVE_SPACE_EDGE = '\u2064';
    private static final char BACKGROUND = '\uE00F';
    private static FontEntry cachedFancyFont;

    private WynnFont() {}

    public static String asBackgroundFont(
            String text, CustomColor textColor, CustomColor backgroundColor, String leftEdge, String rightEdge) {
        StringBuilder sb = new StringBuilder();
        BackgroundEdge left = BackgroundEdge.fromString(leftEdge);
        BackgroundEdge right = BackgroundEdge.fromString(rightEdge);

        FontEntry fancyFont = getFancyFont();
        Map<String, String> fancyGlyphs = fancyFont != null ? fancyFont.glyphs() : Map.of();

        // Tracks whether we are currently "inside" the background
        boolean inBackground = false;

        for (char raw : text.toCharArray()) {
            char c = Character.toLowerCase(raw);

            if (c == ' ') {
                if (inBackground) {
                    sb.append("§")
                            .append(backgroundColor.toHexString())
                            .append(BACKGROUND)
                            .append(NEGATIVE_SPACE)
                            .append(' ');
                } else {
                    sb.append("§").append(textColor.toHexString()).append(' ');
                }
                continue;
            }

            // Look up the mapped glyph
            String fancyStr = fancyGlyphs.get(String.valueOf(c));
            Character fancy = (fancyStr != null && fancyStr.length() == 1) ? fancyStr.charAt(0) : null;

            // If the character is not present in the map then we add the normal version but we also need to close the
            // current background if we are currently inside it.
            if (fancy != null) {
                if (!inBackground) {
                    if (left != BackgroundEdge.NONE) {
                        sb.append("§")
                                .append(backgroundColor.toHexString())
                                .append(left.getLeft())
                                .append(NEGATIVE_SPACE_EDGE);
                    }
                    inBackground = true;
                }

                sb.append("§")
                        .append(backgroundColor.toHexString())
                        .append(BACKGROUND)
                        .append(NEGATIVE_SPACE);

                sb.append("§").append(textColor.toHexString()).append(fancy);
            } else {
                if (inBackground) {
                    if (right != BackgroundEdge.NONE) {
                        sb.append("§")
                                .append(backgroundColor.toHexString())
                                .append(NEGATIVE_SPACE_EDGE)
                                .append(right.getRight());
                    }
                    inBackground = false;
                }
                sb.append("§").append(textColor.toHexString()).append(c);
            }
        }

        // If we are in a background then we need to close it.
        if (inBackground && right != BackgroundEdge.NONE) {
            sb.append("§")
                    .append(backgroundColor.toHexString())
                    .append(NEGATIVE_SPACE_EDGE)
                    .append(right.getRight());
        }

        return sb.toString();
    }

    public static String asFancyFont(String text) {
        FontEntry fancyFont = getFancyFont();
        if (fancyFont == null) {
            return text;
        }

        Map<String, String> glyphs = fancyFont.glyphs();
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == ' ') {
                sb.append(' ');
                continue;
            }
            String fancy = glyphs.get(String.valueOf(c));
            // Uppercase letters reuse the lowercase glyphs when no explicit mapping exists.
            if (fancy == null && Character.isUpperCase(c)) {
                fancy = glyphs.get(String.valueOf(Character.toLowerCase(c)));
            }
            sb.append(fancy != null ? fancy : c);
        }
        return sb.toString();
    }

    public static <T extends RegisteredFont> MutableComponent asFont(String glyphKey, Class<T> fontClass) {
        FontEntry entry = Managers.Font.getFontEntry(fontClass);
        if (entry == null) {
            return Component.literal(glyphKey);
        }

        String glyph = entry.glyphs().get(glyphKey);
        if (glyph == null) {
            return Component.literal(glyphKey);
        }

        return Component.literal(glyph).withStyle(style -> style.withFont(entry.font()));
    }

    private static FontEntry getFancyFont() {
        if (cachedFancyFont != null) {
            return cachedFancyFont;
        }

        FontEntry fancyFont = Managers.Font.getFontEntry(FancyFont.class);
        if (fancyFont != null) {
            cachedFancyFont = fancyFont;
        }
        return fancyFont;
    }
}
