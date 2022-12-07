/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.wynntils.core.WynntilsMod;

public class Reference {
    private static final String WYNN_API_KEY = "XRSxAkA6OXKek9Zvds5sRqZ4ZK0YcE6wRyHx5IE6wSfr";
    private static final String USER_AGENT = String.format(
            "Wynntils Artemis\\%s (%s) %s",
            WynntilsMod.getVersion(),
            WynntilsMod.isDevelopmentEnvironment() ? "dev" : "client",
            WynntilsMod.getModLoader());

    public static String getUserAgent() {
        return USER_AGENT;
    }

    public static String getWynnApiKey() {
        return WYNN_API_KEY;
    }

    public static class URLs {
        private static final String ATHENA = "https://athena.wynntils.com";
        private static final String DISCORD_INVITE = "https://discord.gg/SZuNem8";
        private static final String DISCOVERIES = "https://api.wynntils.com/discoveries.json";
        private static final String ITEM_GUESSES = "https://wynndata.tk/api/unid/data.json";
        private static final String MAPS =
                "https://raw.githubusercontent.com/Wynntils/WynntilsWebsite-API/master/maps/maps.json";
        private static final String ONLINE_PLAYERS = "https://api.wynncraft.com/public_api.php?action=onlinePlayers";
        private static final String PLACES =
                "https://raw.githubusercontent.com/Wynntils/Reference/main/locations/places.json";
        private static final String SERVICES =
                "https://raw.githubusercontent.com/Wynntils/Reference/main/locations/services.json";
        private static final String WIKI_DISCOVERY_QUERY =
                "https://wynncraft.gamepedia.com/api.php?action=parse&format=json&prop=wikitext&section=0&redirects=true&page=";

        private static String getAthena() {
            return ATHENA;
        }

        public static String getAthenaAuthGetPublicKey() {
            return getAthena() + "/auth/getPublicKey";
        }

        public static String getAthenaAuthResponse() {
            return getAthena() + "/auth/responseEncryption";
        }

        public static String getAthenaIngredientList() {
            return getAthena() + "/cache/get/ingredientList";
        }

        public static String getAthenaItemList() {
            return getAthena() + "/cache/get/itemList";
        }

        public static String getAthenaServerList() {
            return getAthena() + "/cache/get/serverList";
        }

        public static String getAthenaTerritoryList() {
            return getAthena() + "/cache/get/territoryList";
        }

        public static String getAthenaUserInfo() {
            return getAthena() + "/user/getInfo";
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

        public static String getMaps() {
            return MAPS;
        }

        public static String getOnlinePlayers() {
            return ONLINE_PLAYERS;
        }

        public static String getPlaces() {
            return PLACES;
        }

        public static String getServices() {
            return SERVICES;
        }

        public static String getWikiDiscoveryQuery() {
            return WIKI_DISCOVERY_QUERY;
        }

        public static void reloadUrls() {
            // FIXME: Not implemented yet
        }
    }
}
