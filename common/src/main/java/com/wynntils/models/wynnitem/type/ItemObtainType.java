/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.type;

public enum ItemObtainType {
    // From Wynncraft API
    LOOT_CHEST("lootchest", "Tier III/IV Loot Chest"), // lootchests (implies t3 or t4, afaict)
    NORMAL_MOB_DROP("normal", "Unspecified Mob Drop"), // mob drops (and any loot chest, afaict)
    UNKNOWN("never", "Unknown"), // more information is needed

    // From crowd sourced data
    BOSS_ALTAR("bossaltar", "Boss Altar"),
    DISCONTINUED("discontinued", "Discontinued"),
    DUNGEON_MERCHANT("dungeonmerchant", "Dungeon Merchant", true),
    DUNGEON_RAIN("dungeon", "Dungeon Rain"),
    FORGERY_CHEST("forgerychest", "Forgery Chest", false, true),
    GATHERING("gathering", "Gathering"),
    LEGENDARY_ISLAND_MERCHANT("legendaryisland", "Legendary Island Merchant", true),
    MERCHANT("merchant", "Merchant", true),
    QIRA_HIVE_MERCHANT("hive", "Qira Hive Merchant", true),
    QUEST("quest", "Quest"),
    RAID("raid", "Raid"),
    SECRET_DISCOVER("discovery", "Secret Discovery"),
    SPECIAL_MOB_DROP("specialdrop", "Specific Mob Drop"),
    UNOBTAINABLE("unobtainable", "Unobtainable");

    private final String apiName;
    private final String displayName;
    private final boolean isMerchant;
    private final boolean isDungeon;

    ItemObtainType(String apiName, String displayName) {
        this(apiName, displayName, false, false);
    }

    ItemObtainType(String apiName, String displayName, boolean isMerchant) {
        this(apiName, displayName, isMerchant, false);
    }

    ItemObtainType(String apiName, String displayName, boolean isMerchant, boolean isDungeon) {
        this.apiName = apiName;
        this.displayName = displayName;
        this.isMerchant = isMerchant;
        this.isDungeon = isDungeon;
    }

    public static ItemObtainType fromApiName(String apiName) {
        for (ItemObtainType source : ItemObtainType.values()) {
            if (source.apiName.equals(apiName)) return source;
        }

        return null;
    }

    public String getApiName() {
        return apiName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isMerchant() {
        return isMerchant;
    }

    public boolean isDungeon() {
        return isDungeon;
    }
}
