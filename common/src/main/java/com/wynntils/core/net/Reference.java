/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.wynntils.core.WynntilsMod;

public class Reference {
    private static final String USER_AGENT = String.format(
            "Wynntils Artemis\\%s (%s) %s",
            WynntilsMod.getVersion(),
            WynntilsMod.isDevelopmentEnvironment() ? "dev" : "client",
            WynntilsMod.getModLoader());
    private static final String WYNN_API_KEY = "XRSxAkA6OXKek9Zvds5sRqZ4ZK0YcE6wRyHx5IE6wSfr";
    private static final String SPLASHES = "Move splashes to separate file!, json FTW!";

    public static String getUserAgent() {
        return USER_AGENT;
    }

    public static String getWynnApiKey() {
        return WYNN_API_KEY;
    }

    public static String getSplashes() {
        return SPLASHES;
    }

    public static class URLs {
        private static final String ATHENA = "https://athena.wynntils.com";
        private static final String DISCORD_INVITE = "https://discord.gg/SZuNem8";
        private static final String DISCOVERIES = "https://api.wynntils.com/discoveries.json";
        private static final String ITEM_GUESSES = "https://wynndata.tk/api/unid/data.json";
        private static final String ONLINE_PLAYERS = "https://api.wynncraft.com/public_api.php?action=onlinePlayers";
        private static final String WIKI_DISCOVERY_QUERY =
                "https://wynncraft.gamepedia.com/api.php?action=parse&format=json&prop=wikitext&section=0&redirects=true&page=";

        public static String getAthena() {
            return ATHENA;
        }

        public static String getDiscordInvite() {
            return DISCORD_INVITE;
        }

        public static String getDiscoveries() {
            return DISCOVERIES;
        }

        public static String getItemGuesses() {
            return ITEM_GUESSES;
        }

        public static String getOnlinePlayers() {
            return ONLINE_PLAYERS;
        }

        public static String getWikiDiscoveryQuery() {
            return WIKI_DISCOVERY_QUERY;
        }
    }
}
