/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.leaderboard.type;

import com.wynntils.models.profession.type.ProfessionType;

public record LeaderboardBadge(int uOffset, int vOffset) {
    public static final int WIDTH = 19;
    public static final int HEIGHT = 17;

    public static LeaderboardBadge from(String profession, int standing) {
        int uOffset;
        if (profession.equals("OVERALL")) {
            uOffset = 12 * LeaderboardBadge.WIDTH;
        } else {
            ProfessionType type = ProfessionType.valueOf(profession);
            uOffset = type.ordinal() * LeaderboardBadge.WIDTH;
        }

        int color = 2; // just in case Athena gives a number not between 1 and 9

        if (standing >= 1 && standing <= 3) {
            color = 0;
        }
        if (standing >= 4 && standing <= 6) {
            color = 1;
        }
        if (standing >= 7 && standing <= 9) {
            color = 2;
        }

        int vOffset = color * LeaderboardBadge.HEIGHT;

        return new LeaderboardBadge(uOffset, vOffset);
    }
}
