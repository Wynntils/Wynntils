/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.ingredients.type;

import com.wynntils.utils.colors.CustomColor;
import net.minecraft.ChatFormatting;

public enum IngredientTierFormatting {
    TIER_0(
            ChatFormatting.GRAY + "[" + ChatFormatting.DARK_GRAY + "✫✫✫" + ChatFormatting.GRAY + "]",
            new CustomColor(102, 102, 102)),
    TIER_1(
            ChatFormatting.GOLD + "[" + ChatFormatting.YELLOW + "✫" + ChatFormatting.DARK_GRAY + "✫✫"
                    + ChatFormatting.GOLD + "]",
            new CustomColor(255, 247, 153)),
    TIER_2(
            ChatFormatting.DARK_PURPLE + "[" + ChatFormatting.LIGHT_PURPLE + "✫✫" + ChatFormatting.DARK_GRAY + "✫"
                    + ChatFormatting.DARK_PURPLE + "]",
            new CustomColor(255, 255, 0)),
    TIER_3(
            ChatFormatting.DARK_AQUA + "[" + ChatFormatting.AQUA + "✫✫✫" + ChatFormatting.DARK_AQUA + "]",
            new CustomColor(230, 77, 0));

    private final String tierString;
    private final CustomColor highlightColor;

    IngredientTierFormatting(String tierString, CustomColor highlightColor) {
        this.tierString = tierString;
        this.highlightColor = highlightColor;
    }

    public static IngredientTierFormatting fromTierNum(int tier) {
        if (tier >= IngredientTierFormatting.values().length) return null;

        return IngredientTierFormatting.values()[tier];
    }

    public int getTierInt() {
        return this.ordinal();
    }

    public String getTierString() {
        return tierString;
    }

    public CustomColor getHighlightColor() {
        return highlightColor;
    }
}
