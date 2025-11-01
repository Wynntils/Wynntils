/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.leaderboard;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
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
    private Map<UUID, List<LeaderboardBadge>> playerLeaderboards = new HashMap<>();
    private Map<String, List<LeaderboardBadge>> guildLeaderboards = new HashMap<>();

    public LeaderboardService() {
        super(List.of());
    }

    @Override
    public void reloadData() {
        updateLeaderboards();
    }

    public List<LeaderboardBadge> getPlayerBadges(UUID id) {
        return playerLeaderboards.getOrDefault(id, List.of());
    }

    private void updateLeaderboards() {
        playerLeaderboards = new HashMap<>();
        guildLeaderboards = new HashMap<>();

        ApiResponse apiResponse = Managers.Net.callApi(UrlId.DATA_ATHENA_LEADERBOARD);
        apiResponse.handleJsonObject(json -> {
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                LeaderboardType type = LeaderboardType.fromKey(entry.getKey());

                if (type == null) {
                    WynntilsMod.warn("Unknown leaderboard type: " + entry.getKey());
                    continue;
                }

                JsonObject leaderboard = entry.getValue().getAsJsonObject();

                for (Map.Entry<String, JsonElement> rank : leaderboard.entrySet()) {
                    String value = rank.getValue().getAsString();

                    if (value.equals("redacted")) continue;

                    if (type.isGuildLeaderboard()) {
                        List<LeaderboardBadge> badges = guildLeaderboards.getOrDefault(value, new ArrayList<>());

                        badges.add(LeaderboardBadge.from(type, Integer.parseInt(rank.getKey())));
                        guildLeaderboards.put(value, badges);
                    } else {
                        UUID uuid = UUID.fromString(value);
                        List<LeaderboardBadge> badges = playerLeaderboards.getOrDefault(uuid, new ArrayList<>());

                        badges.add(LeaderboardBadge.from(type, Integer.parseInt(rank.getKey())));
                        playerLeaderboards.put(uuid, badges);
                    }
                }
            }
        });
    }
}
