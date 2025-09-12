/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.raids;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CustomColor;
import java.util.Map;
import java.util.TreeMap;

public class OrphionsNexusOfLightRaid extends RaidKind {
    private static final String RAID_NAME = "Orphion's Nexus of Light";
    private static final String ABBREVIATION = "NOL";
    private static final CustomColor RAID_COLOR = CustomColor.fromHexString("#ffaa00ff");
    private static final StyledText ENTRY_TITLE = StyledText.fromString("§f§kOrphion's Nexus of §lLight");

    public OrphionsNexusOfLightRaid() {
        super(RAID_NAME, ABBREVIATION, RAID_COLOR, ENTRY_TITLE, buildRoomMap(), 3, 2, buildMajorIdMap());
    }

    private static Map<Integer, Map<String, String>> buildRoomMap() {
        Map<Integer, Map<String, String>> nameMap = new TreeMap<>();

        Map<String, String> challenge1Map = Map.of("Hold the tower", "Decaying Tower");
        Map<String, String> challenge2Map = Map.of(
                "Kill all Crystalline", "Cloud Decay",
                "Collect 10 Light", "Light Gathering");
        Map<String, String> challenge3Map = Map.of(
                "Purify the decaying", "Light Tower",
                "Escort your party to", "Invisible Maze");

        nameMap.put(1, challenge1Map);
        nameMap.put(2, challenge2Map);
        nameMap.put(3, challenge3Map);
        nameMap.put(4, Map.of("Save Him.", "Orphion"));
        nameMap.put(5, Map.of("Finish that which He", "The Parasite"));

        return nameMap;
    }

    private static Map<String, Map<Integer, String>> buildMajorIdMap() {
        return Map.of(
                "Seraphim", Map.of(3, "Sorcery"),
                "Ophanim", Map.of(3, "Guardian"),
                "Throne", Map.of(3, "Transcendence"),
                "Anti", Map.of(3, "Explosive Impact"));
    }
}
