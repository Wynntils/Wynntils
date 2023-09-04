/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.leaderboard.type;

import com.wynntils.models.profession.type.ProfessionType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record LeaderboardEntry(String name, int timePlayed, Map<String, Integer> ranks) {
    public List<LeaderboardBadge> getBadges() {
        List<LeaderboardBadge> list = new ArrayList<>();

        for (Map.Entry<String, Integer> rank : ranks.entrySet()) {
            ProfessionType type = ProfessionType.valueOf(rank.getKey());
            int uOffset = type.ordinal() * LeaderboardBadge.WIDTH;

            int standing = rank.getValue();
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

            list.add(new LeaderboardBadge(uOffset, vOffset));
        }

        return list;
    }
}
