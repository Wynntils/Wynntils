/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.items.items.gui.IngredientPouchItem;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class IngredientPouchAnnotator implements ItemAnnotator {
    private static final String INGREDIENT_POUCH_NAME = "§6Ingredient Pouch";
    private static final Pattern INGREDIENT_LORE_LINE_PATTERN = Pattern.compile(
            "^§f(\\d+) x (?:§r)?§7([^§]*)(?:§r)?(?:§[3567])? \\[(?:§r)?§([8bde])✫(?:§r)?(§8)?✫(?:§r)?(§8)?✫(?:§r)?§[3567]\\]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        if (itemStack.getItem() != Items.DIAMOND_AXE) return null;
        if (!name.equals(INGREDIENT_POUCH_NAME)) return null;

        List<Pair<IngredientInfo, Integer>> ingredients = new ArrayList<>();
        List<String> lore = LoreUtils.getLore(itemStack);
        for (String line : lore) {
            Matcher matcher = INGREDIENT_LORE_LINE_PATTERN.matcher(line);
            if (!matcher.matches()) continue;

            int count = Integer.parseInt(matcher.group(1));
            String ingredientName = matcher.group(2);
            String tierColor = matcher.group(3);

            int tier = Models.Ingredient.getTierFromColorCode(tierColor);
            IngredientInfo ingredientInfo = Models.Ingredient.getIngredientInfoFromName(ingredientName);

            // Skip unknown ingredients; the pouch list will be wrong but better than nothing
            if (ingredientInfo == null) continue;

            if (ingredientInfo.tier() != tier) {
                WynntilsMod.warn("Incorrect tier in ingredient database: " + ingredientName + " is " + tier);
            }

            ingredients.add(Pair.of(ingredientInfo, count));
        }

        return new IngredientPouchItem(ingredients);
    }
}
