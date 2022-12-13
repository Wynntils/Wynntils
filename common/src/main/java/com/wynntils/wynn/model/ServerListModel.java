/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Model;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.NetManager;
import com.wynntils.core.net.UrlId;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.objects.profiles.ServerProfile;
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

public class ServerListModel extends Model {
    private static final List<String> SERVER_TYPES = List.of("WC", "lobby", "GM", "DEV", "WAR", "HB", "YT");

    private static Map<String, ServerProfile> availableServers = new HashMap<>();

    public static void init() {
        updateServerList();
    }

    public static List<String> getWynnServerTypes() {
        return SERVER_TYPES;
    }

    public static Set<String> getServers() {
        return availableServers.keySet();
    }

    public static List<String> getServersSortedOnUptime() {
        return getServers().stream()
                .sorted(Comparator.comparing(profile -> getServer(profile).getUptime()))
                .toList();
    }

    public static List<String> getServersSortedOnNameOfType(String serverType) {
        return getServers().stream()
                .filter(server -> server.startsWith(serverType))
                .sorted((o1, o2) -> {
                    int number1 = Integer.parseInt(o1.substring(serverType.length()));
                    int number2 = Integer.parseInt(o2.substring(serverType.length()));

                    return number1 - number2;
                })
                .toList();
    }

    public static ServerProfile getServer(String worldId) {
        return availableServers.get(worldId);
    }

    public static boolean forceUpdate(int timeOutMs) {
        CompletableFuture future = updateServerList();
        try {
            future.get(timeOutMs, TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // if timeout is reached, return false
            return false;
        }
    }

    @SubscribeEvent
    public static void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() != WorldStateManager.State.HUB
                && event.getNewState() != WorldStateManager.State.CONNECTING) return;

        updateServerList();
    }

    private static CompletableFuture updateServerList() {
        CompletableFuture future = new CompletableFuture<>();

        // dataAthenaServerList is based on
        // https://api.wynncraft.com/public_api.php?action=onlinePlayers
        // but injects a firstSeen timestamp when the server was first noticed by Athena
        Download dl = NetManager.download(UrlId.DATA_ATHENA_SERVER_LIST);
        dl.handleReader(reader -> {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

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
