/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.type;

import com.wynntils.core.text.StyledText;
import java.util.Map;
import java.util.regex.Pattern;

public enum RaidKind {
    NEST_OF_THE_GROOTSLANGS(
            "Nest of the Grootslangs",
            StyledText.fromString("§2Nest of The Grootslangs"),
            Pattern.compile("^Slay the Restless$"),
            Map.of(
                    "Beserk", Map.of(1, "Explosive Impact"),
                    "Lightbearer", Map.of(3, "Transcendence"),
                    "Pestilent", Map.of(3, "Plague"),
                    "Bedrock", Map.of(3, "Heart of the Pack"))),
    ORPHIONS_NEXUS_OF_LIGHT(
            "Orphion's Nexus of Light",
            StyledText.fromString("§f§kOrphion's Nexus of §lLight"),
            Pattern.compile("^Save Him.$"),
            Map.of(
                    "Seraphim", Map.of(3, "Sorcery"),
                    "Ophanim", Map.of(3, "Guardian"),
                    "Throne", Map.of(3, "Transcendence"),
                    "Anti", Map.of(3, "Explosive Impact"))),
    THE_CANYON_COLOSSUS(
            "The Canyon Colossus",
            StyledText.fromString("§#5f968bffThe Canyon Colossus"),
            Pattern.compile("^Calm the canyon's$"),
            Map.of(
                    "Intrepid",
                            Map.of(
                                    1, "Heart of the Pack",
                                    2, "Greed",
                                    3, "Guardian"),
                    "Stonewalker", Map.of(3, "Explosive Impact"))),
    THE_NAMELESS_ANOMALY(
            "The Nameless Anomaly",
            StyledText.fromString("§9§lThe §1§k§lNameless§9§l Anomaly"),
            Pattern.compile("^Survive.$"),
            Map.of(
                    "Fading", Map.of(1, "Heart of the Pack"),
                    "Hollowed", Map.of(2, "Guardian"),
                    "Sojourner", Map.of(2, "Freerunner"),
                    "Hopeless", Map.of(2, "Fission"),
                    "Insidious", Map.of(3, "Sorcery")));

    private final String name;
    private final StyledText entryTitle;
    private final Pattern bossScoreboardPattern;

    private final Map<String, Map<Integer, String>> majorIdBuffs;

    RaidKind(
            String name,
            StyledText entryTitle,
            Pattern bossScoreboardPattern,
            Map<String, Map<Integer, String>> majorIdBuffs) {
        this.name = name;
        this.entryTitle = entryTitle;
        this.bossScoreboardPattern = bossScoreboardPattern;
        this.majorIdBuffs = majorIdBuffs;
    }

    public static RaidKind fromTitle(StyledText title) {
        for (RaidKind raidKind : RaidKind.values()) {
            if (raidKind.getEntryTitle().equals(title)) {
                return raidKind;
            }
        }

        return null;
    }

    public static RaidKind fromName(String name) {
        for (RaidKind raidKind : RaidKind.values()) {
            if (raidKind.getName().equalsIgnoreCase(name)) {
                return raidKind;
            }
        }

        return null;
    }

    public String majorIdFromBuff(String buff, int tier) {
        if (!majorIdBuffs.containsKey(buff)) return null;
        return majorIdBuffs.get(buff).get(tier);
    }

    public String getName() {
        return name;
    }

    public StyledText getEntryTitle() {
        return entryTitle;
    }

    public Pattern getBossScoreboardPattern() {
        return bossScoreboardPattern;
    }
}
