/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.aspect;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.models.aspects.type.AspectInfo;
import com.wynntils.models.items.items.game.AspectItem;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public class GuideAspectItemStack extends GuideItemStack {
    private final AspectInfo aspectInfo;
    private final int tier;
    protected List<Component> generatedTooltip;

    public GuideAspectItemStack(AspectInfo aspectInfo, int tier) {
        super(aspectInfo.itemMaterial().itemStack(), new AspectItem(aspectInfo, tier), aspectInfo.name());

        this.aspectInfo = aspectInfo;
        this.tier = tier;

        buildTooltip();
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag) {
        List<Component> tooltipLines = new ArrayList<>(generatedTooltip);

        return tooltipLines;
    }

    public void buildTooltip() {
        this.generatedTooltip = new ArrayList<>();
        this.generatedTooltip.add(Component.literal(aspectInfo.name())
                .withStyle(aspectInfo.gearTier().getChatFormatting()));
        this.generatedTooltip.add(Component.empty());

        this.generatedTooltip.add(Component.empty());

        aspectInfo.effects().get(tier - 1).b().forEach(line -> this.generatedTooltip.add(line.getComponent()));

        this.generatedTooltip.add(Component.empty());
        MutableComponent classLine = Models.Character.getClassType() == aspectInfo.classType()
                ? Component.literal("✔ ").withStyle(ChatFormatting.GREEN)
                : Component.literal("✖ ").withStyle(ChatFormatting.RED);
        classLine
                .append(Component.literal("Class Req: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(aspectInfo.classType().getFullName()).withStyle(ChatFormatting.WHITE));

        this.generatedTooltip.add(classLine);
        this.generatedTooltip.add(Component.empty());
        Component rarityLine = BannerBoxFont.buildMessage(
                aspectInfo.gearTier().getName() + " aspect",
                CustomColor.fromChatFormatting(aspectInfo.gearTier().getChatFormatting()),
                CommonColors.BLACK,
                "");

        this.generatedTooltip.add(rarityLine);

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
        int widestLine = generatedTooltip.stream()
                .mapToInt(line -> McUtils.mc().font.width(line))
                .max()
                .orElse(0);
        int currentWidth = McUtils.mc().font.width(tierLine);
        int target = currentWidth + ((widestLine - currentWidth) / 2);
        String spacing = Managers.Font.calculateOffset(currentWidth, target);

        Component centeredTierLine = Component.empty()
                .append(Component.literal(spacing).withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(tierLine);

        this.generatedTooltip.add(2, centeredTierLine);
    }

    public AspectInfo getAspectInfo() {
        return aspectInfo;
    }

    public int getTier() {
        return tier;
    }
}
