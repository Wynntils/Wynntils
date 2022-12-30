/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.wynn.item.WynnItemStack;

public abstract class ItemProperty {
    public static final Class<CosmeticTierProperty> COSMETIC_TIER = CosmeticTierProperty.class;
    public static final Class<SkillPointProperty> SKILL_POINT = SkillPointProperty.class;

    public static final Class<ServerCountProperty> SERVER_COUNT_PROPERTY = ServerCountProperty.class;

    public static final Class<SearchOverlayProperty> SEARCH_OVERLAY = SearchOverlayProperty.class;

    protected final WynnItemStack item;

    protected ItemProperty(WynnItemStack item) {
        this.item = item;

        // attach property to the itemstack
        item.addProperty(this);
    }
}
