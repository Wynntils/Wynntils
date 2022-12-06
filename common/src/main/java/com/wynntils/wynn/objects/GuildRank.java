/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects;

public enum GuildRank {
    Owner("Owner", 5),
    Chief("Chief", 4),
    Strategist("Strategist", 3),
    Captain("Captain", 2),
    Recruiter("Recruiter", 1),
    Recruit("Recruit", 0),
    None("None", 0);

    private final String rank;
    private final int stars;

    GuildRank(String rank, int stars) {
        this.rank = rank;
        this.stars = stars;
    }

    public static GuildRank fromStars(int stars) {
        for (GuildRank type : GuildRank.values()) {
            if (stars == type.getStars()) {
                return type;
            }
        }
        return GuildRank.None;
    }

    public String getRank() {
        return rank;
    }

    public int getStars() {
        return stars;
    }
}
