/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.utils.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
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

    public static final class UrlInfo {
        public String getUrl() {
            return url;
        }

        String id;
        String url;
        String md5;
        String encoding;
        Integer numArguments;
    }

    public static final class URLs {
        private static final Gson GSON = new Gson();

        private static Map<String, UrlInfo> urlMap;

        static {
            init();
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
            return String.format(
                    urlMap.get("googleTranslation").getUrl(),
                    StringUtils.encodeUrl(toLanguage),
                    StringUtils.encodeUrl(message));
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

        public static void init() {
            urlMap = new HashMap<>();
            try {
                InputStream inputStream = WynntilsMod.getModResourceAsStream("urls.json");
                byte[] data = inputStream.readAllBytes();
                String json = new String(data, StandardCharsets.UTF_8);
                Type type = new TypeToken<List<UrlInfo>>() {}.getType();
                List<UrlInfo> urlInfos = GSON.fromJson(json, type);
                for (UrlInfo urlInfo : urlInfos) {
                    urlMap.put(urlInfo.id, urlInfo);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
