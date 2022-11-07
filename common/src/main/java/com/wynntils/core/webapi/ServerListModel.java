/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi;

import com.wynntils.core.managers.Model;
import com.wynntils.core.webapi.profiles.ServerProfile;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import java.util.HashMap;
import java.util.Map;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerListModel extends Model {
    private static Map<String, ServerProfile> availableServers = new HashMap<>();

    public static void init() {}

    public static synchronized void updateServers() {
        WebManager.getServerList((servers) -> availableServers = servers);
    }

    public static ServerProfile getServer(String worldId) {
        return availableServers.get(worldId);
    }

    public static Map<String, ServerProfile> getAvailableServers() {
        return availableServers;
    }

    @SubscribeEvent
    public static void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() != WorldStateManager.State.HUB
                && event.getNewState() != WorldStateManager.State.CONNECTING) return;

        updateServers();
    }
}
