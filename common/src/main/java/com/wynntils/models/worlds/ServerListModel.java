/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.profile.ServerProfile;
import com.wynntils.models.worlds.type.WorldState;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ServerListModel extends Model {
    private static final List<String> SERVER_TYPES = List.of("WC", "lobby", "GM", "DEV", "WAR", "HB", "YT");

    private Map<String, ServerProfile> availableServers = new HashMap<>();

    public ServerListModel() {
        super(List.of());

        updateServerList();
    }

    public List<String> getWynnServerTypes() {
        return SERVER_TYPES;
    }

    public Set<String> getServers() {
        return availableServers.keySet();
    }

    public List<String> getServersSortedOnUptime() {
        return getServers().stream()
                .sorted(Comparator.comparing(profile -> getServer(profile).getUptime()))
                .toList();
    }

    public List<String> getServersSortedOnNameOfType(String serverType) {
        return getServers().stream()
                .filter(server -> server.startsWith(serverType))
                .sorted((o1, o2) -> {
                    int number1 = Integer.parseInt(o1.substring(serverType.length()));
                    int number2 = Integer.parseInt(o2.substring(serverType.length()));

                    return number1 - number2;
                })
                .toList();
    }

    public ServerProfile getServer(String worldId) {
        return availableServers.get(worldId);
    }

    public boolean forceUpdate(int timeOutMs) {
        CompletableFuture<Boolean> future = updateServerList();
        try {
            future.get(timeOutMs, TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // if timeout is reached, return false
            return false;
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() != WorldState.HUB && event.getNewState() != WorldState.CONNECTING) return;

        updateServerList();
    }

    private CompletableFuture<Boolean> updateServerList() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // dataAthenaServerList is based on
        // https://api.wynncraft.com/public_api.php?action=onlinePlayers
        // but injects a firstSeen timestamp when the server was first noticed by Athena

        Download dl = Managers.Net.download(UrlId.DATA_ATHENA_SERVER_LIST);
        dl.handleJsonObject(json -> {
            JsonObject servers = json.getAsJsonObject("servers");
            Map<String, ServerProfile> newMap = new HashMap<>();

            long serverTime = dl.getResponseTimestamp();
            for (Map.Entry<String, JsonElement> entry : servers.entrySet()) {
                ServerProfile profile = WynntilsMod.GSON.fromJson(entry.getValue(), ServerProfile.class);
                profile.matchTime(serverTime);

                newMap.put(entry.getKey(), profile);
            }

            availableServers = newMap;
            future.complete(true);
        });
        return future;
    }
}
