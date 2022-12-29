/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item;

import com.wynntils.core.components.Managers;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.model.item.game.IngredientItem;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.objects.profiles.ingredient.IngredientProfile;
import java.util.regex.Matcher;
import net.minecraft.world.item.ItemStack;

public final class IngredientAnnotator implements ItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        Matcher matcher = WynnItemMatchers.ingredientOrMaterialMatcher(itemStack.getHoverName());
        if (!matcher.matches()) {
            return null;
        }
        IngredientProfile ingredientProfile = Managers.ItemProfiles.getIngredient(matcher.group(1));
        if (ingredientProfile == null) return null;

        return new IngredientItem(ingredientProfile);
    }
}
