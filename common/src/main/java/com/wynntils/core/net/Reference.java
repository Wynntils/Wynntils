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

        public static String getUrl(String urlKey) {
            return getUrl(urlKey);
        }

        public static String getAthenaAuthGetPublicKey() {
            return getUrl("athenaAuthGetPublicKey");
        }

        public static String getAthenaAuthResponse() {
            return getUrl("athenaAuthResponse");
        }

        public static String getAthenaIngredientList() {
            return getUrl("athenaIngredientList");
        }

        public static String getAthenaItemList() {
            return getUrl("athenaItemList");
        }

        public static String getAthenaServerList() {
            return getUrl("athenaServerList");
        }

        public static String getAthenaTerritoryList() {
            return getUrl("athenaTerritoryList");
        }

        public static String getAthenaUserInfo() {
            return getUrl("athenaUserInfo");
        }

        public static String getDiscordInvite() {
            return getUrl("discordInvite");
        }

        public static String getDiscoveries() {
            return getUrl("discoveries");
        }

        public static String getItemGuesses() {
            return getUrl("itemGuesses");
        }

        public static String getMaps() {
            return getUrl("maps");
        }

        public static String getOnlinePlayers() {
            return getUrl("onlinePlayers");
        }

        public static String getPlaces() {
            return getUrl("places");
        }

        public static String getServices() {
            return getUrl("services");
        }

        public static String getUpdateCheck() {
            return getUrl("updateCheck");
        }

        public static String getWynntilsPatreon() {
            return getUrl("wynntilsPatreon");
        }

        public static String createGoogleTranslation(String toLanguage, String message) {
            return String.format(
                    getUrl("googleTranslation"),
                    StringUtils.encodeUrl(toLanguage),
                    StringUtils.encodeUrl(message));
        }

        public static String createPlayerStats(String playerName) {
            return String.format(getUrl("playerStats"), StringUtils.encodeUrl(playerName));
        }

        public static String createWikiTitleLookup(String pageTitle) {
            return String.format(getUrl("wikiTitleLookup"), StringUtils.encodeUrl(pageTitle));
        }

        public static String createWikiDiscoveryQuery(String name) {
            return String.format(getUrl("wikiDiscoveryQuery"), StringUtils.encodeUrl(name));
        }

        public static String createWikiQuestPageQuery(String name) {
            return String.format(getUrl("wikiQuestPageQuery"), StringUtils.encodeUrl(name));
        }

        public static String createWynndataItemLookup(String unformattedName) {
            return String.format(getUrl("wynndataItemLookup"), StringUtils.encodeUrl(unformattedName));
        }

        public static String createWynntilsRegisterToken(String token) {
            return String.format(getUrl("wynntilsRegisterToken"), StringUtils.encodeUrl(token));
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
