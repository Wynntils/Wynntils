/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item.properties;

import com.wynntils.wc.custom.item.WynnItemStack;
import com.wynntils.wc.custom.item.properties.type.HighlightProperty;
import com.wynntils.wc.custom.item.properties.type.TextOverlayProperty;

public abstract class ItemProperty {
    public static final Class<AmplifierTierProperty> AMPLIFIER_TIER = AmplifierTierProperty.class;
    public static final Class<ConsumableChargeProperty> CONSUMABLE_CHARGE = ConsumableChargeProperty.class;
    public static final Class<CosmeticTierProperty> COSMETIC_TIER = CosmeticTierProperty.class;
    public static final Class<DungeonKeyProperty> DUNGEON_KEY = DungeonKeyProperty.class;
    public static final Class<DurabilityProperty> DURABILITY = DurabilityProperty.class;
    public static final Class<ItemTierProperty> ITEM_TIER = ItemTierProperty.class;
    public static final Class<IngredientProperty> INGREDIENT = IngredientProperty.class;
    public static final Class<TeleportScrollProperty> TELEPORT_SCROLL = TeleportScrollProperty.class;

    public static final Class<HighlightProperty> HIGHLIGHT = HighlightProperty.class;
    public static final Class<TextOverlayProperty> TEXT_OVERLAY = TextOverlayProperty.class;

    protected final WynnItemStack item;

    protected ItemProperty(WynnItemStack item) {
        this.item = item;

        // attach property to the itemstack
        item.addProperty(this);
    }
}
