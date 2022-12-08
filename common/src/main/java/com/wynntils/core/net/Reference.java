/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.wynntils.core.WynntilsMod;
import com.wynntils.utils.StringUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    public static final class UrlInfo {
        public String getUrl() {
            return url;
        }

        String url;
        Optional<String> md5;
        Optional<String> encoding;
        Optional<Integer> numArguments;

        public UrlInfo(String url) {
            this.url = url;
        }
    }

    public static final class URLs {
        private static final Map<String, UrlInfo> urlMap = new HashMap<>();
        static {
            urlMap.put("athenaAuthGetPublicKey", new UrlInfo("https://athena.wynntils.com/auth/getPublicKey"));
            urlMap.put("athenaAuthResponse", new UrlInfo("https://athena.wynntils.com/auth/responseEncryption"));
            urlMap.put("athenaIngredientList", new UrlInfo("https://athena.wynntils.com/cache/get/ingredientList"));
            urlMap.put("athenaItemList", new UrlInfo("https://athena.wynntils.com/cache/get/itemList"));
            urlMap.put("athenaServerList", new UrlInfo("https://athena.wynntils.com/cache/get/serverList"));
            urlMap.put("athenaTerritoryList", new UrlInfo("https://athena.wynntils.com/cache/get/territoryList"));
            urlMap.put("athenaUserInfo", new UrlInfo("https://athena.wynntils.com/user/getInfo"));
            urlMap.put("discordInvite", new UrlInfo("https://discord.gg/SZuNem8"));
            urlMap.put("discoveries", new UrlInfo("https://api.wynntils.com/discoveries.json"));
            urlMap.put("googleTranslation", new UrlInfo("https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=%s&dt=t&q=%s"));
            urlMap.put("itemGuesses", new UrlInfo("https://wynndata.tk/api/unid/data.json"));
            urlMap.put("maps", new UrlInfo("https://raw.githubusercontent.com/Wynntils/WynntilsWebsite-API/master/maps/maps.json"));
            urlMap.put("onlinePlayers", new UrlInfo("https://api.wynncraft.com/public_api.php?action=onlinePlayers"));
            urlMap.put("places", new UrlInfo("https://raw.githubusercontent.com/Wynntils/Reference/main/locations/places.json"));
            urlMap.put("playerStats", new UrlInfo("https://wynncraft.com/stats/player/%s"));
            urlMap.put("services", new UrlInfo("https://raw.githubusercontent.com/Wynntils/Reference/main/locations/services.json"));
            urlMap.put("updateCheck", new UrlInfo("https://athena.wynntils.com/version/latest/ce"));
            urlMap.put("wikiDiscoveryQuery", new UrlInfo("https://wynncraft.gamepedia.com/api.php?action=parse&format=json&prop=wikitext&section=0&redirects=true&page=%s"));
            urlMap.put("wikiQuestPageQuery", new UrlInfo("https://wynncraft.fandom.com/index.php?title=Special:CargoExport&format=json&tables=Quests&fields=Quests._pageTitle&where=Quests.name=%s"));
            urlMap.put("wikiTitleLookup", new UrlInfo("https://wynncraft.fandom.com/wiki/%s"));
            urlMap.put("wynndataItemLookup", new UrlInfo("https://www.wynndata.tk/i/%s"));
            urlMap.put("wynntilsPatreon", new UrlInfo("https://www.patreon.com/Wynntils"));
            urlMap.put("wynntilsRegisterToken", new UrlInfo("https://account.wynntils.com/register.php?token=%s"));
        }

        public static String getAthenaAuthGetPublicKey() {
            return urlMap.get("athenaAuthGetPublicKey").getUrl();
        }

        public static String getAthenaAuthResponse() {
            return urlMap.get("athenaAuthResponse").getUrl();
        }

        public static String getAthenaIngredientList() {
            return urlMap.get("athenaIngredientList").getUrl();
        }

        public static String getAthenaItemList() {
            return urlMap.get("athenaItemList").getUrl();
        }

        public static String getAthenaServerList() {
            return urlMap.get("athenaServerList").getUrl();
        }

        public static String getAthenaTerritoryList() {
            return urlMap.get("athenaTerritoryList").getUrl();
        }

        public static String getAthenaUserInfo() {
            return urlMap.get("athenaUserInfo").getUrl();
        }

        public static String getDiscordInvite() {
            return urlMap.get("discordInvite").getUrl();
        }

        public static String getDiscoveries() {
            return urlMap.get("discoveries").getUrl();
        }

        public static String getItemGuesses() {
            return urlMap.get("itemGuesses").getUrl();
        }

        public static String getMaps() {
            return urlMap.get("maps").getUrl();
        }

        public static String getOnlinePlayers() {
            return urlMap.get("onlinePlayers").getUrl();
        }

        public static String getPlaces() {
            return urlMap.get("places").getUrl();
        }

        public static String getServices() {
            return urlMap.get("services").getUrl();
        }

        public static String getUpdateCheck() {
            return urlMap.get("updateCheck").getUrl();
        }

        public static String getWynntilsPatreon() {
            return urlMap.get("wynntilsPatreon").getUrl();
        }

        public static String createGoogleTranslation(String toLanguage, String message) {
            return String.format(urlMap.get("googleTranslation").getUrl(), StringUtils.encodeUrl(toLanguage), StringUtils.encodeUrl(message));
        }

        public static String createPlayerStats(String playerName) {
            return String.format(urlMap.get("playerStats").getUrl(), StringUtils.encodeUrl(playerName));
        }

        public static String createWikiTitleLookup(String pageTitle) {
            return String.format(urlMap.get("wikiTitleLookup").getUrl(), StringUtils.encodeUrl(pageTitle));
        }

        public static String createWikiDiscoveryQuery(String name) {
            return String.format(urlMap.get("wikiDiscoveryQuery").getUrl(), StringUtils.encodeUrl(name));
        }

        public static String createWikiQuestPageQuery(String name) {
            return String.format(urlMap.get("wikiQuestPageQuery").getUrl(), StringUtils.encodeUrl(name));
        }

        public static String createWynndataItemLookup(String unformattedName) {
            return String.format(urlMap.get("wynndataItemLookup").getUrl(), StringUtils.encodeUrl(unformattedName));
        }

        public static String createWynntilsRegisterToken(String token) {
            return String.format(urlMap.get("wynntilsRegisterToken").getUrl(), StringUtils.encodeUrl(token));
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
