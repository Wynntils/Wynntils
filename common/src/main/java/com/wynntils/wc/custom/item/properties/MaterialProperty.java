/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item.properties;

import com.wynntils.features.user.inventory.ItemHighlightFeature;
import com.wynntils.utils.objects.CustomColor;
import com.wynntils.wc.custom.item.WynnItemStack;
import net.minecraft.ChatFormatting;

public class MaterialProperty extends TieredCraftingItemProperty {
    public MaterialProperty(WynnItemStack item) {
        super(item);
    }

    @Override
    protected ChatFormatting getPrimaryColor(IngredientTier tier) {
        return ChatFormatting.GOLD;
    }

    @Override
    protected ChatFormatting getSecondaryColor(IngredientTier tier) {
        return ChatFormatting.YELLOW;
    }

    @Override
    public CustomColor getHighlightColor() {
        return switch (tier) {
            case ZERO -> CustomColor.NONE;
            case ONE -> ItemHighlightFeature.oneStarMaterialHighlightColor;
            case TWO -> ItemHighlightFeature.twoStarMaterialHighlightColor;
            case THREE -> ItemHighlightFeature.threeStarMaterialHighlightColor;
        };
    }

    @Override
    public boolean isHighlightEnabled() {
        return switch (tier) {
            case ZERO -> false; // should not happen
            case ONE -> ItemHighlightFeature.oneStarMaterialHighlightEnabled;
            case TWO -> ItemHighlightFeature.twoStarMaterialHighlightEnabled;
            case THREE -> ItemHighlightFeature.threeStarMaterialHighlightEnabled;
        };
    }
}
