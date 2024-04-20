/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
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

public final class IngredientPouchAnnotator implements GuiItemAnnotator {
    private static final StyledText INGREDIENT_POUCH_NAME = StyledText.fromString("§6Ingredient Pouch");
    private static final Pattern INGREDIENT_LORE_LINE_PATTERN =
            Pattern.compile("^§f(\\d+) x §7([^§]*)(?:§[3567])? \\[§([8bde])✫(§8)?✫(§8)?✫§[3567]\\]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        // DIAMOND_AXE when not empty, SNOW when empty
        if (itemStack.getItem() != Items.DIAMOND_AXE && itemStack.getItem() != Items.SNOW) return null;
        if (!name.equals(INGREDIENT_POUCH_NAME)) return null;

        List<Pair<IngredientInfo, Integer>> ingredients = new ArrayList<>();
        List<StyledText> lore = LoreUtils.getLore(itemStack);
        for (StyledText line : lore) {
            Matcher matcher = line.getMatcher(INGREDIENT_LORE_LINE_PATTERN);
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
