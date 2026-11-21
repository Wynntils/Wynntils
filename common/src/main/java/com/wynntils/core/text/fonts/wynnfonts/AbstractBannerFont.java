/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text.fonts.wynnfonts;

import com.wynntils.core.components.Managers;
import com.wynntils.core.text.fonts.FontEntry;
import com.wynntils.core.text.fonts.RegisteredFont;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public abstract class AbstractBannerFont extends RegisteredFont {
    protected AbstractBannerFont(String key) {
        super(key);
    }

    protected static Component buildMessage(
            Class<? extends AbstractBannerFont> fontClass,
            FontDescription font,
            int expectedWidth,
            String text,
            CustomColor backgroundColor,
            CustomColor foregroundColor,
            String suffix) {
        text = text.toLowerCase(Locale.ROOT);

        MutableComponent component =
                Component.empty().withStyle(Style.EMPTY.withFont(font)).withoutShadow();

        StringBuilder background = new StringBuilder();

        background.append(WynnFont.toGlyph("left_edge", fontClass));
        background.append("\uDAFF\uDFFF");

        for (char character : text.toCharArray()) {
            if (character == ' ') {
                background.append(WynnFont.toGlyph("space", fontClass));
            } else {
                background.append(WynnFont.toGlyph(character + "_background", fontClass));
            }

            background.append("\uDAFF\uDFFF");
        }

        background.append(WynnFont.toGlyph("right_edge", fontClass));

        component.append(Component.literal(background.toString()).withColor(backgroundColor.asInt()));

        component.append(Component.literal(
                Managers.Font.calculateOffset(McUtils.mc().font.width(component), expectedWidth)));

        StringBuilder foreground = new StringBuilder();

        for (char character : text.toCharArray()) {
            if (character == ' ') {
                foreground.append(" ");
            } else {
                foreground.append(WynnFont.toGlyph(character + "_foreground", fontClass));
            }
        }

        foreground.append(suffix);

        component.append(Component.literal(foreground.toString()).withColor(foregroundColor.asInt()));

        return component;
    }

    // Decodes the foreground section of a piece of text using the given banner font
    protected static String decodeForeground(Class<? extends AbstractBannerFont> fontClass, String text) {
        FontEntry entry = Managers.Font.getFontEntry(fontClass);

        if (entry == null) return "";

        Map<Integer, String> letters = new HashMap<>();
        entry.glyphs().forEach((key, glyph) -> {
            if (key.endsWith("_foreground")) {
                letters.put(glyph.codePointAt(0), key.substring(0, key.length() - "_foreground".length()));
            }
        });

        StringBuilder decoded = new StringBuilder();
        text.codePoints().forEach(codePoint -> {
            if (codePoint == ' ') {
                decoded.append(' ');
            } else {
                decoded.append(letters.getOrDefault(codePoint, ""));
            }
        });

        return decoded.toString().trim();
    }
}
