/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.wynntils.core.WynntilsMod;
import com.wynntils.utils.StringUtils;
import java.util.HashMap;
import java.util.Map;

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
        private static final Map<String, String> urlMap = new HashMap<>();
        static {
            urlMap.put("DISCORD_INVITE", "https://discord.gg/SZuNem8");
            urlMap.put("DISCOVERIES", "https://api.wynntils.com/discoveries.json");
            urlMap.put("GOOGLE_TRANSLATION", "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=%s&dt=t&q=%s");
            urlMap.put("ITEM_GUESSES", "https://wynndata.tk/api/unid/data.json");
            urlMap.put("MAPS", "https://raw.githubusercontent.com/Wynntils/WynntilsWebsite-API/master/maps/maps.json");
            urlMap.put("ONLINE_PLAYERS", "https://api.wynncraft.com/public_api.php?action=onlinePlayers");
            urlMap.put("PLACES", "https://raw.githubusercontent.com/Wynntils/Reference/main/locations/places.json");
            urlMap.put("PLAYER_STATS", "https://wynncraft.com/stats/player/%s");
            urlMap.put("SERVICES", "https://raw.githubusercontent.com/Wynntils/Reference/main/locations/services.json");
            urlMap.put("UPDATE_CHECK", "https://athena.wynntils.com/version/latest/ce");
            urlMap.put("WIKI_TITLE_LOOKUP", "https://wynncraft.fandom.com/wiki/%s");
            urlMap.put("WIKI_DISCOVERY_QUERY", "https://wynncraft.gamepedia.com/api.php?action=parse&format=json&prop=wikitext&section=0&redirects=true&page=%s");
            urlMap.put("WIKI_QUEST_PAGE_QUERY", "https://wynncraft.fandom.com/index.php?title=Special:CargoExport&format=json&tables=Quests&fields=Quests._pageTitle&where=Quests.name=%s");
            urlMap.put("WYNNDATA_ITEM_LOOKUP", "https://www.wynndata.tk/i/%s");
            urlMap.put("WYNNTILS_PATREON", "https://www.patreon.com/Wynntils");
            urlMap.put("WYNNTILS_REGISTER_TOKEN", "https://account.wynntils.com/register.php?token=%s");
        }

        public static String getAthenaAuthGetPublicKey() {
            return "https://athena.wynntils.com/auth/getPublicKey";
        }

        public static String getAthenaAuthResponse() {
            return "https://athena.wynntils.com/auth/responseEncryption";
        }

        public static String getAthenaIngredientList() {
            return "https://athena.wynntils.com/cache/get/ingredientList";
        }

        public static String getAthenaItemList() {
            return "https://athena.wynntils.com/cache/get/itemList";
        }

        public static String getAthenaServerList() {
            return "https://athena.wynntils.com/cache/get/serverList";
        }

        public static String getAthenaTerritoryList() {
            return "https://athena.wynntils.com/cache/get/territoryList";
        }

        public static String getAthenaUserInfo() {
            return "https://athena.wynntils.com/user/getInfo";
        }

        public static String getDiscordInvite() {
            return urlMap.get("DISCORD_INVITE");
        }

        public static String getDiscoveries() {
            return urlMap.get("DISCOVERIES");
        }

        public static String getItemGuesses() {
            return urlMap.get("ITEM_GUESSES");
        }

        public static String getMaps() {
            return urlMap.get("MAPS");
        }

        public static String getOnlinePlayers() {
            return urlMap.get("ONLINE_PLAYERS");
        }

        public static String getPlaces() {
            return urlMap.get("PLACES");
        }

        public static String getServices() {
            return urlMap.get("SERVICES");
        }

        public static String getUpdateCheck() {
            return urlMap.get("UPDATE_CHECK");
        }

        public static String getWynntilsPatreon() {
            return urlMap.get("WYNNTILS_PATREON");
        }

        public static String createGoogleTranslation(String toLanguage, String message) {
            return String.format(urlMap.get("GOOGLE_TRANSLATION"), StringUtils.encodeUrl(toLanguage), StringUtils.encodeUrl(message));
        }

        public static String createPlayerStats(String playerName) {
            return String.format(urlMap.get("PLAYER_STATS"), StringUtils.encodeUrl(playerName));
        }

        public static String createWikiTitleLookup(String pageTitle) {
            return String.format(urlMap.get("WIKI_TITLE_LOOKUP"), StringUtils.encodeUrl(pageTitle));
        }

        public static String createWikiDiscoveryQuery(String name) {
            return String.format(urlMap.get("WIKI_DISCOVERY_QUERY"), StringUtils.encodeUrl(name));
        }

        public static String createWikiQuestPageQuery(String name) {
            return String.format(urlMap.get("WIKI_QUEST_PAGE_QUERY"), StringUtils.encodeUrl(name));
        }

        public static String createWynndataItemLookup(String unformattedName) {
            return String.format(urlMap.get("WYNNDATA_ITEM_LOOKUP"), StringUtils.encodeUrl(unformattedName));
        }

        public static String createWynntilsRegisterToken(String token) {
            return String.format(urlMap.get("WYNNTILS_REGISTER_TOKEN"), StringUtils.encodeUrl(token));
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
