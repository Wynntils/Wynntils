/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.crafted;

import com.wynntils.models.items.properties.CraftedItemProperty;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class CraftedTooltipComponent<T extends CraftedItemProperty> {
    public abstract List<Component> buildHeaderTooltip(T craftedItem);

    public abstract List<Component> buildFooterTooltip(T craftedItem);

    protected MutableComponent buildRequirementLine(String requirementName, boolean fulfilled) {
        MutableComponent requirement;

        requirement = fulfilled
                ? Component.literal("✔ ").withStyle(ChatFormatting.GREEN)
                : Component.literal("✖ ").withStyle(ChatFormatting.RED);
        requirement.append(Component.literal(requirementName).withStyle(ChatFormatting.GRAY));
        return requirement;
    }
}
