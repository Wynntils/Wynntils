/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.items.items.game.IngredientItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class IngredientAnnotator implements GameItemAnnotator {
    // Test in IngredientAnnotator_INGREDIENT_PATTERN
    private static final Pattern INGREDIENT_PATTERN =
            Pattern.compile("^§7(.+?) (?:§[3567])?\\[§([8bde])✫(§8)?✫(§8)?✫§[3567]\\]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(INGREDIENT_PATTERN);
        if (!matcher.matches()) return null;

        String ingredientName = matcher.group(1);
        String tierColor = matcher.group(2);

        int tier = Models.Ingredient.getTierFromColorCode(tierColor);
        IngredientInfo ingredientInfo = Models.Ingredient.getIngredientInfoFromName(ingredientName);
        if (ingredientInfo == null) {
            ingredientInfo = Models.Ingredient.getIngredientInfoFromApiName(ingredientName);
            if (ingredientInfo == null) return null;
        }

        if (ingredientInfo.tier() != tier) {
            WynntilsMod.warn("Incorrect tier in ingredient database: " + ingredientName + " is currently " + tier
                    + " vs API " + ingredientInfo.tier());
        }

        return new IngredientItem(ingredientInfo);
    }
}
