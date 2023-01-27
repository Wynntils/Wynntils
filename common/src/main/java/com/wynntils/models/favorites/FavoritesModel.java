/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.favorites;

import com.wynntils.core.components.Model;
import com.wynntils.features.user.ItemFavoriteFeature;
import com.wynntils.models.ingredients.profile.IngredientProfile;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.models.items.items.game.IngredientItem;
import com.wynntils.models.items.items.gui.IngredientPouchItem;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.List;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class FavoritesModel extends Model {
    private int revision = 1;

    public FavoritesModel() {
        super(List.of());
    }

    public boolean isFavorite(String unformattedName) {
        return getFavoriteItems().contains(unformattedName);
    }

    public boolean isFavorite(Component component) {
        return isFavorite(ComponentUtils.getUnformatted(component));
    }

    public boolean isFavorite(ItemStack itemStack) {
        return isFavorite(itemStack.getHoverName());
    }

    public boolean calculateFavorite(ItemStack itemStack, WynnItem wynnItem) {
        String unformattedName = WynnUtils.normalizeBadString(ComponentUtils.getUnformatted(itemStack.getHoverName()));

        if (isFavorite(unformattedName)) {
            return true;
        }

        if (wynnItem instanceof IngredientItem ingredientItem) {
            return isFavorite(ingredientItem.getIngredientProfile().getDisplayName());
        }

        if (wynnItem instanceof IngredientPouchItem pouchItem) {
            for (Pair<IngredientProfile, Integer> ingredientPair : pouchItem.getIngredients()) {
                IngredientProfile ingredientProfile = ingredientPair.a();
                if (isFavorite(ingredientProfile.getDisplayName())) {
                    return true;
                }
            }
        }

        if (wynnItem instanceof GearBoxItem gearBoxItem) {
            for (String possibleItem : gearBoxItem.getItemPossibilities()) {
                if (isFavorite(possibleItem)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void addFavorite(String unformattedName) {
        getFavoriteItems().add(unformattedName);
        revision++;
    }

    public void removeFavorite(String unformattedName) {
        getFavoriteItems().remove(unformattedName);
        revision++;
    }

    public void toggleFavorite(String unformattedName) {
        if (isFavorite(unformattedName)) {
            removeFavorite(unformattedName);
        } else {
            addFavorite(unformattedName);
        }
    }

    // Used to track changes in the set of favorites
    public int getRevision() {
        return revision;
    }

    private Set<String> getFavoriteItems() {
        // This is a hack to allow saving of favorites in the config
        return ItemFavoriteFeature.INSTANCE.favoriteItems;
    }
}
