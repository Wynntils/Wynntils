/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.type;

import com.wynntils.utils.MathUtils;
import java.util.HashMap;
import java.util.Map;

public final class RaidBuffs {
    // Map of raid buff names to a list of the major ids they provide
    private static final Map<String, Map<Integer, String>> staticRaidBuffs = new HashMap<>() {
        {
            // NOTG
            put("Beserk", Map.of(1, "Explosive Impact"));
            put("Lightbearer", Map.of(3, "Transcendence"));
            put("Pestilent", Map.of(3, "Plague"));
            put("Bedrock", Map.of(3, "Heart of the Pack"));

            // NOL
            put("Seraphim", Map.of(3, "Sorcery"));
            put("Ophanim", Map.of(3, "Guardian"));
            put("Throne", Map.of(3, "Transcendence"));
            put("Anti", Map.of(3, "Explosive Impact"));

            // TCC
            put(
                    "Intrepid",
                    Map.of(
                            1, "Heart of the Pack",
                            2, "Greed",
                            3, "Guardian"));
            put("StoneWalker", Map.of(3, "Explosive Impact"));

            // TNA
            put("Fading", Map.of(1, "Heart of the Pack"));
            put("Hollowed", Map.of(2, "Guardian"));
            put("Sojourner", Map.of(2, "Freerunner"));
            put("Hopeless", Map.of(2, "Fission"));
            put("Insidious", Map.of(3, "Sorcery"));
        }
    };

    public static String majorIdFromName(String name) {
        String[] split = name.split(" ");
        if (split.length == 1) return null;

        if (staticRaidBuffs.containsKey(split[0])) {
            return staticRaidBuffs.get(split[0]).get(MathUtils.integerFromRoman(split[1]));
        }

        return null;
    }
}
