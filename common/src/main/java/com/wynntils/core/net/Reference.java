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
            urlMap.put("discordInvite", "https://discord.gg/SZuNem8");
            urlMap.put("discoveries", "https://api.wynntils.com/discoveries.json");
            urlMap.put("googleTranslation", "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=%s&dt=t&q=%s");
            urlMap.put("itemGuesses", "https://wynndata.tk/api/unid/data.json");
            urlMap.put("maps", "https://raw.githubusercontent.com/Wynntils/WynntilsWebsite-API/master/maps/maps.json");
            urlMap.put("onlinePlayers", "https://api.wynncraft.com/public_api.php?action=onlinePlayers");
            urlMap.put("places", "https://raw.githubusercontent.com/Wynntils/Reference/main/locations/places.json");
            urlMap.put("playerStats", "https://wynncraft.com/stats/player/%s");
            urlMap.put("services", "https://raw.githubusercontent.com/Wynntils/Reference/main/locations/services.json");
            urlMap.put("updateCheck", "https://athena.wynntils.com/version/latest/ce");
            urlMap.put("wikiTitleLookup", "https://wynncraft.fandom.com/wiki/%s");
            urlMap.put("wikiDiscoveryQuery", "https://wynncraft.gamepedia.com/api.php?action=parse&format=json&prop=wikitext&section=0&redirects=true&page=%s");
            urlMap.put("wikiQuestPageQuery", "https://wynncraft.fandom.com/index.php?title=Special:CargoExport&format=json&tables=Quests&fields=Quests._pageTitle&where=Quests.name=%s");
            urlMap.put("wynndataItemLookup", "https://www.wynndata.tk/i/%s");
            urlMap.put("wynntilsPatreon", "https://www.patreon.com/Wynntils");
            urlMap.put("wynntilsRegisterToken", "https://account.wynntils.com/register.php?token=%s");
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
            return urlMap.get("discordInvite");
        }

        public static String getDiscoveries() {
            return urlMap.get("discoveries");
        }

        public static String getItemGuesses() {
            return urlMap.get("itemGuesses");
        }

        public static String getMaps() {
            return urlMap.get("maps");
        }

        public static String getOnlinePlayers() {
            return urlMap.get("onlinePlayers");
        }

        public static String getPlaces() {
            return urlMap.get("places");
        }

        public static String getServices() {
            return urlMap.get("services");
        }

        public static String getUpdateCheck() {
            return urlMap.get("updateCheck");
        }

        public static String getWynntilsPatreon() {
            return urlMap.get("wynntilsPatreon");
        }

        public static String createGoogleTranslation(String toLanguage, String message) {
            return String.format(urlMap.get("googleTranslation"), StringUtils.encodeUrl(toLanguage), StringUtils.encodeUrl(message));
        }

        public static String createPlayerStats(String playerName) {
            return String.format(urlMap.get("playerStats"), StringUtils.encodeUrl(playerName));
        }

        public static String createWikiTitleLookup(String pageTitle) {
            return String.format(urlMap.get("wikiTitleLookup"), StringUtils.encodeUrl(pageTitle));
        }

        public static String createWikiDiscoveryQuery(String name) {
            return String.format(urlMap.get("wikiDiscoveryQuery"), StringUtils.encodeUrl(name));
        }

        public static String createWikiQuestPageQuery(String name) {
            return String.format(urlMap.get("wikiQuestPageQuery"), StringUtils.encodeUrl(name));
        }

        public static String createWynndataItemLookup(String unformattedName) {
            return String.format(urlMap.get("wynndataItemLookup"), StringUtils.encodeUrl(unformattedName));
        }

        public static String createWynntilsRegisterToken(String token) {
            return String.format(urlMap.get("wynntilsRegisterToken"), StringUtils.encodeUrl(token));
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
