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
    private final String stars;

    GuildRank(String rank, String stars) {
        this.rank = rank;
        this.stars = stars;
    }

    public static GuildRank fromInt(int stars) {
        for (GuildRank type : values()) {
            if (stars == type.getStars().length()) {
                return type;
            }
        }
        return GuildRank.None;
    }

    public static GuildRank fromStars(String stars) {
        for (GuildRank type : values()) {
            if (stars.equalsIgnoreCase(type.getStars())) {
                return type;
            }
        }
        return GuildRank.Recruit;
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
