/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi.profiles.ingredient;

import com.google.gson.annotations.SerializedName;
import net.minecraft.ChatFormatting;

public enum IngredientTier {
    @SerializedName("0")
    TIER_0(0, ChatFormatting.GRAY + "[" + ChatFormatting.DARK_GRAY + "✫✫✫" + ChatFormatting.GRAY + "]"),
    @SerializedName("1")
    TIER_1(
            1,
            ChatFormatting.GOLD + "[" + ChatFormatting.YELLOW + "✫" + ChatFormatting.DARK_GRAY + "✫✫"
                    + ChatFormatting.GOLD + "]"),
    @SerializedName("2")
    TIER_2(
            2,
            ChatFormatting.DARK_PURPLE + "[" + ChatFormatting.LIGHT_PURPLE + "✫✫" + ChatFormatting.DARK_GRAY + "✫"
                    + ChatFormatting.DARK_PURPLE + "]"),
    @SerializedName("3")
    TIER_3(3, ChatFormatting.DARK_AQUA + "[" + ChatFormatting.AQUA + "✫✫✫" + ChatFormatting.DARK_AQUA + "]");

    private final int tierInt;
    private final String tierString;

    IngredientTier(int tierInt, String tierString) {
        this.tierInt = tierInt;
        this.tierString = tierString;
    }

    public int getTierInt() {
        return tierInt;
    }

    public String getTierString() {
        return tierString;
    }
}
