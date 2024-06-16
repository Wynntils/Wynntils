/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

public enum MissionType {
    CLEANSING_GREED("Cleansing Greed", 'e'),
    HIGH_ROLLER("High Roller", 'e'),
    MATERIALISM("Materialism", 'e'),
    ORPHIONS_GRACE("Orphion's Grace", '9'),
    CLEANSING_RITUAL("Cleansing Ritual", '5'),
    EQUILIBRIUM("Equilibrium", '5'),
    INNER_PEACE("Inner Peace", '5'),
    BACKUP_BEAT("Backup Beat", 'a'),
    STASIS("Stasis", 'a'),
    GAMBLING_BEAST("Gambling Beast", 'c'),
    REDEMPTION("Redemption", 'c'),
    ULTIMATE_SACRIFICE("Ultimate Sacrifice", 'c'),
    FAILED("Failed", '4'),
    ;

    private final String name;
    private final char color;

    MissionType(String name, char color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getColoredName() {
        return "§" + color + name;
    }

    public char getColor() {
        return color;
    }

    public static MissionType fromName(String name) {
        for (MissionType type : values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
