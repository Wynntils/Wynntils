/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.crafted;

import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.Pair;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class CraftedTooltipBuilder extends TooltipBuilder {
    private final CraftedItemProperty craftedItem;

    private CraftedTooltipBuilder(CraftedItemProperty craftedItem, List<Component> header, List<Component> footer) {
        super(header, footer);
        this.craftedItem = craftedItem;
    }

    public static <T extends CraftedItemProperty> CraftedTooltipBuilder buildNewItem(
            T craftedItem, CraftedTooltipComponent<T> tooltipComponent) {
        List<Component> header = tooltipComponent.buildHeaderTooltip(craftedItem);
        List<Component> footer = tooltipComponent.buildFooterTooltip(craftedItem);
        return new CraftedTooltipBuilder(craftedItem, header, footer);
    }

    public static CraftedTooltipBuilder fromParsedItemStack(ItemStack itemStack, CraftedItemProperty craftedItem) {
        List<Component> tooltips = LoreUtils.getTooltipLines(itemStack);

        Pair<List<Component>, List<Component>> splitLore = extractHeaderAndFooter(tooltips);
        List<Component> header = splitLore.a();
        List<Component> footer = splitLore.b();

        return new CraftedTooltipBuilder(craftedItem, header, footer);
    }

    @Override
    protected List<Component> getIdentificationLines(
            ClassType currentClass, TooltipStyle style, TooltipIdentificationDecorator decorator) {
        return CraftedTooltipIdentifications.buildTooltip(craftedItem, currentClass, style);
    }
}
