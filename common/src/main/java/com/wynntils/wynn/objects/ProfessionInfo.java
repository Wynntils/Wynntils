/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects;

import com.wynntils.wynn.objects.profiles.ingredient.ProfessionType;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class ProfessionInfo {
    private Map<ProfessionType, Integer> levels = new HashMap<>();

    public ProfessionInfo(Map<ProfessionType, Integer> levels) {
        for (ProfessionType value : ProfessionType.values()) {
            levels.putIfAbsent(value, 0);
        }

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

    @Override
    public String toString() {
        return "ProfessionInfo{" + "levels=" + StringUtils.join(levels) + '}';
    }
}
