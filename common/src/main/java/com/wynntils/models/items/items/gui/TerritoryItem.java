/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.territories.type.GuildResource;
import com.wynntils.models.territories.type.GuildResourceValues;
import com.wynntils.models.territories.type.TerritoryUpgrade;
import com.wynntils.utils.colors.CustomColor;
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

    private final CustomColor productionColor;
    private final CustomColor seekingColor;
    private final CustomColor treasuryColor;

    private boolean isPending = false;

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

        // Production color
        int emeraldUpgrades = upgrades.getOrDefault(TerritoryUpgrade.EMERALD_RATE, 0)
                + upgrades.getOrDefault(TerritoryUpgrade.EFFICIENT_EMERALDS, 0);
        int resourceUpgrades = upgrades.getOrDefault(TerritoryUpgrade.RESOURCE_RATE, 0)
                + upgrades.getOrDefault(TerritoryUpgrade.EFFICIENT_RESOURCES, 0);
        if (emeraldUpgrades > 0) {
            if (resourceUpgrades > 0) {
                productionColor = CustomColor.fromHSV(0.5f, 0.8f, 0.9f, 1);
            } else {
                productionColor = CustomColor.fromHSV(1 / 3f, 0.8f, 0.9f, 1);
            }
        } else {
            // 4 3 or above -> 100% saturation
            // 3 3 or below -> 50% saturation
            if (resourceUpgrades > 6) {
                productionColor = CustomColor.fromHSV(1 / 6f, 1.0f, 1.0f, 1);
            } else if (resourceUpgrades > 0) {
                productionColor = CustomColor.fromHSV(1 / 6f, 0.50f, 0.9f, 1);
            } else {
                productionColor = CustomColor.fromHSV(0, 0, 0.6f, 1);
            }
        }

        // Seeking color
        int tomeSeek = upgrades.getOrDefault(TerritoryUpgrade.TOME_SEEKING, 0);
        int emeraldSeek = upgrades.getOrDefault(TerritoryUpgrade.EMERALD_SEEKING, 0);
        if (tomeSeek > 0 && emeraldSeek > 0) {
            seekingColor = CustomColor.fromHSV(1 / 2f, 0.8f, 0.9f, 1);
        } else if (tomeSeek > 0) {
            seekingColor = CustomColor.fromHSV(2 / 3f, 0.8f, 0.9f, 1);
        } else if (emeraldSeek > 0) {
            seekingColor = CustomColor.fromHSV(1 / 3f, 0.8f, 0.9f, 1);
        } else {
            seekingColor = CustomColor.fromHSV(0, 0, 0.6f, 1);
        }

        // Treasury color
        treasuryColor = CustomColor.fromHSV(
                Math.max(treasuryBonus / 15 - 1, 0) * -1 / 3f + 5f / 6,
                Math.min(treasuryBonus / 18.75f, 0.8f),
                Math.min(treasuryBonus / 15, 1) * 0.3f + 0.6f,
                1);
    }

    public String getName() {
        return name;
    }

    public void markPending() {
        isPending = true;
    }

    public boolean isHeadquarters() {
        return isHeadquarters;
    }

    // Note: Only works in loadout apply mode
    public boolean isSelected() {
        return isSelected;
    }

    public boolean isPending() {
        return isPending;
    }

    public float getTreasuryBonus() {
        return treasuryBonus;
    }

    public CustomColor getProductionColor() {
        return productionColor;
    }

    public CustomColor getSeekingColor() {
        return seekingColor;
    }

    public CustomColor getTreasuryColor() {
        return treasuryColor;
    }

    public Map<GuildResource, Integer> getProduction() {
        return production;
    }

    public Map<GuildResource, CappedValue> getStorage() {
        return storage;
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
