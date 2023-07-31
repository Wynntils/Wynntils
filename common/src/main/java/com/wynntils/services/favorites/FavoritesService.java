/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.favorites;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Service;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.inventory.ItemFavoriteFeature;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.models.items.items.game.IngredientItem;
import com.wynntils.models.items.items.gui.IngredientPouchItem;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class FavoritesService extends Service {
    private int revision = 1;

    public FavoritesService() {
        super(List.of());
    }

    public boolean isFavorite(String unformattedName) {
        return getFavoriteItems().contains(unformattedName);
    }

    public boolean isFavorite(Component component) {
        return isFavorite(StyledText.fromComponent(component).getStringWithoutFormatting());
    }

    public boolean isFavorite(ItemStack itemStack) {
        return isFavorite(itemStack.getHoverName());
    }

    public boolean calculateFavorite(ItemStack itemStack, WynnItem wynnItem) {
        String unformattedName = StyledText.fromComponent(itemStack.getHoverName())
                .getNormalized()
                .getStringWithoutFormatting();

        if (isFavorite(unformattedName)) {
            return true;
        }

        if (wynnItem instanceof IngredientItem ingredientItem) {
            return isFavorite(ingredientItem.getIngredientInfo().name());
        }

        if (wynnItem instanceof IngredientPouchItem pouchItem) {
            for (Pair<IngredientInfo, Integer> ingredientPair : pouchItem.getIngredients()) {
                IngredientInfo ingredientProfile = ingredientPair.a();
                if (isFavorite(ingredientProfile.name())) {
                    return true;
                }
            }
        }

        if (wynnItem instanceof GearBoxItem gearBoxItem) {
            for (GearInfo possibleGear : Models.Gear.getPossibleGears(gearBoxItem)) {
                if (isFavorite(possibleGear.name())) {
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

    public Set<String> getFavoriteItems() {
        // This is a hack to allow saving of favorites in the config
        return Managers.Feature.getFeatureInstance(ItemFavoriteFeature.class)
                .favoriteItems
                .get();
    }
}
