/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.emerald;

import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.models.items.items.game.EmeraldItem;
import com.wynntils.screens.guides.GuideItemStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

public class GuideEmeraldItemStack extends GuideItemStack {
    private final EmeraldUnits unit;

    private static final String EMERALD_TEXT = "\uE004\uDB00\uDC02";
    private static final String EMERALD_BLOCK_TEXT = "\uE004\uE001\uDB00\uDC02";
    private static final String LIQUID_TEXT = "\uE00B\uE004\uDB00\uDC02";

    private static final FontDescription BANNER_TAG_FONT = new FontDescription.Resource(Identifier.withDefaultNamespace("banner/box"));
    private static final Style BANNER_STYLE = Style.EMPTY.withFont(BANNER_TAG_FONT);

    public GuideEmeraldItemStack(EmeraldUnits unit) {
        super(unit.getItemStack(), new EmeraldItem(null, unit), unit.name());
        this.unit = unit;
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(getHoverName());
        tooltip.add(Component.empty());
        tooltip.addAll(generateLore());

        appendFavoriteInfo(tooltip);

        return tooltip;
    }

    @Override
    public Component getHoverName() {
        return Component.empty()
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))
                .append(Component.literal(unit.getDisplayName()));
    }

    public EmeraldUnits getEmeraldUnit() {
        return unit;
    }

    private List<Component> generateLore() {
        List<Component> itemLore = new ArrayList<>();
        itemLore.add(getBanner());
        itemLore.add(Component.empty());
        itemLore.add(Component.literal("Equals to ")
                .append(Component.literal(unit.getMultiplier() + " " + EmeraldUnits.EMERALD.getSymbol())
                        .withStyle(ChatFormatting.BOLD)));

        return itemLore;
    }

    private Component getBanner() {
        return switch (unit) {
            case EMERALD_BLOCK -> buildBanner(EMERALD_BLOCK_TEXT);
            case LIQUID_EMERALD -> buildBanner(LIQUID_TEXT);
            default -> buildBanner(EMERALD_TEXT);
        };
    }

    private Component buildBanner(String text) {
        return Component.empty().withStyle(style -> style.withColor(ChatFormatting.DARK_GREEN).withItalic(false))
                .append(Component.literal("\uE060\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE031\uDAFF\uDFFF\uE062\uDAFF\uDFF2")
                        .withStyle(BANNER_STYLE)
                        .append(Component.literal(text)
                                .withStyle(style -> style.withColor(ChatFormatting.WHITE).withShadowColor(16777215))))
                .append(Component.empty().withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal("\uE060\uDAFF\uDFFF\uE032\uDAFF\uDFFF\uE044\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE03D\uDAFF\uDFFF\uE032\uDAFF\uDFFF\uE048\uDAFF\uDFFF\uE062\uDAFF\uDFCE")
                                .withStyle(BANNER_STYLE)
                                .append(Component.literal("\uE002\uE014\uE011\uE011\uE004\uE00D\uE002\uE018\uDB00\uDC02")
                                        .withStyle(style -> style.withColor(ChatFormatting.WHITE).withShadowColor(16777215)))));
    }
}
