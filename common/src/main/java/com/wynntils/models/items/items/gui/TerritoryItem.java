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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TerritoryItem extends GuiItem {
    private static final List<Float> RATE_MODIFIERS = List.of(1f, 4f / 3f, 2f, 4f);
    private static final List<Float> RESOURCE_MODIFIERS = List.of(1.0f, 1.5f, 2.0f, 2.5f, 3.0f, 3.5f, 4.0f);
    private static final List<Float> EMERALD_MODIFIERS = List.of(1.0f, 1.35f, 2.0f, 4.0f);

    private final String name;
    private final boolean isHeadquarters;
    private final boolean isSelected;
    private final Map<GuildResource, Integer> production;
    private final Map<GuildResource, CappedValue> storage;
    private final float treasuryBonus;
    private final List<String> alerts;
    private final Map<TerritoryUpgrade, Integer> upgrades;

    private final List<CustomColor> productionColors;
    private final List<CustomColor> seekingColors;
    private final CustomColor treasuryColor;

    private final boolean isDoubleEmeralds;
    private final boolean isDoubleResource;

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
        productionColors = new ArrayList<>();
        int emeraldUpgrades = upgrades.getOrDefault(TerritoryUpgrade.EMERALD_RATE, 0)
                + upgrades.getOrDefault(TerritoryUpgrade.EFFICIENT_EMERALDS, 0);
        int resourceUpgrades = upgrades.getOrDefault(TerritoryUpgrade.RESOURCE_RATE, 0)
                + upgrades.getOrDefault(TerritoryUpgrade.EFFICIENT_RESOURCES, 0);
        if (emeraldUpgrades > 0) {
            productionColors.add(CustomColor.fromHSV(1 / 3f, 0.8f, 0.9f, 1));
        }
        if (resourceUpgrades > 6) {
            productionColors.add(CustomColor.fromHSV(1 / 10f, 0.9f, 0.9f, 1));
        } else if (resourceUpgrades == 6) {
            productionColors.add(CustomColor.fromHSV(1 / 6f, 0.8f, 0.9f, 1));
        } else if (resourceUpgrades > 0) {
            productionColors.add(CustomColor.fromHSV(1 / 6f, 0.5f, 0.8f, 1));
        }
        if (emeraldUpgrades == 0 && resourceUpgrades == 0) {
            productionColors.add(CustomColor.fromHSV(0, 0, 0.6f, 1));
        }

        // Seeking color
        seekingColors = new ArrayList<>();
        int tomeSeek = upgrades.getOrDefault(TerritoryUpgrade.TOME_SEEKING, 0);
        int emeraldSeek = upgrades.getOrDefault(TerritoryUpgrade.EMERALD_SEEKING, 0);
        if (tomeSeek > 0) {
            seekingColors.add(CustomColor.fromHSV(1 / 2f, 0.8f, 0.9f, 1));
        }
        if (emeraldSeek > 0) {
            seekingColors.add(CustomColor.fromHSV(1 / 3f, 0.8f, 0.9f, 1));
        }
        if (tomeSeek == 0 && emeraldSeek == 0) {
            seekingColors.add(CustomColor.fromHSV(0, 0, 0.6f, 1));
        }

        // Treasury color
        treasuryColor = CustomColor.fromHSV(
                Math.max(treasuryBonus / 15 - 1, 0) * -1 / 3f + 5f / 6,
                Math.min(treasuryBonus / 18.75f, 0.8f),
                Math.min(treasuryBonus / 15, 1) * 0.3f + 0.6f,
                1);

        // Double Resource/Emeralds
        double expectedEmeralds = 9000d
                * EMERALD_MODIFIERS.get(upgrades.getOrDefault(TerritoryUpgrade.EFFICIENT_EMERALDS, 0))
                * RATE_MODIFIERS.get(upgrades.getOrDefault(TerritoryUpgrade.EMERALD_RATE, 0));
        // If actual production > 1.5x (Max treasury is 1.3x) what we expect based on upgrades, it likely is a city
        isDoubleEmeralds = production.getOrDefault(GuildResource.EMERALDS, 0) > expectedEmeralds * 1.5f;

        Map.Entry<GuildResource, Integer> primaryResource = null;
        for (Map.Entry<GuildResource, Integer> entry : production.entrySet()) {
            if (entry.getKey() == GuildResource.EMERALDS) continue;
            if (primaryResource == null) primaryResource = entry;
            else if (primaryResource.getValue() < entry.getValue()) primaryResource = entry;
        }
        double expectedResources = 3600d
                * RESOURCE_MODIFIERS.get(upgrades.getOrDefault(TerritoryUpgrade.EFFICIENT_RESOURCES, 0))
                * RATE_MODIFIERS.get(upgrades.getOrDefault(TerritoryUpgrade.RESOURCE_RATE, 0));
        // If actual production > 1.5x (Max treasury is 1.3x) what we expect based on upgrades, it likely is a double
        // production
        isDoubleResource = primaryResource.getValue() > expectedResources * 1.5f;
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

    public List<CustomColor> getProductionColors() {
        return productionColors;
    }

    public List<CustomColor> getSeekingColors() {
        return seekingColors;
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

    public boolean getDoubleEmeralds() {
        return isDoubleEmeralds;
    }

    public boolean getDoubleResource() {
        return isDoubleResource;
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
