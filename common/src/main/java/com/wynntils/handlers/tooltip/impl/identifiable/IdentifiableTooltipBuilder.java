/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable;

import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.Pair;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class IdentifiableTooltipBuilder<T, U> extends TooltipBuilder {
    private IdentifiableTooltipBuilder(List<Component> header, List<Component> footer, String source) {
        super(header, footer, source);
    }

    public static <T, U> IdentifiableTooltipBuilder<T, U> buildNewItem(
            IdentifiableItemProperty<T, U> identifiableItem, String source) {
        return new IdentifiableTooltipBuilder<>(List.of(), List.of(), source);
    }

    public static <T, U> IdentifiableTooltipBuilder<T, U> buildFromItemStack(
            ItemStack itemStack, IdentifiableItemProperty<T, U> identifiableItem, String source) {
        List<Component> tooltips = LoreUtils.getTooltipLines(itemStack);
        Pair<List<Component>, List<Component>> splitLore = extractHeaderAndFooter(tooltips);
        return new IdentifiableTooltipBuilder<>(splitLore.a(), splitLore.b(), source);
    }

    public static IdentifiableTooltipBuilder fromParsedItemStack(
            ItemStack itemStack, IdentifiableItemProperty itemInfo) {
        List<Component> tooltips = LoreUtils.getTooltipLines(itemStack);
        Pair<List<Component>, List<Component>> splitLore = extractHeaderAndFooter(tooltips);
        return new IdentifiableTooltipBuilder(splitLore.a(), splitLore.b(), "");
    }
}
