/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip;

import com.wynntils.core.components.Handler;
import com.wynntils.handlers.tooltip.impl.gear.GearTooltipComponent;
import com.wynntils.handlers.tooltip.type.TooltipComponent;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.item.ItemStack;

public class TooltipHandler extends Handler {
    private final Map<Class<? extends WynnItem>, TooltipComponent> tooltipComponents = new HashMap<>();

    public TooltipHandler() {
        registerTooltipComponents();
    }

    /**
     * Creates a tooltip builder that provides a synthetic header and footer
     */
    public TooltipBuilder buildNew(IdentifiableItemProperty identifiableItem, boolean hideUnidentified) {
        TooltipComponent tooltipComponent = tooltipComponents.get(identifiableItem.getClass());
        if (tooltipComponent == null) {
            throw new IllegalArgumentException("No tooltip component registered for "
                    + identifiableItem.getClass().getName());
        }

        return TooltipBuilder.buildNewItem(identifiableItem, tooltipComponent, hideUnidentified);
    }

    /**
     * Creates a tooltip builder that parses the header and footer from an existing tooltip
     */
    public TooltipBuilder fromParsedItemStack(ItemStack itemStack, IdentifiableItemProperty itemInfo) {
        return TooltipBuilder.fromParsedItemStack(itemStack, itemInfo);
    }

    private void registerTooltipComponents() {
        registerTooltipComponent(GearItem.class, new GearTooltipComponent());
    }

    private void registerTooltipComponent(Class<? extends WynnItem> itemClass, TooltipComponent tooltipComponent) {
        tooltipComponents.put(itemClass, tooltipComponent);
    }
}
