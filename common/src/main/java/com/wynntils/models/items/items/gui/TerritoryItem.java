/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.territories.type.GuildResource;
import com.wynntils.models.territories.type.TerritoryStorage;
import com.wynntils.models.territories.type.TerritoryUpgrade;
import java.util.List;
import java.util.Map;

public class TerritoryItem extends GuiItem {
    private final String name;
    private final Map<GuildResource, Integer> generation;
    private final Map<GuildResource, TerritoryStorage> storage;
    private final int treasuryBonus;
    private final List<String> alerts;
    private final Map<TerritoryUpgrade, Integer> upgrades;

    public TerritoryItem(
            String name,
            Map<GuildResource, Integer> generation,
            Map<GuildResource, TerritoryStorage> storage,
            int treasuryBonus,
            List<String> alerts,
            Map<TerritoryUpgrade, Integer> upgrades) {
        this.name = name;
        this.generation = generation;
        this.storage = storage;
        this.treasuryBonus = treasuryBonus;
        this.alerts = alerts;
        this.upgrades = upgrades;
    }

    public String getName() {
        return name;
    }

    public Map<GuildResource, Integer> getGeneration() {
        return generation;
    }

    public Map<GuildResource, TerritoryStorage> getStorage() {
        return storage;
    }

    public int getTreasuryBonus() {
        return treasuryBonus;
    }

    public List<String> getAlerts() {
        return alerts;
    }

    public Map<TerritoryUpgrade, Integer> getUpgrades() {
        return upgrades;
    }

    @Override
    public String toString() {
        return "TerritoryItem{" + "name='"
                + name + '\'' + ", generation="
                + generation + ", storage="
                + storage + ", treasuryBonus="
                + treasuryBonus + ", alerts="
                + alerts + ", upgrades="
                + upgrades + '}';
    }
}
