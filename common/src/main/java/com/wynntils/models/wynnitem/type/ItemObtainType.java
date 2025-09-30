/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.type;

import java.util.List;

public enum ItemObtainType {
    // From Wynncraft API or crowd sourced
    BOSS_ALTAR("altar", "Boss Altar"),
    CHALLENGE("challenge", "Challenge"),
    DUNGEON("dungeon", "Dungeon Rain"),
    DUNGEON_MERCHANT("dungeonMerchant", "Dungeon Merchant"),
    EVENT("event", "Event"),
    GUILD("guild", "Guild"),
    LOOTRUN("lootrun", "Lootrun"),
    MERCHANT("merchant", "Merchant"),
    MINIBOSS("miniboss", "Miniboss"),
    QUEST("quest", "Quest"),
    RAID("raid", "Raid"),

    // Crowd sourced or determined via drop restriction
    CAVE_COMPLETION("caveCompletion", "Cave Completion"),
    CAVE_LOOT_CHEST("caveLootChest", "Tier 3/4 Loot Chest"),
    DISCOVERY("discovery", "Discovery"),
    ENVIRONMENT("environment", "Environment"),
    FORGERY_CHEST("forgeryChest", "Forgery Chest"),
    GATHERING("gathering", "Gathering"),
    INTERACTION("interaction", "World Interaction"),
    LOOT_CHEST("lootChest", "Any Loot Chest"),
    MOB_REGION("mobDropRegion", "Mobs in"),
    NORMAL_MOB_DROP("normalMobDrop", "Normal Mob Drop"),
    RARE_MOB_REGION("rareMobDropRegion", "Rare Mobs in"),
    SPECIFIC_MOB_DROP("specificMobDrop", "Specific Mob Drop"),
    TINKERING("tinkering", "Tinkering"),
    WORLD_EVENT("worldEvent", "World Event"),

    UNAVAILABLE("unavailable", "Unavailable"),
    UNKNOWN("unknown", "Unknown");

    // All sources that possibly drop boxed items
    public static final List<ItemObtainType> BOXED_ITEMS = List.of(
            BOSS_ALTAR,
            CAVE_LOOT_CHEST,
            DISCOVERY,
            DUNGEON,
            FORGERY_CHEST,
            GUILD,
            INTERACTION,
            LOOT_CHEST,
            MINIBOSS,
            MOB_REGION,
            NORMAL_MOB_DROP,
            RAID,
            SPECIFIC_MOB_DROP,
            UNKNOWN);

    private final String apiName;
    private final String displayName;

    ItemObtainType(String apiName, String displayName) {
        this.apiName = apiName;
        this.displayName = displayName;
    }

    public static ItemObtainType fromApiName(String apiName) {
        for (ItemObtainType source : ItemObtainType.values()) {
            if (source.apiName.equals(apiName)) return source;
        }

        return null;
    }

    public String getDisplayName() {
        return displayName;
    }
}
