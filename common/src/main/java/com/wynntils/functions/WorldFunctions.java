/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.functions.Function;
import com.wynntils.models.worlds.profile.ServerProfile;
import java.util.List;

public class WorldFunctions {
    public static class CurrentWorldFunction extends Function<String> {
        private static final String NO_DATA = "<unknown>";
        private static final String NO_WORLD = "<not on world>";

        @Override
        public String getValue(String argument) {
            if (!Models.WorldState.onWorld()) {
                return NO_WORLD;
            }

            String currentWorldName = Models.WorldState.getCurrentWorldName();
            return currentWorldName.isEmpty() ? NO_DATA : currentWorldName;
        }

        @Override
        public List<String> getAliases() {
            return List.of("world");
        }
    }

    public static class CurrentWorldUptimeFunction extends Function<String> {
        private static final String NO_DATA = "<unknown>";
        private static final String NO_WORLD = "<not on world>";

        @Override
        public String getValue(String argument) {
            if (!Models.WorldState.onWorld()) {
                return NO_WORLD;
            }

            String currentWorldName = Models.WorldState.getCurrentWorldName();

            ServerProfile server = Models.ServerList.getServer(currentWorldName);

            if (server == null) {
                return NO_DATA;
            }

            return server.getUptime();
        }

        @Override
        public List<String> getAliases() {
            return List.of("world_uptime", "uptime");
        }
    }
}
