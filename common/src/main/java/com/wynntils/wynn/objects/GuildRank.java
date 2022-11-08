/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects;

public enum GuildRank {
    Owner("Owner", "★★★★★"),
    Chief("Chief", "★★★★"),
    Strategist("Strategist", "★★★"),
    Captain("Captain", "★★"),
    Recruiter("Recruiter", "★"),
    Recruit("Recruit", " "),
    None("None", "");

    private final String rank;
    private final int stars;

    GuildRank(String rank, int stars) {
        this.rank = rank;
        this.stars = stars;
    }

    public static GuildRank fromStars(int stars) {
        for (GuildRank type : values()) {
            if (stars == type.getStars()) {
                return type;
            }
        }
        return GuildRank.None;
    }

    public static GuildRank fromString(String rank) {
        for (GuildRank type : values()) {
            if (rank.equalsIgnoreCase(type.getRank())) {
                return type;
            }
        }
        return GuildRank.None;
    }

    public String getRank() {
        return rank;
    }

    public String getStars() {
        return stars;
    }
}
