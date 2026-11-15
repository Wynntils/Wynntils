/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text.fonts.wynnfonts;

import com.wynntils.utils.colors.CustomColor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public class BannerFlagFont extends AbstractBannerFont {
    private static final Integer EXPECTED_WIDTH = 2;
    public static final FontDescription BANNER_FLAG_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("banner/flag"));

    public BannerFlagFont() {
        super("wynncraft_banner_flag");
    }

    public static Component buildMessage(
            String text, CustomColor backgroundColor, CustomColor foregroundColor, String suffix) {
        return buildMessage(
                BannerFlagFont.class, BANNER_FLAG_FONT, EXPECTED_WIDTH, text, backgroundColor, foregroundColor, suffix);
    }

    public static String decodeForeground(String text) {
        return decodeForeground(BannerFlagFont.class, text);
    }
}
