/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.ingredientinfo;

import com.wynntils.core.components.Model;
import com.wynntils.models.stats.StatModel;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;

public class IngredientModel extends Model {
    private static final Map<ChatFormatting, Integer> TIER_COLOR_CODES = Map.of(
            ChatFormatting.DARK_GRAY, 0,
            ChatFormatting.YELLOW, 1,
            ChatFormatting.LIGHT_PURPLE, 2,
            ChatFormatting.AQUA, 3);
    private final IngredientInfoRegistry ingredientInfoRegistry = new IngredientInfoRegistry();

    public IngredientModel(StatModel statModel) {
        super(List.of(statModel));
    }

    public int getTierFromColorCode(String tierColor) {
        return TIER_COLOR_CODES.getOrDefault(ChatFormatting.getByCode(tierColor.charAt(0)), 0);
    }

    public IngredientInfo getIngredientInfoFromName(String ingredientName) {
        return ingredientInfoRegistry.getFromDisplayName(ingredientName);
    }

    public Stream<IngredientInfo> getAllIngredientInfos() {
        return ingredientInfoRegistry.getIngredientInfoStream();
    }

    public void reloadData() {
        ingredientInfoRegistry.reloadData();
    }
}
