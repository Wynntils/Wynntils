/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item.properties;

import com.wynntils.features.user.inventory.ItemHighlightFeature;
import com.wynntils.utils.objects.CustomColor;
import com.wynntils.wc.custom.item.WynnItemStack;
import net.minecraft.ChatFormatting;

public class IngredientProperty extends TieredCraftingItemProperty {
    public IngredientProperty(WynnItemStack item) {
        super(item);
    }

    @Override
    protected ChatFormatting getPrimaryColor(IngredientTier tier) {
        return switch (tier) {
            case ZERO -> null; // should not be used
            case ONE -> ChatFormatting.GOLD;
            case TWO -> ChatFormatting.DARK_PURPLE;
            case THREE -> ChatFormatting.DARK_AQUA;
        };
    }

    @Override
    protected ChatFormatting getSecondaryColor(IngredientTier tier) {
        return switch (tier) {
            case ZERO -> null; // should not be used
            case ONE -> ChatFormatting.YELLOW;
            case TWO -> ChatFormatting.LIGHT_PURPLE;
            case THREE -> ChatFormatting.AQUA;
        };
    }

    @Override
    public CustomColor getHighlightColor() {
        return switch (tier) {
            case ZERO -> ItemHighlightFeature.zeroStarIngredientHighlightColor;
            case ONE -> ItemHighlightFeature.oneStarIngredientHighlightColor;
            case TWO -> ItemHighlightFeature.twoStarIngredientHighlightColor;
            case THREE -> ItemHighlightFeature.threeStarIngredientHighlightColor;
        };
    }

    @Override
    public boolean isHighlightEnabled() {
        return switch (tier) {
            case ZERO -> ItemHighlightFeature.zeroStarIngredientHighlightEnabled;
            case ONE -> ItemHighlightFeature.oneStarIngredientHighlightEnabled;
            case TWO -> ItemHighlightFeature.twoStarIngredientHighlightEnabled;
            case THREE -> ItemHighlightFeature.threeStarIngredientHighlightEnabled;
        };
    }
}
