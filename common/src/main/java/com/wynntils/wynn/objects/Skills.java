/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects;

import java.util.HashMap;
import java.util.Map;

public enum Skills {
    STRENGTH("rawAgility"),
    DEXTERITY("rawDexterity"),
    INTELLIGENCE("rawIntelligence"),
    DEFENCE("rawDefence"),
    AGILITY("rawAgility");

    private final String shortIdName;

    Skills(String shortIdName) {
        this.shortIdName = shortIdName;
    }

    public String getShortIdName() {
        return shortIdName;
    }

    public static Map<String, Skills> getIdSkillMap() {
        Map<String, Skills> map = new HashMap<>();

        for (Skills value : Skills.values()) {
            map.put(value.shortIdName, value);
        }

        return map;
    }
}
