/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.ingredientinfo;

import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;

public enum IngredientPosition {
    LEFT("to the left of"),
    RIGHT("to the right of"),
    ABOVE("above"),
    UNDER("under"),
    TOUCHING("touching"),
    NOT_TOUCHING("not touching", "notTouching");

    private final String displayName;
    private final String apiName;

    IngredientPosition(String displayName) {
        this.displayName = displayName;
        this.apiName = this.name().toLowerCase(Locale.ROOT);
    }

    IngredientPosition(String displayName, String apiName) {
        this.displayName = displayName;
        this.apiName = apiName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getApiName() {
        return apiName;
    }

    public List<String> getLore(int value) {
        String colorCode = value > 0 ? ChatFormatting.GREEN + "+" : ChatFormatting.RED.toString();
        return List.of(
                colorCode + value + "%" + ChatFormatting.GRAY + " Ingredient Effectiveness",
                ChatFormatting.GRAY + "(To ingredients " + this.displayName + " this one)");
    }
}
