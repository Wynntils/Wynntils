/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.ingredients.profile.IngredientProfile;
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
    private static final Pattern INGREDIENT_POUCH_PATTERN = Pattern.compile("^§6Ingredient Pouch$");
    private static final Pattern INGREDIENT_LORE_LINE_PATTERN = Pattern.compile(
            "^§f(\\d+) x (?:§r)?§7([^§]*)(?:§r)?(?:§[3567])? \\[(?:§r)?§([8bde])✫(?:§r)?(§8)?✫(?:§r)?(§8)?✫(?:§r)?§[3567]\\]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        if (itemStack.getItem() != Items.DIAMOND_AXE) return null;
        Matcher matcher = INGREDIENT_POUCH_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        List<Pair<IngredientProfile, Integer>> ingredients = new ArrayList<>();
        List<String> lore = LoreUtils.getLore(itemStack);
        for (String line : lore) {
            Matcher loreMatcher = INGREDIENT_LORE_LINE_PATTERN.matcher(line);
            if (!loreMatcher.matches()) continue;
            int count = Integer.parseInt(loreMatcher.group(1));
            String ingredientName = loreMatcher.group(2);
            String tierColor = loreMatcher.group(3);
            int tier = Managers.GearProfiles.getTierFromColorCode(tierColor);

            IngredientProfile ingredientProfile = Managers.GearProfiles.getIngredient(ingredientName);
            if (ingredientProfile == null) return null;
            if (ingredientProfile.getTier().getTierInt() != tier) {
                WynntilsMod.warn("Incorrect tier in ingredient database: " + ingredientName + " is " + tier);
                return null;
            }

            ingredients.add(Pair.of(ingredientProfile, count));
        }

        return new IngredientPouchItem(ingredients);
    }
}
