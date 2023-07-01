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
    private final String stars;

    GuildRank(String name) {
        this.name = name;

        switch (this.name) {
            case "Owner" -> stars = "★★★★★";
            case "Chief" -> stars = "★★★★";
            case "Strategist" -> stars = "★★★";
            case "Captain" -> stars = "★★";
            case "Recruiter" -> stars = "★";
            default -> stars = "";
        }
    }

    public String getName() {
        return name;
    }

    public String getStars() {
        return stars;
    }
}
