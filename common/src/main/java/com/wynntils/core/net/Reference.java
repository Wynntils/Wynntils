/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.wynntils.core.WynntilsMod;
import com.wynntils.utils.StringUtils;

public final class Reference {
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

    public static final class URLs {
        private static final String ATHENA_BASE = "https://athena.wynntils.com";
        private static final String DISCORD_INVITE = "https://discord.gg/SZuNem8";
        private static final String DISCOVERIES = "https://api.wynntils.com/discoveries.json";
        private static final String GOOGLE_TRANSLATION =
                "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=%s&dt=t&q=%s";
        private static final String ITEM_GUESSES = "https://wynndata.tk/api/unid/data.json";
        private static final String MAPS =
                "https://raw.githubusercontent.com/Wynntils/WynntilsWebsite-API/master/maps/maps.json";
        private static final String ONLINE_PLAYERS = "https://api.wynncraft.com/public_api.php?action=onlinePlayers";
        private static final String PLACES =
                "https://raw.githubusercontent.com/Wynntils/Reference/main/locations/places.json";
        private static final String PLAYER_STATS = "https://wynncraft.com/stats/player/%s";
        private static final String SERVICES =
                "https://raw.githubusercontent.com/Wynntils/Reference/main/locations/services.json";
        private static final String UPDATE_CHECK = "https://athena.wynntils.com/version/latest/ce";
        private static final String WIKI_TITLE_LOOKUP = "https://wynncraft.fandom.com/wiki/%s";
        private static final String WIKI_DISCOVERY_QUERY =
                "https://wynncraft.gamepedia.com/api.php?action=parse&format=json&prop=wikitext&section=0&redirects=true&page=%s";
        private static final String WIKI_QUEST_PAGE_QUERY =
                "https://wynncraft.fandom.com/index.php?title=Special:CargoExport&format=json&tables=Quests&fields=Quests._pageTitle&where=Quests.name=%s";
        private static final String WYNNDATA_ITEM_LOOKUP = "https://www.wynndata.tk/i/%s";
        private static final String WYNNTILS_PATREON = "https://www.patreon.com/Wynntils";
        private static final String WYNNTILS_REGISTER_TOKEN = "https://account.wynntils.com/register.php?token=%s";

        private static String getAthenaBase() {
            return ATHENA_BASE;
        }

        public static String getAthenaAuthGetPublicKey() {
            return getAthenaBase() + "/auth/getPublicKey";
        }

        public static String getAthenaAuthResponse() {
            return getAthenaBase() + "/auth/responseEncryption";
        }

        public static String getAthenaIngredientList() {
            return getAthenaBase() + "/cache/get/ingredientList";
        }

        public static String getAthenaItemList() {
            return getAthenaBase() + "/cache/get/itemList";
        }

        public static String getAthenaServerList() {
            return getAthenaBase() + "/cache/get/serverList";
        }

        public static String getAthenaTerritoryList() {
            return getAthenaBase() + "/cache/get/territoryList";
        }

        public static String getAthenaUserInfo() {
            return getAthenaBase() + "/user/getInfo";
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

        public static String getUpdateCheck() {
            return UPDATE_CHECK;
        }

        public static String getWynntilsPatreon() {
            return WYNNTILS_PATREON;
        }

        public static String createGoogleTranslation(String toLanguage, String message) {
            return String.format(GOOGLE_TRANSLATION, StringUtils.encodeUrl(toLanguage), StringUtils.encodeUrl(message));
        }

        public static String createPlayerStats(String playerName) {
            return String.format(PLAYER_STATS, StringUtils.encodeUrl(playerName));
        }

        public static String createWikiTitleLookup(String pageTitle) {
            return String.format(WIKI_TITLE_LOOKUP, StringUtils.encodeUrl(pageTitle));
        }

        public static String createWikiDiscoveryQuery(String name) {
            return String.format(WIKI_DISCOVERY_QUERY, StringUtils.encodeUrl(name));
        }

        public static String createWikiQuestPageQuery(String name) {
            return String.format(WIKI_QUEST_PAGE_QUERY, StringUtils.encodeUrl(name));
        }

        public static String createWynndataItemLookup(String unformattedName) {
            return String.format(WYNNDATA_ITEM_LOOKUP, StringUtils.encodeUrl(unformattedName));
        }

        public static String createWynntilsRegisterToken(String token) {
            return String.format(WYNNTILS_REGISTER_TOKEN, StringUtils.encodeUrl(token));
        }

        public static void reloadUrls() {
            // FIXME: Not implemented yet
        }

        public static String encodeForCargoQuery(String name) {
            return "'" + name.replace("'", "\\'") + "'";
        }

        public static String encodeForWikiTitle(String pageTitle) {
            return pageTitle.replace(" ", "_");
        }
    }
}
