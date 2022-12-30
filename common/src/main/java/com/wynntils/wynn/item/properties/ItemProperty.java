/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.properties.type.HighlightProperty;
import com.wynntils.wynn.item.properties.type.TextOverlayProperty;

public abstract class ItemProperty {
    public static final Class<ConsumableChargeProperty> CONSUMABLE_CHARGE = ConsumableChargeProperty.class;
    public static final Class<CosmeticTierProperty> COSMETIC_TIER = CosmeticTierProperty.class;
    public static final Class<ItemTierProperty> ITEM_TIER = ItemTierProperty.class;
    public static final Class<IngredientProperty> INGREDIENT = IngredientProperty.class;
    public static final Class<MaterialProperty> MATERIAL = MaterialProperty.class;
    public static final Class<SkillPointProperty> SKILL_POINT = SkillPointProperty.class;
    public static final Class<SkillIconProperty> SKILL_ICON = SkillIconProperty.class;

    public static final Class<HighlightProperty> HIGHLIGHT = HighlightProperty.class;
    public static final Class<TextOverlayProperty> TEXT_OVERLAY = TextOverlayProperty.class;
    public static final Class<ServerCountProperty> SERVER_COUNT_PROPERTY = ServerCountProperty.class;

    public static final Class<SearchOverlayProperty> SEARCH_OVERLAY = SearchOverlayProperty.class;

    protected final WynnItemStack item;

    protected ItemProperty(WynnItemStack item) {
        this.item = item;

        // attach property to the itemstack
        item.addProperty(this);
    }
}
