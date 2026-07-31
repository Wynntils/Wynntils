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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class BannerSymbolFont extends RegisteredFont {
    private static final Integer EXPECTED_WIDTH = 1;
    public static final FontDescription BANNER_SYMBOL_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("banner/symbol"));

    public BannerSymbolFont() {
        super("wynncraft_banner_symbol");
    }

    public static Component buildMessage(
            int stars,
            int activeStars,
            CustomColor backgroundColor,
            CustomColor starColor,
            CustomColor inactiveStarColor,
            String suffix) {
        MutableComponent component = Component.empty()
                .withStyle(Style.EMPTY.withFont(BANNER_SYMBOL_FONT))
                .withoutShadow();

        StringBuilder background = new StringBuilder();

        background.append(WynnFont.toGlyph("left_edge", BannerSymbolFont.class));
        background.append("\uDAFF\uDFFF");

        for (int i = 0; i < stars; i++) {
            background.append(WynnFont.toGlyph("star_background", BannerSymbolFont.class));

            background.append("\uDAFF\uDFFF");
        }

        background.append(WynnFont.toGlyph("right_edge", BannerSymbolFont.class));

        component.append(Component.literal(background.toString()).withColor(backgroundColor.asInt()));

        component.append(Component.literal(
                Managers.Font.calculateOffset(McUtils.mc().font.width(component), EXPECTED_WIDTH)));

        StringBuilder activeStarsForeground = new StringBuilder();

        for (int i = 0; i < activeStars; i++) {
            activeStarsForeground.append(WynnFont.toGlyph("star_foreground", BannerSymbolFont.class));
        }

        component.append(Component.literal(activeStarsForeground.toString()).withColor(starColor.asInt()));

        if (stars - activeStars > 0) {
            StringBuilder inactiveStarsForeground = new StringBuilder();

            for (int i = 0; i < stars - activeStars; i++) {
                inactiveStarsForeground.append(WynnFont.toGlyph("star_foreground", BannerSymbolFont.class));
            }

            component.append(
                    Component.literal(inactiveStarsForeground.toString()).withColor(inactiveStarColor.asInt()));
        }

        component.append(suffix);

        return component;
    }
}
