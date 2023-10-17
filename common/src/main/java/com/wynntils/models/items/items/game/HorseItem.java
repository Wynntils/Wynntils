/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.horse.type.HorseTier;
import com.wynntils.utils.type.CappedValue;
import java.util.Optional;

public class HorseItem extends GameItem {
    private final HorseTier tier;
    private final CappedValue level;
    private final CappedValue xp;
    private final String name;

    public HorseItem(HorseTier tier, CappedValue level, CappedValue xp, String name) {
        this.tier = tier;
        this.level = level;
        this.xp = xp;
        this.name = name;
    }

    public HorseTier getTier() {
        return tier;
    }

    public CappedValue getLevel() {
        return level;
    }

    public CappedValue getXp() {
        return xp;
    }

    public Optional<String> getName() {
        // Name is only set if the horse is named
        return Optional.ofNullable(name);
    }

    @Override
    public String toString() {
        return "HorseItem{" + "tier=" + tier + ", level=" + level + ", xp=" + xp + ", name='" + name + '\'' + '}';
    }
}
