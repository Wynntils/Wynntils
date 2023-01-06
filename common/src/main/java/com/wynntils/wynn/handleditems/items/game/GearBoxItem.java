/*
 * Copyright © Wynntils 2022, 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.game;

import com.wynntils.core.components.Managers;
import com.wynntils.wynn.handleditems.properties.GearTierItemProperty;
import com.wynntils.wynn.objects.profiles.ItemGuessProfile;
import com.wynntils.wynn.objects.profiles.item.ItemTier;
import com.wynntils.wynn.objects.profiles.item.ItemType;
import java.util.List;
import java.util.Map;

public class GearBoxItem extends GameItem implements GearTierItemProperty {
    private final ItemType itemType;
    private final ItemTier itemTier;
    private final String levelRange;

    public GearBoxItem(ItemType itemType, ItemTier itemTier, String levelRange) {
        this.itemType = itemType;
        this.itemTier = itemTier;
        this.levelRange = levelRange;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public ItemTier getItemTier() {
        return itemTier;
    }

    public String getLevelRange() {
        return levelRange;
    }

    public List<String> getItemPossibilities() {
        ItemGuessProfile guessProfile = Managers.ItemProfiles.getItemGuess(levelRange);
        if (guessProfile == null) return List.of();

        Map<ItemTier, List<String>> rarityMap = guessProfile.getItems().get(itemType);
        if (rarityMap == null) return List.of();

        List<String> itemPossibilities = rarityMap.get(itemTier);
        if (itemPossibilities == null) return List.of();

        return itemPossibilities;
    }

    @Override
    public ItemTier getGearTier() {
        return itemTier;
    }

    @Override
    public String toString() {
        return "GearBoxItem{" + "itemType="
                + itemType + ", itemTier="
                + itemTier + ", levelRange='"
                + levelRange + '\'' + '}';
    }
}
