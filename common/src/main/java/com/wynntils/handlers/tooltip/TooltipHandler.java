/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip;

import com.wynntils.core.components.Handler;
import com.wynntils.handlers.tooltip.impl.crafted.CraftedTooltipBuilder;
import com.wynntils.handlers.tooltip.impl.crafted.CraftedTooltipComponent;
import com.wynntils.handlers.tooltip.impl.crafted.components.CraftedConsumableTooltipComponent;
import com.wynntils.handlers.tooltip.impl.crafted.components.CraftedGearTooltipComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipBuilder;
import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.components.CharmTooltipComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.components.GearTooltipComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.components.TomeTooltipComponent;
import com.wynntils.models.items.items.game.CharmItem;
import com.wynntils.models.items.items.game.CraftedConsumableItem;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.item.ItemStack;

public final class TooltipHandler extends Handler {
    private final Map<Class<? extends IdentifiableItemProperty>, IdentifiableTooltipComponent>
            identifiableTooltipComponents = new HashMap<>();
    private final Map<Class<? extends CraftedItemProperty>, CraftedTooltipComponent> craftedTooltipComponents =
            new HashMap<>();

    public TooltipHandler() {
        registerTooltipComponents();
    }

    /**
     * Creates a tooltip builder that provides a synthetic header and footer with no source
     */
    public IdentifiableTooltipBuilder buildNew(
            IdentifiableItemProperty identifiableItem, boolean hideUnidentified, boolean showItemType) {
        return buildNew(identifiableItem, hideUnidentified, showItemType, "");
    }

    /**
     * Creates a tooltip builder that provides a synthetic header and footer with the given source
     */
    public IdentifiableTooltipBuilder buildNew(
            IdentifiableItemProperty identifiableItem, boolean hideUnidentified, boolean showItemType, String source) {
        IdentifiableTooltipComponent tooltipComponent = identifiableTooltipComponents.get(identifiableItem.getClass());
        if (tooltipComponent == null) {
            throw new IllegalArgumentException("No tooltip component registered for "
                    + identifiableItem.getClass().getName());
        }

        return IdentifiableTooltipBuilder.buildNewItem(
                identifiableItem, tooltipComponent, hideUnidentified, showItemType, source);
    }

    /**
     * Creates a tooltip builder that provides a synthetic header and footer with the given source
     */
    public CraftedTooltipBuilder buildNew(CraftedItemProperty craftedItemProperty, String source) {
        CraftedTooltipComponent tooltipComponent = craftedTooltipComponents.get(craftedItemProperty.getClass());
        if (tooltipComponent == null) {
            throw new IllegalArgumentException("No tooltip component registered for "
                    + craftedItemProperty.getClass().getName());
        }

        return CraftedTooltipBuilder.buildNewItem(craftedItemProperty, tooltipComponent, source);
    }

    /**
     * Creates a tooltip builder that parses the header and footer from an existing tooltip
     */
    public TooltipBuilder fromParsedItemStack(ItemStack itemStack, IdentifiableItemProperty itemInfo) {
        return IdentifiableTooltipBuilder.fromParsedItemStack(itemStack, itemInfo);
    }

    /**
     * Creates a tooltip builder that parses the header and footer from an existing tooltip
     */
    public TooltipBuilder fromParsedItemStack(ItemStack itemStack, CraftedItemProperty craftedItemProperty) {
        return CraftedTooltipBuilder.fromParsedItemStack(itemStack, craftedItemProperty);
    }

    private void registerTooltipComponents() {
        registerTooltipComponent(CharmItem.class, new CharmTooltipComponent());
        registerTooltipComponent(GearItem.class, new GearTooltipComponent());
        registerTooltipComponent(TomeItem.class, new TomeTooltipComponent());

        registerTooltipComponent(CraftedGearItem.class, new CraftedGearTooltipComponent());
        registerTooltipComponent(CraftedConsumableItem.class, new CraftedConsumableTooltipComponent());
    }

    private <T, U, I extends IdentifiableItemProperty<T, U>> void registerTooltipComponent(
            Class<I> itemClass, IdentifiableTooltipComponent<T, U> tooltipComponent) {
        identifiableTooltipComponents.put(itemClass, tooltipComponent);
    }

    private <T extends CraftedItemProperty> void registerTooltipComponent(
            Class<T> itemClass, CraftedTooltipComponent<T> tooltipComponent) {
        craftedTooltipComponents.put(itemClass, tooltipComponent);
    }
}
