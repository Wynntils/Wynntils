/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.features.user.inventory.ItemHighlightFeature;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.wynn.item.WynnItemStack;
import net.minecraft.ChatFormatting;

public class IngredientProperty extends TieredCraftingItemProperty {
    public IngredientProperty(WynnItemStack item) {
        super(item);
    }

    @Override
    protected ChatFormatting getPrimaryParsingColor(IngredientTier tier) {
        return switch (tier) {
            case ZERO -> null; // should not be used
            case ONE -> ChatFormatting.GOLD;
            case TWO -> ChatFormatting.DARK_PURPLE;
            case THREE -> ChatFormatting.DARK_AQUA;
        };
    }

    @Override
    protected ChatFormatting getSecondaryParsingColor(IngredientTier tier) {
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
            case ZERO -> ItemHighlightFeature.INSTANCE.zeroStarIngredientHighlightColor;
            case ONE -> ItemHighlightFeature.INSTANCE.oneStarIngredientHighlightColor;
            case TWO -> ItemHighlightFeature.INSTANCE.twoStarIngredientHighlightColor;
            case THREE -> ItemHighlightFeature.INSTANCE.threeStarIngredientHighlightColor;
        };
    }

    @Override
    public boolean isHighlightEnabled() {
        return switch (tier) {
            case ZERO -> ItemHighlightFeature.INSTANCE.zeroStarIngredientHighlightEnabled;
            case ONE -> ItemHighlightFeature.INSTANCE.oneStarIngredientHighlightEnabled;
            case TWO -> ItemHighlightFeature.INSTANCE.twoStarIngredientHighlightEnabled;
            case THREE -> ItemHighlightFeature.INSTANCE.threeStarIngredientHighlightEnabled;
        };
    }
}
