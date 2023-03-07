/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.utils.type.CappedValue;

public class HorseItem extends GameItem {
    private final int tier;
    private final CappedValue level;
    private final CappedValue xp;
    private final String name;

    public HorseItem(int tier, CappedValue level, CappedValue xp, String name) {
        this.tier = tier;
        this.level = level;
        this.xp = xp;
        this.name = name;
    }

    public int getTier() {
        return tier;
    }

    public CappedValue getLevel() {
        return level;
    }

    public CappedValue getXp() {
        return xp;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "HorseItem{" + "tier=" + tier + ", level=" + level + ", xp=" + xp + ", name='" + name + '\'' + '}';
    }
}
