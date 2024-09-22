/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.territories.type.GuildResource;
import com.wynntils.models.territories.type.GuildResourceValues;
import com.wynntils.models.territories.type.TerritoryUpgrade;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Map;

public class TerritoryItem extends GuiItem {
    private final String name;
    private final boolean isHeadquarters;
    private final boolean isSelected;
    private final Map<GuildResource, Integer> production;
    private final Map<GuildResource, CappedValue> storage;
    private final float treasuryBonus;
    private final List<String> alerts;
    private final Map<TerritoryUpgrade, Integer> upgrades;

    public TerritoryItem(
            String name,
            boolean isHeadquarters,
            boolean isSelected,
            Map<GuildResource, Integer> production,
            Map<GuildResource, CappedValue> storage,
            float treasuryBonus,
            List<String> alerts,
            Map<TerritoryUpgrade, Integer> upgrades) {
        this.name = name;
        this.isHeadquarters = isHeadquarters;
        this.isSelected = isSelected;
        this.production = production;
        this.storage = storage;
        this.treasuryBonus = treasuryBonus;
        this.alerts = alerts;
        this.upgrades = upgrades;
    }

    public String getName() {
        return name;
    }

    public boolean isHeadquarters() {
        return isHeadquarters;
    }

    // Note: Only works in loadout apply mode
    public boolean isSelected() {
        return isSelected;
    }

    public Map<GuildResource, Integer> getProduction() {
        return production;
    }

    public Map<GuildResource, CappedValue> getStorage() {
        return storage;
    }

    public float getTreasuryBonus() {
        return treasuryBonus;
    }

    public List<String> getAlerts() {
        return alerts;
    }

    public Map<TerritoryUpgrade, Integer> getUpgrades() {
        return upgrades;
    }

    public GuildResourceValues getDefenseDifficulty() {
        // total = damage + attack + health + defense + aura + volley + (-5 if no aura) + (-3 if no volley)
        int total = 0;
        for (Map.Entry<TerritoryUpgrade, Integer> entry : upgrades.entrySet()) {
            switch (entry.getKey()) {
                case DAMAGE:
                case ATTACK:
                case HEALTH:
                case DEFENCE:
                case TOWER_AURA:
                case TOWER_VOLLEY:
                    total += entry.getValue();
                    break;
            }
        }

        if (upgrades.getOrDefault(TerritoryUpgrade.TOWER_AURA, 0) == 0) {
            total -= 5;
        }

        if (upgrades.getOrDefault(TerritoryUpgrade.TOWER_VOLLEY, 0) == 0) {
            total -= 3;
        }

        // Cutoffs:
        // very low
        // low >= -2
        // medium >= 11
        // high >= 23
        // very high >= 41

        if (total < -2) {
            return GuildResourceValues.VERY_LOW;
        } else if (total < 11) {
            return GuildResourceValues.LOW;
        } else if (total < 23) {
            return GuildResourceValues.MEDIUM;
        } else if (total < 41) {
            return GuildResourceValues.HIGH;
        } else {
            return GuildResourceValues.VERY_HIGH;
        }
    }

    @Override
    public String toString() {
        return "TerritoryItem{" + "name='"
                + name + '\'' + ", generation="
                + production + ", storage="
                + storage + ", treasuryBonus="
                + treasuryBonus + ", alerts="
                + alerts + ", upgrades="
                + upgrades + '}';
    }
}
