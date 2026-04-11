/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.crafted;

import com.wynntils.models.items.properties.CraftedItemProperty;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public abstract class CraftedTooltipComponent<T extends CraftedItemProperty> {
    public record TooltipParts(List<Component> header, List<Component> footer) {}

    public abstract List<Component> buildHeaderTooltip(T craftedItem);

    public abstract List<Component> buildFooterTooltip(T craftedItem);

    public TooltipParts buildTooltipParts(ItemStack itemStack, T craftedItem) {
        return null;
    }

    public List<Component> finalizeTooltipLines(List<Component> tooltip, int targetWidth, T craftedItem) {
        return tooltip;
    }

    protected MutableComponent buildRequirementLine(String requirementName, boolean fulfilled) {
        MutableComponent requirement;

        requirement = fulfilled
                ? Component.literal("✔ ").withStyle(ChatFormatting.GREEN)
                : Component.literal("✖ ").withStyle(ChatFormatting.RED);
        requirement.append(Component.literal(requirementName).withStyle(ChatFormatting.GRAY));
        return requirement;
    }
}
