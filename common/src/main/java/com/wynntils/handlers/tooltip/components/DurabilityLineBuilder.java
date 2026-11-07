/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.components;

import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.fonts.wynnfonts.TooltipIdentificationMeterFont;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.type.CappedValue;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class DurabilityLineBuilder {
    public static TooltipElement buildDurabilityLine(GearTier tier, CappedValue durability) {
        MutableComponent durabilityComponent = Component.empty()
                .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT)
                .append(TooltipIdentificationMeterFont.buildCounterSingleLayerMeter(
                        durability,
                        tier.getSecondaryColor(),
                        CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY),
                        ""))
                .append(Component.literal(" Durability " + durability));

        return TooltipLine.left(durabilityComponent);
    }
}
