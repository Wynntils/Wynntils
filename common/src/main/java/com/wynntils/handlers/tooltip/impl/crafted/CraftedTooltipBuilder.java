/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.crafted;

import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.Pair;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class CraftedTooltipBuilder extends TooltipBuilder {
    private CraftedTooltipBuilder(List<Component> header, List<Component> footer, String source) {
        super(header, footer, source);
    }

    public static CraftedTooltipBuilder buildNewItem(CraftedItemProperty craftedItem, String source) {
        return new CraftedTooltipBuilder(List.of(), List.of(), source);
    }

    public static CraftedTooltipBuilder fromParsedItemStack(ItemStack itemStack, CraftedItemProperty craftedItem) {
        List<Component> tooltips = LoreUtils.getTooltipLines(itemStack);
        Pair<List<Component>, List<Component>> splitLore = extractHeaderAndFooter(tooltips);
        return new CraftedTooltipBuilder(splitLore.a(), splitLore.b(), "");
    }
}
