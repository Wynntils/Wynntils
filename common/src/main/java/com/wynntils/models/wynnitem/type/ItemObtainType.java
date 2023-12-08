/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.type;

import java.util.List;

public enum ItemObtainType {
    // From Wynncraft API
    LOOT_CHEST("lootchest", "Tier III/IV Loot Chest"), // lootchests (implies t3 or t4, afaict)
    NORMAL_MOB_DROP("normal", "Unspecified Mob Drop"), // mob drops (and any loot chest, afaict)
    CHALLENGE("challenge", "Challenge"), // at the moment, only Legendary Island
    EVENT("event", "Event"), // like Bonfire, Heroes, etc.
    LOOTRUN("lootrun", "Lootrun"), // lootrun
    GUILD("guild", "Guild"), // guild
    UNKNOWN("never", "Unknown"), // more information is needed

    // From crowd sourced data or Wynncraft API
    BOSS_ALTAR(List.of("bossaltar", "altar"), "Boss Altar"),
    DISCONTINUED("discontinued", "Discontinued"),
    DUNGEON_MERCHANT(List.of("dungeonmerchant", "dungeonMerchant"), "Dungeon Merchant", true),
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

    private final List<String> apiNames;
    private final String displayName;
    private final boolean isMerchant;
    private final boolean isDungeon;

    ItemObtainType(String apiName, String displayName) {
        this(List.of(apiName), displayName, false, false);
    }

    ItemObtainType(List<String> apiNames, String displayName) {
        this(apiNames, displayName, false, false);
    }

    ItemObtainType(String apiName, String displayName, boolean isMerchant) {
        this(List.of(apiName), displayName, isMerchant, false);
    }

    ItemObtainType(List<String> apiNames, String displayName, boolean isMerchant) {
        this(apiNames, displayName, isMerchant, false);
    }

    ItemObtainType(String apiName, String displayName, boolean isMerchant, boolean isDungeon) {
        this(List.of(apiName), displayName, isMerchant, isDungeon);
    }

    ItemObtainType(List<String> apiNames, String displayName, boolean isMerchant, boolean isDungeon) {
        this.apiNames = apiNames;
        this.displayName = displayName;
        this.isMerchant = isMerchant;
        this.isDungeon = isDungeon;
    }

    public static ItemObtainType fromApiName(String apiName) {
        for (ItemObtainType source : ItemObtainType.values()) {
            if (source.apiNames.contains(apiName)) return source;
        }

        return null;
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
