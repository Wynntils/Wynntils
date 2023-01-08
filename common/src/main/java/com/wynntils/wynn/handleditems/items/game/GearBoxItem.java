/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.game;

import com.wynntils.core.components.Managers;
import com.wynntils.wynn.handleditems.properties.GearTierItemProperty;
import com.wynntils.wynn.objects.profiles.ItemGuessProfile;
import com.wynntils.wynn.objects.profiles.item.GearTier;
import com.wynntils.wynn.objects.profiles.item.GearType;
import java.util.List;
import java.util.Map;

public class GearBoxItem extends GameItem implements GearTierItemProperty {
    private final GearType gearType;
    private final GearTier gearTier;
    private final String levelRange;

    public GearBoxItem(GearType gearType, GearTier gearTier, String levelRange) {
        this.gearType = gearType;
        this.gearTier = gearTier;
        this.levelRange = levelRange;
    }

    public GearType getGearType() {
        return gearType;
    }

    public String getLevelRange() {
        return levelRange;
    }

    public List<String> getItemPossibilities() {
        ItemGuessProfile guessProfile = Managers.GearProfiles.getItemGuess(levelRange);
        if (guessProfile == null) return List.of();

        Map<GearTier, List<String>> rarityMap = guessProfile.getItems().get(gearType);
        if (rarityMap == null) return List.of();

        List<String> itemPossibilities = rarityMap.get(gearTier);
        if (itemPossibilities == null) return List.of();

        return itemPossibilities;
    }

    @Override
    public GearTier getGearTier() {
        return gearTier;
    }

    @Override
    public String toString() {
        return "GearBoxItem{" + "gearType="
                + gearType + ", gearTier="
                + gearTier + ", levelRange='"
                + levelRange + '\'' + '}';
    }
}
