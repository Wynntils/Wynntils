/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.leaderboard.type;

public enum LeaderboardType {
    // Gathering Professions
    WOODCUTTING("woodcuttingLevel"),
    MINING("miningLevel"),
    FISHING("fishingLevel"),
    FARMING("farmingLevel"),
    // Crafting Professions
    ALCHEMISM("alchemismLevel"),
    ARMOURING("armouringLevel"),
    COOKING("cookingLevel"),
    JEWELING("jewelingLevel"),
    SCRIBING("scribingLevel"),
    TAILORING("tailoringLevel"),
    WEAPONSMITHING("weaponsmithingLevel"),
    WOODWORKING("woodworkingLevel"),
    // Total Professions
    PROFESSIONS_GLOBAL("professionsGlobalLevel"),
    PROFESSIONS_SOLO("professionsSoloLevel"),
    // Total Combat
    COMBAT_GLOBAL("combatGlobalLevel"),
    COMBAT_SOLO("combatSoloLevel"),
    // Total Overall
    TOTAL_GLOBAL("totalGlobalLevel"),
    TOTAL_SOLO("totalSoloLevel"),
    // Content Completion
    CONTENT_GLOBAL("globalPlayerContent"),
    CONTENT_SOLO("playerContent"),
    // Raids
    NOG_SCORE("grootslangSrPlayers"),
    NOG_COMPLETION("grootslangCompletion"),
    NOL_SCORE("orphionSrPlayers"),
    NOL_COMPLETION("orphionCompletion"),
    TCC_SCORE("colossusSrPlayers"),
    TCC_COMPLETION("colossusCompletion"),
    TNA_SCORE("namelessSrPlayers"),
    TNA_COMPLETION("namelessCompletion"),
    // Solo Gamemodes
    IRONMAN("ironmanContent"),
    ULTIMATE_IRONMAN("ultimateIronmanContent"),
    HARDCORE("hardcoreContent"),
    CRAFTSMAN("craftsmanContent"),
    HUNTED("huntedContent"),
    // Multi Gamemodes
    HUIC("huicContent"),
    HUICH("huichContent"),
    HICH("hichContent"),
    HIC("hicContent"),
    // Misc
    WARS("warsCompletion"),
    HARDCORE_LEGACY_LEVEL("hardcoreLegacyLevel"), // Should move to Solo Gamemodes when a badge is added
    // Guild
    GUILD_LEVEL("guildLevel", true),
    GUILD_TERRITORIES("guildTerritories", true),
    GUILD_WARS("guildWars", true),
    GUILD_NOG("grootslangSrGuilds", true),
    GUILD_NOL("orphionSrGuilds", true),
    GUILD_TCC("colossusSrGuilds", true),
    GUILD_TNA("namelessSrGuilds", true);

    private final String key;
    private final boolean guildLeaderboard;

    LeaderboardType(String key, boolean guildLeaderboard) {
        this.key = key;
        this.guildLeaderboard = guildLeaderboard;
    }

    LeaderboardType(String key) {
        this(key, false);
    }

    public String getKey() {
        return key;
    }

    public static LeaderboardType fromKey(String key) {
        for (LeaderboardType type : LeaderboardType.values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }

        return null;
    }

    public boolean isGuildLeaderboard() {
        return guildLeaderboard;
    }
}
