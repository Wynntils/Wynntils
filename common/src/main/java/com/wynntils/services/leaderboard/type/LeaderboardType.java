/*
 * Copyright © Wynntils 2024.
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
    NOG_SCORE("nogSrPlayers"),
    NOG_COMPLETION("nogCompletion"),
    NOL_SCORE("nolSrPlayers"),
    NOL_COMPLETION("nolCompletion"),
    TCC_SCORE("tccSrPlayers"),
    TCC_COMPLETION("tccCompletion"),
    TNA_SCORE("tnaSrPlayers"),
    TNA_COMPLETION("tnaCompletion"),
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
    WARS("warsCompletion");

    private final String key;

    LeaderboardType(String key) {
        this.key = key;
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
}
