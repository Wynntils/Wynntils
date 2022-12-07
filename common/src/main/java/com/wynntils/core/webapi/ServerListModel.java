/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.objects.profiles.ServerProfile;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerListModel extends Model {
    private static Map<String, ServerProfile> availableServers = new HashMap<>();

    public static void init() {}

    public static synchronized void updateServers() {
        try {
            availableServers = WebManager.getServerList();
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
}
