/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.type;

public enum GuildRank {
    RECRUIT("Recruit"),
    RECRUITER("Recruiter"),
    CAPTAIN("Captain"),
    STRATEGIST("Strategist"),
    CHIEF("Chief"),
    OWNER("Owner");

    private final String name;

    GuildRank(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
