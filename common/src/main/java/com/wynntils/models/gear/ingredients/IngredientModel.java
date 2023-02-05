/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.ingredients;

import com.wynntils.core.components.Model;
import com.wynntils.models.ingredients.type.IdentificationModifier;
import com.wynntils.models.stats.StatModel;
import com.wynntils.utils.StringUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.ChatFormatting;

public class IngredientModel extends Model {
    private static final Map<ChatFormatting, Integer> TIER_COLOR_CODES = Map.of(
            ChatFormatting.DARK_GRAY, 0,
            ChatFormatting.YELLOW, 1,
            ChatFormatting.LIGHT_PURPLE, 2,
            ChatFormatting.AQUA, 3);
    public static final Map<String, IdentificationModifier> typeMap = new HashMap<>();

    private final IngredientInfoRegistry ingredientInfoRegistry = new IngredientInfoRegistry();

    public IngredientModel(StatModel statModel) {
        super(List.of(statModel));
    }

    public static IdentificationModifier getTypeFromName(String name) {
        return typeMap.get(name);
    }

    public static String getAsLongName(String shortName) {
        if (shortName.startsWith("raw")) {
            shortName = shortName.substring(3);
            shortName = Character.toLowerCase(shortName.charAt(0)) + shortName.substring(1);
        }

        StringBuilder nameBuilder = new StringBuilder();
        for (char c : shortName.toCharArray()) {
            if (Character.isUpperCase(c)) nameBuilder.append(" ").append(c);
            else nameBuilder.append(c);
        }

        return StringUtils.capitalizeFirst(nameBuilder.toString())
                .replaceAll("\\bXp\\b", "XP")
                .replaceAll("\\bX P\\b", "XP");
    }

    public static String getAsShortName(String longIdName, boolean raw) {
        String[] splitName = longIdName.split(" ");
        StringBuilder result = new StringBuilder(raw ? "raw" : "");
        for (String r : splitName) {
            result.append(Character.toUpperCase(r.charAt(0)))
                    .append(r.substring(1).toLowerCase(Locale.ROOT));
        }

        return StringUtils.uncapitalizeFirst(
                StringUtils.capitalizeFirst(result.toString()).replaceAll("\\bXP\\b", "Xp"));
    }

    public int getTierFromColorCode(String tierColor) {
        return TIER_COLOR_CODES.getOrDefault(ChatFormatting.getByCode(tierColor.charAt(0)), 0);
    }

    public IngredientInfo fromName(String ingredientName) {
        return ingredientInfoRegistry.ingredientInfoLookup.get(ingredientName);
    }

    public List<IngredientInfo> getIngredientInfoRegistry() {
        return ingredientInfoRegistry.ingredientInfoRegistry;
    }

    public void reloadData() {
        ingredientInfoRegistry.reloadData();
    }
}
