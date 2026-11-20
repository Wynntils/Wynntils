/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text.fonts.wynnfonts;

import com.wynntils.utils.colors.CustomColor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public class BannerRibbonFont extends AbstractBannerFont {
    private static final Integer EXPECTED_WIDTH = 7;
    public static final FontDescription BANNER_RIBBON_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("banner/ribbon"));

    public BannerRibbonFont() {
        super("wynncraft_banner_ribbon");
    }

    public static Component buildMessage(
            String text, CustomColor backgroundColor, CustomColor foregroundColor, String suffix) {
        return buildMessage(
                BannerRibbonFont.class,
                BANNER_RIBBON_FONT,
                EXPECTED_WIDTH,
                text,
                backgroundColor,
                foregroundColor,
                suffix);
    }

    public static String decodeForeground(String text) {
        return decodeForeground(BannerRibbonFont.class, text);
    }
}
