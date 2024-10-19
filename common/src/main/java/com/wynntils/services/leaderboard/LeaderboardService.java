/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.leaderboard;

import com.google.gson.JsonElement;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Service;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import com.wynntils.services.leaderboard.type.LeaderboardBadge;
import com.wynntils.services.leaderboard.type.LeaderboardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LeaderboardService extends Service {
    private Map<UUID, List<LeaderboardBadge>> leaderboard = new HashMap<>();

    public LeaderboardService() {
        super(List.of());
    }

    @Override
    public void reloadData() {
        updateLeaderboards();
    }

    public List<LeaderboardBadge> getBadges(UUID id) {
        return leaderboard.getOrDefault(id, List.of());
    }

    private void updateLeaderboards() {
        leaderboard = new HashMap<>();

        for (LeaderboardType type : LeaderboardType.values()) {
            ApiResponse apiResponse =
                    Managers.Net.callApi(UrlId.DATA_WYNNCRAFT_LEADERBOARD, Map.of("type", type.getKey()));
            apiResponse.handleJsonObject(json -> {
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    UUID uuid = UUID.fromString(
                            entry.getValue().getAsJsonObject().get("uuid").getAsString());
                    List<LeaderboardBadge> badges = leaderboard.getOrDefault(uuid, new ArrayList<>());

                    badges.add(LeaderboardBadge.from(type, Integer.parseInt(entry.getKey())));
                    leaderboard.put(uuid, badges);
                }
            });
        }
    }
}
