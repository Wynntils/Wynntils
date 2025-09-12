/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.raids;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CustomColor;
import java.util.Map;
import java.util.TreeMap;

public class TheNamelessAnomalyRaid extends RaidKind {
    private static final String RAID_NAME = "The Nameless Anomaly";
    private static final String ABBREVIATION = "TNA";
    private static final CustomColor RAID_COLOR = CustomColor.fromHexString("#5555ffff");
    private static final StyledText ENTRY_TITLE = StyledText.fromString("§9§lThe §1§k§lNameless§9§l Anomaly");

    public TheNamelessAnomalyRaid() {
        super(RAID_NAME, ABBREVIATION, RAID_COLOR, ENTRY_TITLE, buildRoomMap(), buildMajorIdMap());
    }

    private static Map<Integer, Map<String, String>> buildRoomMap() {
        Map<Integer, Map<String, String>> nameMap = new TreeMap<>();

        Map<String, String> challenge1Map = Map.of(
                "One player must take", "Flooding Canyon",
                "Hold the stump for", "Sunken Grotto");
        Map<String, String> challenge2Map = Map.of(
                "Find and kill", "Nameless Cave",
                "Offer souls to the", "Weeping Soulroot");
        Map<String, String> challenge3Map = Map.of(
                "Protect the Bulb", "Blueshift Wilds",
                "Collect 5 Void Matter", "Twisted Jungle");

        nameMap.put(1, challenge1Map);
        nameMap.put(2, challenge2Map);
        nameMap.put(3, challenge3Map);
        nameMap.put(4, Map.of("Survive.", "The ##### Anomaly"));

        return nameMap;
    }

    private static Map<String, Map<Integer, String>> buildMajorIdMap() {
        return Map.of(
                "Fading", Map.of(1, "Altruism"),
                "Hollowed", Map.of(2, "Guardian"),
                "Sojourner", Map.of(2, "Freerunner"),
                "Hopeless", Map.of(2, "Fission"),
                "Insidious", Map.of(3, "Sorcery"));
    }
}
