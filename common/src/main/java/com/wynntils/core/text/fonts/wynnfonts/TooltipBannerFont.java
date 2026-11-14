/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text.fonts.wynnfonts;

import com.wynntils.core.components.Managers;
import com.wynntils.core.text.fonts.RegisteredFont;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.type.TooltipBannerIcon;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class TooltipBannerFont extends RegisteredFont {
    private static final Integer EXPECTED_WIDTH = 0;
    public static final FontDescription TOOLTIP_BANNER_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/banner"));

    public TooltipBannerFont() {
        super("wynncraft_tooltip_banner");
    }

    public static Component buildMessage(TooltipBannerIcon bannerIcon, CustomColor backgroundColor, String suffix) {
        MutableComponent component = Component.empty()
                .withStyle(Style.EMPTY.withFont(TOOLTIP_BANNER_FONT))
                .withoutShadow();

        component.append(
                Component.literal(WynnFont.toGlyph(bannerIcon.getKey() + "_background", TooltipBannerFont.class))
                        .withColor(backgroundColor.asInt()));

        component.append(Component.literal(
                Managers.Font.calculateOffset(McUtils.mc().font.width(component), EXPECTED_WIDTH)));

        String foreground = WynnFont.toGlyph(bannerIcon.getKey() + "_foreground", TooltipBannerFont.class) + suffix;

        component.append(Component.literal(foreground));

        return component;
    }
}
