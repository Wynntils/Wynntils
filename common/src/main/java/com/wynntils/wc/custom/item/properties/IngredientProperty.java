/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item.properties;

import com.wynntils.features.user.inventory.ItemHighlightFeature;
import com.wynntils.utils.objects.CustomColor;
import com.wynntils.wc.custom.item.WynnItemStack;
import com.wynntils.wc.custom.item.properties.type.HighlightProperty;
import com.wynntils.wc.utils.WynnUtils;
import net.minecraft.ChatFormatting;

public class IngredientProperty extends ItemProperty implements HighlightProperty {

    private final IngredientTier tier;

    public IngredientProperty(WynnItemStack item) {
        super(item);

        String name = WynnUtils.normalizeBadString(item.getHoverName().getString());

        tier = calculateTier(name);
    }

    private IngredientTier calculateTier(String name) {
        if (name.endsWith(ChatFormatting.GOLD + " [" + ChatFormatting.YELLOW + "✫" + ChatFormatting.DARK_GRAY + "✫✫"
                + ChatFormatting.GOLD + "]")) {
            return IngredientTier.ONE;
        } else if (name.endsWith(ChatFormatting.DARK_PURPLE + " [" + ChatFormatting.LIGHT_PURPLE + "✫✫"
                + ChatFormatting.DARK_GRAY + "✫" + ChatFormatting.DARK_PURPLE + "]")) {
            return IngredientTier.TWO;
        } else if (name.endsWith(
                ChatFormatting.DARK_AQUA + " [" + ChatFormatting.AQUA + "✫✫✫" + ChatFormatting.DARK_AQUA + "]")) {
            return IngredientTier.THREE;
        } else {
            return IngredientTier.ZERO;
        }
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

    @Override
    public boolean isInventoryHighlight() {
        return true;
    }

    @Override
    public boolean isHotbarHighlight() {
        return false;
    }

    public enum IngredientTier {
        ZERO,
        ONE,
        TWO,
        THREE
    }
}
