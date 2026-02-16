/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.emerald;

import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.models.items.items.game.EmeraldItem;
import com.wynntils.screens.guides.GuideItemStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public class GuideEmeraldItemStack extends GuideItemStack {
    private final EmeraldUnits unit;

    public GuideEmeraldItemStack(EmeraldUnits unit) {
        super(unit.getItemStack(), new EmeraldItem(null, unit), unit.name());
        this.unit = unit;
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(getHoverName());
        tooltip.add(Component.empty());
        tooltip.add(generateLore());

        appendFavoriteInfo(tooltip);

        return tooltip;
    }

    @Override
    public Component getHoverName() {
        return Component.empty()
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))
                .append(Component.literal(unit.getDisplayName() + " (" + unit.getSymbol() + ")"));
    }

    public EmeraldUnits getEmeraldUnit() {
        return unit;
    }

    private Component generateLore() {
        return Component.literal("Equals to ")
                .append(Component.literal(unit.getMultiplier() + " " + EmeraldUnits.EMERALD.getSymbol())
                        .withStyle(ChatFormatting.BOLD));
    }

    public EmeraldUnits getUnit() {
        return unit;
    }
}
