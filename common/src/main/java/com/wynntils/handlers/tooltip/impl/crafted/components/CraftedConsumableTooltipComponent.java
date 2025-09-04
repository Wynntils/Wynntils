/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.crafted.components;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.impl.crafted.CraftedTooltipComponent;
import com.wynntils.models.items.items.game.CraftedConsumableItem;
import com.wynntils.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class CraftedConsumableTooltipComponent extends CraftedTooltipComponent<CraftedConsumableItem> {
    @Override
    public List<Component> buildHeaderTooltip(CraftedConsumableItem craftedItem) {
        List<Component> header = new ArrayList<>();

        // name
        header.add(Component.literal(craftedItem.getName())
                .withStyle(ChatFormatting.DARK_AQUA)
                .append(Component.literal(" [" + craftedItem.getUses().current() + "/"
                                + craftedItem.getUses().max() + "]")
                        .withStyle(ChatFormatting.AQUA)));

        // Effects
        if (!craftedItem.getNamedEffects().isEmpty()) {
            header.add(Component.literal("Effect:").withStyle(ChatFormatting.GREEN));
            craftedItem
                    .getNamedEffects()
                    .forEach(effect -> header.add(Component.literal("- ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(
                                            StringUtils.capitalizeFirst(
                                                            effect.type().name().toLowerCase(Locale.ROOT)) + ": ")
                                    .withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(String.valueOf(effect.value()))
                                    .withStyle(ChatFormatting.WHITE)
                                    .append(Component.literal(
                                            " " + effect.type().getSuffix())))));
            header.add(Component.literal(""));
        }

        // requirements
        int level = craftedItem.getLevel();
        if (level != 0) {
            boolean fulfilled = Models.CombatXp.getCombatLevel().current() >= level;
            header.add(buildRequirementLine("Combat Lv. Min: " + level, fulfilled));
            header.add(Component.literal(""));
        }

        return header;
    }

    @Override
    public List<Component> buildFooterTooltip(CraftedConsumableItem craftedItem) {
        List<Component> footer = new ArrayList<>();

        footer.add(Component.empty());

        // item type
        footer.add(Component.literal("Crafted ")
                .withStyle(ChatFormatting.DARK_AQUA)
                .append(Component.literal(StringUtils.capitalizeFirst(
                        craftedItem.getConsumableType().name().toLowerCase(Locale.ROOT)))));

        return footer;
    }
}
