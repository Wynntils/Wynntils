/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.leaderboard.type;

import com.wynntils.core.WynntilsMod;

public record LeaderboardBadge(int uOffset, int vOffset) {
    public static final int WIDTH = 19;
    public static final int HEIGHT = 18;

    public static LeaderboardBadge from(LeaderboardType leaderboardType, int standing) {
        int uOffset = leaderboardType.ordinal() * WIDTH;

        int color = 2; // just in case Athena gives a number not between 1 and 10

        if (standing >= 1 && standing <= 3) {
            // Gold
            color = 0;
        } else if (standing >= 4 && standing <= 6) {
            // Silver
            color = 1;
        } else if (standing >= 7 && standing <= 10) {
            // Bronze
            color = 2;
        } else {
            WynntilsMod.warn("Unexpected leaderboard standing: " + standing + " for " + leaderboardType);
        }

        int vOffset = color * HEIGHT;

        return new LeaderboardBadge(uOffset, vOffset);
    }
}
