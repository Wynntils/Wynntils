/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.type;

public enum GuildRank {
    OWNER("Owner", "★★★★★"),
    CHIEF("Chief", "★★★★"),
    STRATEGIST("Strategist", "★★★"),
    CAPTAIN("Captain", "★★"),
    RECRUITER("Recruiter", "★"),
    RECRUIT("Recruit", "");

    private final String name;
    private final String stars;

    GuildRank(String name, String stars) {
        this.name = name;
        this.stars = stars;
    }

    public String getName() {
        return name;
    }

    public String getGuildDescription() {
        if (this == RECRUIT) {
            return name;
        } else {
            return name + " (" + stars + ")";
        }
    }

    public static GuildRank fromName(String string) {
        for (GuildRank rank : values()) {
            if (rank.getName().equalsIgnoreCase(string)) {
                return rank;
            }
        }

        return null;
    }
}
