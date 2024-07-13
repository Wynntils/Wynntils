/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.type;

import com.wynntils.core.text.StyledText;

public enum RaidKind {
    NEST_OF_THE_GROOTSLANGS("Nest of the Grootslangs", StyledText.fromString("§2Nest of The Grootslangs")),
    ORPHIONS_NEXUS_OF_LIGHT("Orphion's Nexus of Light", StyledText.fromString("§f§kOrphion's Nexus of §lLight")),
    THE_CANYON_COLOSSUS("The Canyon Colossus", StyledText.fromString("§3§kThe Canyon Colossus")),
    THE_NAMELESS_ANOMALY("The Nameless Anomaly", StyledText.fromString("§9§lThe §1§k§lNameless§9§l Anomaly"));

    private final String name;
    private final StyledText entryTitle;

    RaidKind(String name, StyledText entryTitle) {
        this.name = name;
        this.entryTitle = entryTitle;
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

    public String getName() {
        return name;
    }

    public StyledText getEntryTitle() {
        return entryTitle;
    }
}
