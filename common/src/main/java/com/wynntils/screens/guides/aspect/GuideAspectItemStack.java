/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.aspect;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.aspects.type.AspectInfo;
import com.wynntils.models.items.items.game.AspectItem;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.utils.MathUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public class GuideAspectItemStack extends GuideItemStack {
    private final AspectInfo aspectInfo;
    private final MutableComponent name;
    private final int tier;
    private List<Component> generatedTooltip;

    public GuideAspectItemStack(AspectInfo aspectInfo, int tier) {
        super(aspectInfo.itemMaterial().itemStack(), new AspectItem(aspectInfo, tier), aspectInfo.name());

        this.aspectInfo = aspectInfo;
        this.name = Component.literal(aspectInfo.name())
                .withStyle(aspectInfo.gearTier().getChatFormatting());
        this.tier = tier;

        buildTooltip();
    }

    @Override
    public Component getHoverName() {
        return name;
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag) {
        List<Component> tooltipLines = new ArrayList<>(generatedTooltip);

        tooltipLines.add(Component.empty());
        if (Services.Favorites.isFavorite(this)) {
            tooltipLines.add(Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.unfavorite")
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltipLines.add(Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.favorite")
                    .withStyle(ChatFormatting.GREEN));
        }

        return tooltipLines;
    }

    public void buildTooltip() {
        this.generatedTooltip = new ArrayList<>();
        this.generatedTooltip.add(Component.literal(aspectInfo.name())
                .withStyle(aspectInfo.gearTier().getChatFormatting()));
        this.generatedTooltip.add(Component.empty());

        // FIXME: Center tier line
        MutableComponent tierLine;
        if (tier == aspectInfo.effects().size()) {
            // Max tier
            tierLine = Component.literal("           Tier " + MathUtils.toRoman(tier))
                    .withStyle(aspectInfo.gearTier().getChatFormatting())
                    .append(Component.literal(" [MAX]").withStyle(ChatFormatting.GRAY));
        } else {
            // Any other tier
            tierLine = Component.literal("     Tier " + MathUtils.toRoman(tier))
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(" / ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("Tier " + MathUtils.toRoman(tier + 1))
                            .withStyle(aspectInfo.gearTier().getChatFormatting()))
                    .append(Component.literal(
                                    " [" + aspectInfo.effects().get(tier).a() + "]")
                            .withStyle(ChatFormatting.GRAY));
        }
        this.generatedTooltip.add(tierLine);

        aspectInfo.effects().get(tier - 1).b().forEach(line -> this.generatedTooltip.add(line.getComponent()));

        this.generatedTooltip.add(Component.empty());
        MutableComponent classLine = Models.Character.getClassType() == aspectInfo.classType()
                ? Component.literal("✔ ").withStyle(ChatFormatting.GREEN)
                : Component.literal("✖ ").withStyle(ChatFormatting.RED);
        classLine
                .append(Component.literal("Class Req: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(aspectInfo.classType().getFullName()).withStyle(ChatFormatting.WHITE));

        this.generatedTooltip.add(classLine);

        // TODO: Add rarity tag line
    }

    public AspectInfo getAspectInfo() {
        return aspectInfo;
    }

    public int getTier() {
        return tier;
    }
}
