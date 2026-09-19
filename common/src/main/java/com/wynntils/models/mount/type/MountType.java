/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.type;

public enum MountType {
    HORSE("Saddle", "Whistle", "Stable Story", 9, 0),
    WYVERN("Reins", "Flute", "The Canyon Guides", 84, 1),
    ADASAUR("Harness", "Ocarina", "Burning Bonds", 115, 2);

    private String mountItemName;
    private String summonItemName;
    private String questRequirement;
    private int level;
    private final int encodingId;

    MountType(String mountItemName, String summonItemName, String questRequirement, int level, int encodingId) {
        this.mountItemName = mountItemName;
        this.summonItemName = summonItemName;
        this.questRequirement = questRequirement;
        this.level = level;
        this.encodingId = encodingId;
    }

    public static MountType fromName(String name) {
        for (MountType type : values()) {
            if (name.equals(type.mountItemName) || name.equals(type.summonItemName)) {
                return type;
            }
        }

        return null;
    }

    public static MountType fromEncodingId(int id) {
        for (MountType mountType : values()) {
            if (mountType.encodingId == id) return mountType;
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

    public int getEncodingId() {
        return encodingId;
    }
}
