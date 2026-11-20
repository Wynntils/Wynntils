/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text.fonts.wynnfonts;

import com.wynntils.utils.colors.CustomColor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public class BannerPillFont extends AbstractBannerFont {
    private static final Integer EXPECTED_WIDTH = 2;
    public static final FontDescription BANNER_PILL_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("banner/pill"));

    public BannerPillFont() {
        super("wynncraft_banner_pill");
    }

    public static Component buildMessage(
            String text, CustomColor backgroundColor, CustomColor foregroundColor, String suffix) {
        return buildMessage(
                BannerPillFont.class, BANNER_PILL_FONT, EXPECTED_WIDTH, text, backgroundColor, foregroundColor, suffix);
    }

    public static String decodeForeground(String text) {
        return decodeForeground(BannerPillFont.class, text);
    }
}
