/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.builders;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.models.aspects.type.AspectInfo;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class AspectTooltipBuilder extends TooltipBuilder {
    private final AspectInfo aspectInfo;
    private final int tier;

    private AspectTooltipBuilder(AspectInfo aspectInfo, int tier, ItemSourceType sourceType, String source) {
        super(sourceType, source);

        this.aspectInfo = aspectInfo;
        this.tier = tier;
    }

    public static AspectTooltipBuilder buildNewItem(
            AspectInfo aspectInfo, int tier, ItemSourceType sourceType, String source) {
        return new AspectTooltipBuilder(aspectInfo, tier, sourceType, source);
    }

    @Override
    protected void buildTooltip() {
        List<TooltipElement> lines = new ArrayList<>();

        Component aspectName = Component.literal(aspectInfo.name())
                .withStyle(aspectInfo.gearTier().getChatFormatting());

        lines.add(TooltipLine.left(aspectName));
        lines.add(TooltipLine.empty());

        // The threshold in the API is cumulative so we need to subtract the previous tiers threshold
        int threshold = aspectInfo.effects().get(tier - 1).a();
        if (tier > 1) {
            threshold -= aspectInfo.effects().get(tier - 2).a();
        }

        MutableComponent tierLine;
        if (tier == aspectInfo.effects().size()) {
            // Max tier
            tierLine = Component.literal("Tier " + MathUtils.toRoman(tier))
                    .withStyle(aspectInfo.gearTier().getChatFormatting())
                    .append(Component.literal(" [" + threshold + "] [MAX]").withStyle(ChatFormatting.GRAY));
        } else {
            // Any other tier
            tierLine = Component.literal("Tier " + MathUtils.toRoman(tier))
                    .withStyle(aspectInfo.gearTier().getChatFormatting())
                    .append(Component.literal(" / ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("Tier " + MathUtils.toRoman(tier + 1))
                            .withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(" [" + threshold + "]").withStyle(ChatFormatting.GRAY));
        }

        lines.add(TooltipLine.center(tierLine));
        lines.add(TooltipLine.empty());

        aspectInfo.effects().get(tier - 1).b().forEach(line -> lines.add(TooltipLine.left(line.getComponent())));

        lines.add(TooltipLine.empty());

        MutableComponent classLine = Models.Character.getClassType() == aspectInfo.classType()
                ? Component.literal("✔ ").withStyle(ChatFormatting.GREEN)
                : Component.literal("✖ ").withStyle(ChatFormatting.RED);
        classLine
                .append(Component.literal("Class Req: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(aspectInfo.classType().getFullName()).withStyle(ChatFormatting.WHITE));

        lines.add(TooltipLine.left(classLine));
        lines.add(TooltipLine.empty());

        Component rarityLine = BannerBoxFont.buildMessage(
                aspectInfo.gearTier().getName() + " aspect",
                CustomColor.fromChatFormatting(aspectInfo.gearTier().getChatFormatting()),
                CommonColors.BLACK,
                "");

        lines.add(TooltipLine.left(rarityLine));

        this.tooltipLines = Handlers.Tooltip.alignTooltip(lines);
    }
}
