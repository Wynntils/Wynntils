/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.aspect;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.models.aspects.type.AspectInfo;
import com.wynntils.models.items.items.game.AspectItem;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.utils.MathUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public class GuideAspectItemStack extends GuideItemStack {
    private static final MutableComponent LEGENDARY_TAG = Component.literal(
            "\uE060\uDAFF\uDFFF\uE03B\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE036\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE03D\uDAFF\uDFFF\uE033\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE048\uDAFF\uDFFF\uE061\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE042\uDAFF\uDFFF\uE03F\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE032\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE062\uDAFF\uDFA0§0\uE00B\uE004\uE006\uE004\uE00D\uE003\uE000\uE011\uE018 \uE000\uE012\uE00F\uE004\uE002\uE013\uDB00\uDC02");
    private static final MutableComponent FABLED_TAG = Component.literal(
            "\uE060\uDAFF\uDFFF\uE035\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE031\uDAFF\uDFFF\uE03B\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE033\uDAFF\uDFFF\uE061\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE042\uDAFF\uDFFF\uE03F\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE032\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE062\uDAFF\uDFB2§0\uE005\uE000\uE001\uE00B\uE004\uE003 \uE000\uE012\uE00F\uE004\uE002\uE013\uDB00\uDC02");
    private static final MutableComponent MYTHIC_TAG = Component.literal(
            "\uE060\uDAFF\uDFFF\uE03C\uDAFF\uDFFF\uE048\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE037\uDAFF\uDFFF\uE038\uDAFF\uDFFF\uE032\uDAFF\uDFFF\uE061\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE042\uDAFF\uDFFF\uE03F\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE032\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE062\uDAFF\uDFB4§0\uE00C\uE018\uE013\uE007\uE008\uE002 \uE000\uE012\uE00F\uE004\uE002\uE013\uDB00\uDC02");
    private static final ResourceLocation RARITY_TAG_FONT = ResourceLocation.withDefaultNamespace("banner/box");

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

        // The threshold in the API is cumulative so we need to subtract the previous tiers threshold
        int threshold = aspectInfo.effects().get(tier - 1).a();
        if (tier > 1) {
            threshold -= aspectInfo.effects().get(tier - 2).a();
        }

        MutableComponent tierLine;
        if (tier == aspectInfo.effects().size()) {
            // Max tier
            tierLine = Component.literal("           Tier " + MathUtils.toRoman(tier))
                    .withStyle(aspectInfo.gearTier().getChatFormatting())
                    .append(Component.literal(" [" + threshold + "] [MAX]").withStyle(ChatFormatting.GRAY));
        } else {
            // Any other tier
            tierLine = Component.literal("     Tier " + MathUtils.toRoman(tier))
                    .withStyle(aspectInfo.gearTier().getChatFormatting())
                    .append(Component.literal(" / ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("Tier " + MathUtils.toRoman(tier + 1))
                            .withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(" [" + threshold + "]").withStyle(ChatFormatting.GRAY));
        }
        this.generatedTooltip.add(tierLine);
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
        Style rarityStyle =
                Style.EMPTY.withColor(aspectInfo.gearTier().getChatFormatting()).withFont(RARITY_TAG_FONT);
        Component rarityLine = getRarityTag().withStyle(rarityStyle);

        this.generatedTooltip.add(rarityLine);
    }

    public AspectInfo getAspectInfo() {
        return aspectInfo;
    }

    public int getTier() {
        return tier;
    }

    // TODO: We should be able to generate these instead of hardcoding them
    private MutableComponent getRarityTag() {
        return switch (aspectInfo.gearTier()) {
            case MYTHIC -> MYTHIC_TAG;
            case FABLED -> FABLED_TAG;
            default -> LEGENDARY_TAG;
        };
    }
}
