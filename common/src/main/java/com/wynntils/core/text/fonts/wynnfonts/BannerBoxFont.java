/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text.fonts.wynnfonts;

import com.wynntils.core.components.Managers;
import com.wynntils.core.text.fonts.RegisteredFont;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class BannerBoxFont extends RegisteredFont {
    private static final Integer EXPECTED_WIDTH = 2;
    public static final FontDescription BANNER_BOX_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("banner/box"));

    public BannerBoxFont() {
        super("wynncraft_banner_box");
    }

    public static Component buildMessage(
            String text, CustomColor backgroundColor, CustomColor foregroundColor, String suffix) {
        text = text.toLowerCase(Locale.ROOT);

        MutableComponent component = Component.empty()
                .withStyle(Style.EMPTY.withFont(BANNER_BOX_FONT))
                .withoutShadow();

        StringBuilder background = new StringBuilder();

        background.append(WynnFont.toGlyph("left_edge", BannerBoxFont.class));
        background.append("\uDAFF\uDFFF");

        for (char character : text.toCharArray()) {
            if (character == ' ') {
                background.append(WynnFont.toGlyph("space", BannerBoxFont.class));
            } else {
                background.append(WynnFont.toGlyph(character + "_background", BannerBoxFont.class));
            }

            background.append("\uDAFF\uDFFF");
        }

        background.append(WynnFont.toGlyph("right_edge", BannerBoxFont.class));

        component.append(Component.literal(background.toString()).withColor(backgroundColor.asInt()));

        component.append(Component.literal(
                Managers.Font.calculateOffset(McUtils.mc().font.width(component), EXPECTED_WIDTH)));

        StringBuilder foreground = new StringBuilder();

        for (char character : text.toCharArray()) {
            if (character == ' ') {
                foreground.append(" ");
            } else {
                foreground.append(WynnFont.toGlyph(character + "_foreground", BannerBoxFont.class));
            }
        }

        foreground.append(suffix);

        component.append(Component.literal(foreground.toString()).withColor(foregroundColor.asInt()));

        return component;
    }
}
