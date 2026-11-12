/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
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
import com.wynntils.models.items.items.gui.IngredientPouchItem;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.services.favorites.type.FavoriteCategory;
import com.wynntils.services.favorites.type.LootFilter;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;

public final class FavoritesService extends Service {
    private int revision = 1;

    public FavoritesService() {
        super(List.of());
    }

    public FavoriteCategory getCategory(String unformattedName) {
        for (FavoriteCategory category : getCategories()) {
            if (category.getItems().contains(unformattedName)) {
                return category;
            }
        }

        return null;
    }

    public boolean isFavorite(String unformattedName) {
        return getCategory(unformattedName) != null;
    }

    public boolean isFavorite(Component component) {
        return isFavorite(StyledText.fromComponent(component).getStringWithoutFormatting());
    }

    public boolean isFavorite(NamedItemProperty namedItemProperty) {
        return isFavorite(namedItemProperty.getName());
    }

    public boolean isFavorite(ItemStack itemStack) {
        return isFavorite(itemStack.getHoverName());
    }

    public FavoriteCategory calculateCategory(ItemStack itemStack, WynnItem wynnItem) {
        Set<String> names = new TreeSet<>();
        names.add(WynnUtils.stripItemNameMarkers(StyledText.fromComponent(itemStack.getHoverName())
                .getNormalized()
                .getStringWithoutFormatting()));

        if (wynnItem instanceof NamedItemProperty namedItemProperty) {
            names.add(namedItemProperty.getName());
        }

        if (wynnItem instanceof IngredientPouchItem pouchItem) {
            for (Pair<IngredientInfo, Integer> ingredientPair : pouchItem.getIngredients()) {
                names.add(ingredientPair.a().name());
            }
        }

        if (wynnItem instanceof GearBoxItem gearBoxItem) {
            for (GearInfo possibleGear : Models.Gear.getPossibleGears(gearBoxItem)) {
                names.add(possibleGear.name());
            }
        }

        for (FavoriteCategory category : getCategories()) {
            if (names.stream().anyMatch(category.getItems()::contains)) {
                return category;
            }
        }

        return null;
    }

    public void setCategory(String unformattedName, FavoriteCategory category) {
        getCategories().forEach(c -> c.getItems().remove(unformattedName));
        if (category != null) {
            category.getItems().add(unformattedName);
        }
        touched();
    }

    public void addFavorite(String unformattedName) {
        setCategory(unformattedName, getCategories().getFirst());
    }

    public void clearFavorites() {
        getCategories().forEach(category -> category.getItems().clear());
        touched();
    }

    public void toggleFavorite(String unformattedName) {
        List<FavoriteCategory> categories = getCategories();
        int nextIndex = categories.indexOf(getCategory(unformattedName)) + 1;
        setCategory(unformattedName, nextIndex < categories.size() ? categories.get(nextIndex) : null);
    }

    public SoundEvent getSoundEvent(String sound) {
        Identifier soundId = Identifier.tryParse(sound);
        if (soundId == null || McUtils.mc().getSoundManager().getSoundEvent(soundId) == null) return null;

        return SoundEvent.createVariableRangeEvent(soundId);
    }

    public void playSound(FavoriteCategory category) {
        SoundEvent soundEvent = getSoundEvent(category.getSound());
        if (soundEvent == null) return;

        McUtils.playSoundAmbient(soundEvent, category.getSoundVolume(), category.getSoundPitch());
    }

    public void touched() {
        Managers.Feature.getFeatureInstance(ItemFavoriteFeature.class)
                .lootFilters
                .touched();
        revision++;
    }

    // Used to track changes in the favorites, including their categories
    public int getRevision() {
        return revision;
    }

    public Set<String> getFavoriteItems() {
        Set<String> favoriteItems = new TreeSet<>();
        getCategories().forEach(category -> favoriteItems.addAll(category.getItems()));
        return favoriteItems;
    }

    public List<FavoriteCategory> getCategories() {
        return getActiveLootFilter().getCategories();
    }

    public List<LootFilter> getLootFilters() {
        // This is a hack to allow saving of favorites in the config
        return Managers.Feature.getFeatureInstance(ItemFavoriteFeature.class)
                .lootFilters
                .get();
    }

    public LootFilter getActiveLootFilter() {
        List<LootFilter> lootFilters = getLootFilters();
        String activeName = Managers.Feature.getFeatureInstance(ItemFavoriteFeature.class)
                .activeLootFilter
                .get();

        for (LootFilter lootFilter : lootFilters) {
            if (lootFilter.getName().equals(activeName)) {
                return lootFilter;
            }
        }

        return lootFilters.getFirst();
    }

    public void addLootFilter(LootFilter lootFilter) {
        List<LootFilter> lootFilters = getLootFilters();

        List<String> existingNames =
                lootFilters.stream().map(LootFilter::getName).toList();
        String baseName = lootFilter.getName();
        for (int i = 2; existingNames.contains(lootFilter.getName()); i++) {
            lootFilter.setName(baseName + " " + i);
        }

        lootFilters.add(lootFilter);
        setActiveLootFilter(lootFilter);
        touched();
    }

    public void setActiveLootFilter(LootFilter lootFilter) {
        Managers.Feature.getFeatureInstance(ItemFavoriteFeature.class)
                .activeLootFilter
                .setValue(lootFilter.getName());
    }
}
