/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components.gear;

import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.utils.colors.CommonColors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class RerollBannerComponent {
    private static final Style WYNNCRAFT_WHITE_STYLE = Style.EMPTY
            .withFont(IdentifiableTooltipComponent.WYNNCRAFT_LANGUAGE_FONT)
            .withColor(ChatFormatting.WHITE);
    private static final FontDescription TOOLTIP_BANNER_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/banner"));

    public Component buildRerollBanner(GearTier gearTier, GearInstance gearInstance) {
        if (gearInstance == null) {
            return Component.empty();
        }

        int rerolls = Math.max(0, gearInstance.rerolls());
        MutableComponent rerollBanner = Component.empty().withStyle(WYNNCRAFT_WHITE_STYLE);

        rerollBanner.append(BannerBoxFont.buildMessage(
                String.valueOf(rerolls),
                GearTooltipSupport.getDividerColor(gearTier),
                CommonColors.BLACK,
                "\uDB00\uDC02"));

        rerollBanner.append(Component.literal("\uDAFF\uDFFF").withStyle(IdentifiableTooltipComponent.SPACING_STYLE));
        rerollBanner.append(GearTooltipSupport.withWhiteShadow(Component.literal("\uE005")
                .withStyle(Style.EMPTY
                        .withFont(TOOLTIP_BANNER_FONT)
                        .withColor(GearTooltipSupport.getDividerColor(gearTier).asInt()))));
        rerollBanner.append(GearTooltipSupport.withWhiteShadow(
                Component.literal("\uDAFF\uDFF6\uF005").withStyle(Style.EMPTY.withFont(TOOLTIP_BANNER_FONT))));

        return rerollBanner;
    }
}
