/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.ingredients.type;

import com.google.gson.annotations.SerializedName;
import com.wynntils.mc.objects.CustomColor;
import net.minecraft.ChatFormatting;

public enum IngredientTier {
    @SerializedName("0")
    TIER_0(
            0,
            ChatFormatting.GRAY + "[" + ChatFormatting.DARK_GRAY + "✫✫✫" + ChatFormatting.GRAY + "]",
            new CustomColor(102, 102, 102)),
    @SerializedName("1")
    TIER_1(
            1,
            ChatFormatting.GOLD + "[" + ChatFormatting.YELLOW + "✫" + ChatFormatting.DARK_GRAY + "✫✫"
                    + ChatFormatting.GOLD + "]",
            new CustomColor(255, 247, 153)),
    @SerializedName("2")
    TIER_2(
            2,
            ChatFormatting.DARK_PURPLE + "[" + ChatFormatting.LIGHT_PURPLE + "✫✫" + ChatFormatting.DARK_GRAY + "✫"
                    + ChatFormatting.DARK_PURPLE + "]",
            new CustomColor(255, 255, 0)),
    @SerializedName("3")
    TIER_3(
            3,
            ChatFormatting.DARK_AQUA + "[" + ChatFormatting.AQUA + "✫✫✫" + ChatFormatting.DARK_AQUA + "]",
            new CustomColor(230, 77, 0));

    private final int tierInt;
    private final String tierString;
    private final CustomColor highlightColor;

    IngredientTier(int tierInt, String tierString, CustomColor highlightColor) {
        this.tierInt = tierInt;
        this.tierString = tierString;
        this.highlightColor = highlightColor;
    }

    public int getTierInt() {
        return tierInt;
    }

    public String getTierString() {
        return tierString;
    }

    public CustomColor getHighlightColor() {
        return highlightColor;
    }
}
