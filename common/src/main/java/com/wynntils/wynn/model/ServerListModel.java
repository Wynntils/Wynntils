/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Model;
import com.wynntils.core.net.UrlManager;
import com.wynntils.core.net.api.ApiRequester;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.objects.profiles.ServerProfile;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerListModel extends Model {
    private static final Gson GSON = new Gson();
    private static Map<String, ServerProfile> availableServers = new HashMap<>();

    public static void init() {}

    private static synchronized void updateServers() {
        try {
            availableServers = getServerList();
        } catch (IOException e) {
            WynntilsMod.error("Failed to update server list", e);
        }
    }

    public static ServerProfile getServer(String worldId) {
        return availableServers.get(worldId);
    }

    @SubscribeEvent
    public static void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() != WorldStateManager.State.HUB
                && event.getNewState() != WorldStateManager.State.CONNECTING) return;

        // Run async to avoid blocking the render thread
        new Thread(ServerListModel::updateServers).start();
    }

    public static HashMap<String, ServerProfile> getServerList() throws IOException {
        URLConnection st = ApiRequester.generateURLRequest(UrlManager.getUrl(UrlManager.DATA_ATHENA_SERVER_LIST));
        InputStreamReader stInputReader = new InputStreamReader(st.getInputStream(), StandardCharsets.UTF_8);
        JsonObject json = JsonParser.parseReader(stInputReader).getAsJsonObject();

        JsonObject servers = json.getAsJsonObject("servers");
        HashMap<String, ServerProfile> result = new HashMap<>();

        long serverTime = Long.parseLong(st.getHeaderField("timestamp"));
        for (Map.Entry<String, JsonElement> entry : servers.entrySet()) {
            ServerProfile profile = GSON.fromJson(entry.getValue(), ServerProfile.class);
            profile.matchTime(serverTime);

            result.put(entry.getKey(), profile);
        }

        return result;
    }

    /**
     * Request all online players to WynnAPI
     *
     * @return a {@link HashMap} who the key is the server and the value is an array containing all
     *     players on it
     * @throws IOException thrown by URLConnection
     */
    public static Map<String, List<String>> getOnlinePlayers() throws IOException {
        String url = UrlManager.getUrl(UrlManager.API_WYNNCRAFT_ONLINE_PLAYERS);
        URLConnection st = ApiRequester.generateURLRequestWithWynnApiKey(url);
        InputStreamReader stInputReader = new InputStreamReader(st.getInputStream(), StandardCharsets.UTF_8);
        JsonObject main = JsonParser.parseReader(stInputReader).getAsJsonObject();

        if (!main.has("message")) {
            main.remove("request");

            Type type = new TypeToken<LinkedHashMap<String, ArrayList<String>>>() {}.getType();

            return GSON.fromJson(main, type);
        } else {
            return new HashMap<>();
        }
    }
}
