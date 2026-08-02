/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets;

import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.utils.colors.CustomColor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class GuideFilterCategoryShortcut {
    private static final int MAX_PROVIDER_NAMES = 8;

    private final List<Component> tooltip;
    private final int contentOffset;

    public GuideFilterCategoryShortcut(String name, int contentOffset, List<String> providerNames) {
        this.contentOffset = contentOffset;
        this.tooltip = buildTooltip(name, providerNames);
    }

    public int getContentOffset() {
        return contentOffset;
    }

    public List<Component> getTooltip() {
        return tooltip;
    }

    private static List<Component> buildTooltip(String name, List<String> providerNames) {
        List<Component> tooltip = new ArrayList<>();

        tooltip.add(BannerBoxFont.buildMessage(
                name,
                CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY),
                CustomColor.fromChatFormatting(ChatFormatting.WHITE),
                ""));

        providerNames.stream()
                .limit(MAX_PROVIDER_NAMES)
                .map(providerName -> Component.literal(providerName)
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.GRAY)))
                .forEach(tooltip::add);

        if (providerNames.size() > MAX_PROVIDER_NAMES) {
            tooltip.add(Component.literal("... and " + (providerNames.size() - MAX_PROVIDER_NAMES) + " more")
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                            .withColor(ChatFormatting.DARK_GRAY)));
        }

        return tooltip;
    }
}
