/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.game;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.handleditems.items.game.IngredientItem;
import com.wynntils.wynn.objects.profiles.ingredient.IngredientProfile;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;

public final class IngredientAnnotator implements ItemAnnotator {
    private static final Pattern INGREDIENT_PATTERN =
            Pattern.compile("^§7(.*)§[3567] \\[§([8bde])✫(§8)?✫(§8)?✫§[3567]\\]$");
    private static final Map<ChatFormatting, Integer> LEVEL_COLORS = Map.of(
            ChatFormatting.DARK_GRAY, 0,
            ChatFormatting.YELLOW, 1,
            ChatFormatting.LIGHT_PURPLE, 2,
            ChatFormatting.AQUA, 3);

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        String name = ComponentUtils.getCoded(itemStack.getHoverName());
        Matcher matcher = INGREDIENT_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        String ingredientName = matcher.group(1);
        String tierColor = matcher.group(2);
        int tier = getTierFromColorCode(tierColor);

        IngredientProfile ingredientProfile = Managers.ItemProfiles.getIngredient(ingredientName);
        if (ingredientProfile == null) return null;
        if (ingredientProfile.getTier().getTierInt() != tier) {
            WynntilsMod.warn("Incorrect tier in ingredient database: " + ingredientName + " is " + tier);
            return null;
        }

        return new IngredientItem(ingredientProfile);
    }

    private int getTierFromColorCode(String tierColor) {
        return LEVEL_COLORS.getOrDefault(ChatFormatting.getByCode(tierColor.charAt(0)), 0);
    }
}
