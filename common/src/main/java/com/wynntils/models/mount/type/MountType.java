/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.type;

public enum MountType {
    HORSE("Saddle", "Whistle", "Stable Story", 9),
    WYVERN("Reins", "Flute", "The Canyon Guides", 84),
    ADASAUR("Harness", "Ocarina", "Burning Bonds", 115);

    private String mountItemName;
    private String summonItemName;
    private String questRequirement;
    private int level;

    MountType(String mountItemName, String summonItemName, String questRequirement, int level) {
        this.mountItemName = mountItemName;
        this.summonItemName = summonItemName;
        this.questRequirement = questRequirement;
        this.level = level;
    }

    public static MountType fromName(String name) {
        for (MountType type : values()) {
            if (name.equals(type.mountItemName) || name.equals(type.summonItemName)) {
                return type;
            }
        }

        return null;
    }

    public String getMountItemName() {
        return mountItemName;
    }

    public String getSummonItemName() {
        return summonItemName;
    }

    public String getQuestRequirement() {
        return questRequirement;
    }

    public int getLevel() {
        return level;
    }
}
