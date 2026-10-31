/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.builders;

import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipWrappedText;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.rewards.type.AmplifierInfo;
import com.wynntils.utils.MathUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class AmplifierTooltipBuilder extends AugmentTooltipBuilder {
    private final AmplifierInfo amplifierInfo;

    private AmplifierTooltipBuilder(AmplifierInfo amplifierInfo, ItemSourceType sourceType, String source) {
        super(
                Component.literal("Corkian Amplifier " + MathUtils.toRoman(amplifierInfo.tier()))
                        .withStyle(GearTier.LEGENDARY.getChatFormatting()),
                GearTier.LEGENDARY,
                sourceType,
                source);

        this.amplifierInfo = amplifierInfo;
    }

    public static AmplifierTooltipBuilder buildNewItem(
            AmplifierInfo amplifierInfo, ItemSourceType sourceType, String source) {
        return new AmplifierTooltipBuilder(amplifierInfo, sourceType, source);
    }

    @Override
    protected List<TooltipElement> buildAugmentDetails() {
        List<TooltipElement> lines = new ArrayList<>();

        MutableComponent component = Component.translatable("screens.wynntils.wynntilsGuides.augments.amplifier1")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(amplifierInfo.effect() + "%").withStyle(ChatFormatting.WHITE))
                .append(Component.translatable("screens.wynntils.wynntilsGuides.augments.amplifier2")
                        .withStyle(ChatFormatting.GRAY));

        lines.add(TooltipWrappedText.left(component));

        if (sourceType == ItemSourceType.GUIDE) {
            MutableComponent obtainComponent = Component.translatable("screens.wynntils.wynntilsGuides.augments.obtain")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.translatable("screens.wynntils.wynntilsGuides.augments.obtainRaids")
                            .withStyle(ChatFormatting.AQUA));

            lines.add(TooltipLine.empty());
            lines.add(TooltipWrappedText.left(obtainComponent));
        }

        return lines;
    }
}
