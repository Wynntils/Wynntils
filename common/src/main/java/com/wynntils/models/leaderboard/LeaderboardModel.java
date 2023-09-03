/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.leaderboard;

import com.google.gson.JsonElement;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.leaderboard.type.LeaderboardEntry;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LeaderboardModel extends Model {
    private Map<UUID, LeaderboardEntry> leaderboard = new HashMap<>();

    public LeaderboardModel() {
        super(List.of());

        updateLeaderboard();
    }

    public LeaderboardEntry getEntry(UUID id) {
        return leaderboard.get(id);
    }

    // Somewhat arbitrary
    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() != WorldState.HUB && event.getNewState() != WorldState.CONNECTING) return;

        updateLeaderboard();
    }

    private void updateLeaderboard() {
        Download dl = Managers.Net.download(UrlId.DATA_ATHENA_LEADERBOARD);
        dl.handleJsonObject(json -> {
            Map<UUID, LeaderboardEntry> map = new HashMap<>();

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                UUID id = UUID.fromString(entry.getKey());
                LeaderboardEntry leaderboardEntry = WynntilsMod.GSON.fromJson(entry.getValue(), LeaderboardEntry.class);

                map.put(id, leaderboardEntry);
            }

            leaderboard = map;
        });
    }
}
