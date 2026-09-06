/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

public class LootrunDetails {
    private LootrunLocation lootrunLocation = LootrunLocation.UNKNOWN;
    private int sacrifices = 0;
    private int rerolls = 0;

    public LootrunLocation getLootrunLocation() {
        return lootrunLocation;
    }

    public void setLootrunLocation(LootrunLocation lootrunLocation) {
        this.lootrunLocation = lootrunLocation;
    }

    public int getSacrifices() {
        return sacrifices;
    }

    public void setSacrifices(int sacrifices) {
        this.sacrifices = sacrifices;
    }

    public int getRerolls() {
        return rerolls;
    }

    public void setRerolls(int rerolls) {
        this.rerolls = rerolls;
    }
}
