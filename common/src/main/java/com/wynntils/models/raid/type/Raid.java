/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.type;

import com.wynntils.core.text.StyledText;
import java.util.List;
import java.util.regex.Pattern;

public enum Raid {
    NEST_OF_THE_GROOTSLANGS(
            "Nest of the Grootslangs",
            StyledText.fromString("§2§kNest§f §2§kof§f §2§kThe§f §2§kGrootslangs"),
            List.of(Pattern.compile("§2§lInstructions")),
            StyledText.fromString("§cGrootslang Wyrmling§6 [Lv. 75]")),
    ORPHIONS_NEXUS_OF_LIGHT(
            "Orphion's Nexus of Light",
            StyledText.fromString("§f§kOrphion's Nexus of §lLight"),
            List.of(Pattern.compile("§6§lInstructions")),
            StyledText.fromString("§e§lOrphion, §cThe Light Beast§6 [Lv. 250]")),
    THE_CANYON_COLOSSUS(
            "The Canyon Colossus",
            StyledText.fromString("§3§kThe Canyon Colossus"),
            // Currently the only challenge to not say "Instructions"
            List.of(Pattern.compile("§3§lInstructions"), Pattern.compile("§3§lHold the Platforms")),
            StyledText.fromString("§cColossal Duke§6 [Lv. 190]")),
    THE_NAMELESS_ANOMALY(
            "The Nameless Anomaly",
            StyledText.fromString("§9§lThe §1§k§lNameless§9§l Anomaly"),
            List.of(Pattern.compile("§9§lInstructions: .+")),
            StyledText.fromString("§9§lThe §1§k12345§9§l Anomaly§6 [Lv. 250]"));

    private final String name;
    private final StyledText entryTitle;
    private final List<Pattern> instructionsPatterns;
    private final StyledText bossLabel;

    Raid(String name, StyledText entryTitle, List<Pattern> instructionsPatterns, StyledText bossLabel) {
        this.name = name;
        this.entryTitle = entryTitle;
        this.instructionsPatterns = instructionsPatterns;
        this.bossLabel = bossLabel;
    }

    public static Raid fromTitle(StyledText title) {
        for (Raid raid : Raid.values()) {
            if (raid.getEntryTitle().equals(title)) {
                return raid;
            }
        }

        return null;
    }

    public static Raid fromName(String name) {
        for (Raid raid : Raid.values()) {
            if (raid.getName().equalsIgnoreCase(name)) {
                return raid;
            }
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public StyledText getEntryTitle() {
        return entryTitle;
    }

    public List<Pattern> getInstructionsPatterns() {
        return instructionsPatterns;
    }

    public StyledText getBossLabel() {
        return bossLabel;
    }
}
