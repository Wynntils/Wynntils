/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.game;

import com.wynntils.utils.CappedValue;

public class HorseItem extends GameItem {
    private final int tier;
    private final CappedValue level;
    private final int xp;
    private final String name;

    public HorseItem(int tier, CappedValue level, int xp, String name) {
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

    public int getXp() {
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
