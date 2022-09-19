/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects;

import java.util.HashMap;
import java.util.Map;

public class ProfessionInfo {
    private Map<ProfessionType, Integer> levels = new HashMap<>();

    public ProfessionInfo(Map<ProfessionType, Integer> levels) {
        this.levels = levels;
    }

    public ProfessionInfo() {
        for (ProfessionType value : ProfessionType.values()) {
            levels.put(value, 0);
        }
    }

    public int getLevel(ProfessionType type) {
        return levels.getOrDefault(type, 0);
    }

    public enum ProfessionType {
        Mining,
        Woodcutting,
        Farming,
        Fishing,

        Armouring,
        Tailoring,
        Weaponsmithing,
        Woodworking,
        Jeweling,
        Alchemism,
        Scribing,
        Cooking
    }
}
