/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Model;
import com.wynntils.core.webapi.account.WynntilsAccount;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import com.wynntils.wynn.netresources.profiles.ServerProfile;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerListModel extends Model {
    private static Map<String, ServerProfile> availableServers = new HashMap<>();

    public static void init() {}

    public static synchronized void updateServers() {
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
        if (WebManager.apiUrls == null || !WynntilsAccount.isAthenaOnline()) return new HashMap<>();
        String url = WebManager.apiUrls.get("Athena") + "/cache/get/serverList";

        URLConnection st = WebManager.generateURLRequest(url);
        InputStreamReader stInputReader = new InputStreamReader(st.getInputStream(), StandardCharsets.UTF_8);
        JsonObject json = JsonParser.parseReader(stInputReader).getAsJsonObject();

        JsonObject servers = json.getAsJsonObject("servers");
        HashMap<String, ServerProfile> result = new HashMap<>();

        long serverTime = Long.parseLong(st.getHeaderField("timestamp"));
        for (Map.Entry<String, JsonElement> entry : servers.entrySet()) {
            ServerProfile profile = WebManager.gson.fromJson(entry.getValue(), ServerProfile.class);
            profile.matchTime(serverTime);

            result.put(entry.getKey(), profile);
        }

        return result;
    }
}
