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
import com.wynntils.core.net.athena.WynntilsAccountManager;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.objects.profiles.ServerProfile;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerListModel extends Model {
    private static final List<String> SERVER_TYPES = List.of("WC", "lobby", "GM", "DEV", "WAR", "HB", "YT");
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    private static Map<String, ServerProfile> availableServers = new HashMap<>();

    public static void init() {
        updateServerList(0);
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
        // timeOutMs is currently ignored; will be fixed soon
        return updateNow();
    }

    @SubscribeEvent
    public static void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() != WorldStateManager.State.HUB
                && event.getNewState() != WorldStateManager.State.CONNECTING) return;

        updateServerList(0);
    }

    private static boolean updateNow() {
        if (WebManager.apiUrls == null || !WynntilsAccountManager.isLoggedIn()) return false;
        String url = WebManager.apiUrls.get("Athena") + "/cache/get/serverList";

        try {
            URLConnection st = WebManager.generateURLRequest(url);
            InputStreamReader stInputReader = new InputStreamReader(st.getInputStream(), StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseReader(stInputReader).getAsJsonObject();

            JsonObject servers = json.getAsJsonObject("servers");
            Map<String, ServerProfile> newMap = new HashMap<>();

            long serverTime = Long.parseLong(st.getHeaderField("timestamp"));
            for (Map.Entry<String, JsonElement> entry : servers.entrySet()) {
                ServerProfile profile = WynntilsMod.GSON.fromJson(entry.getValue(), ServerProfile.class);
                profile.matchTime(serverTime);

                newMap.put(entry.getKey(), profile);
            }

            availableServers = newMap;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private static void updateServerList(int timeOutMs) {
        // Will update in background
        // timeOutMs is currently ignored; will be fixed soon
        EXECUTOR.submit(ServerListModel::updateNow);
    }
}
