/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.type;

public enum GuildRank {
    RECRUIT("Recruit", ""),
    RECRUITER("Recruiter", "★"),
    CAPTAIN("Captain", "★★"),
    STRATEGIST("Strategist", "★★★"),
    CHIEF("Chief", "★★★★"),
    OWNER("Owner", "★★★★★");

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
}
