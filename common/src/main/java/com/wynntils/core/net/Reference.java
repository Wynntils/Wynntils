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
        public static final String ATHENA_AUTH_GET_PUBLIC_KEY = "athenaAuthGetPublicKey";
        public static final String ATHENA_AUTH_RESPONSE = "athenaAuthResponse";
        public static final String ATHENA_INGREDIENT_LIST = "athenaIngredientList";
        public static final String ATHENA_ITEM_LIST = "athenaItemList";
        public static final String ATHENA_SERVER_LIST = "athenaServerList";
        public static final String ATHENA_TERRITORY_LIST = "athenaTerritoryList";
        public static final String ATHENA_USER_INFO = "athenaUserInfo";
        public static final String DISCORD_INVITE = "discordInvite";
        public static final String DISCOVERIES = "discoveries";
        public static final String GOOGLE_TRANSLATION = "googleTranslation";
        public static final String ITEM_GUESSES = "itemGuesses";
        public static final String MAPS = "maps";
        public static final String ONLINE_PLAYERS = "onlinePlayers";
        public static final String PLACES = "places";
        public static final String PLAYER_STATS = "playerStats";
        public static final String SERVICES = "services";
        public static final String UPDATE_CHECK = "updateCheck";
        public static final String WIKI_DISCOVERY_QUERY = "wikiDiscoveryQuery";
        public static final String WIKI_QUEST_PAGE_QUERY = "wikiQuestPageQuery";
        public static final String WIKI_TITLE_LOOKUP = "wikiTitleLookup";
        public static final String WYNNDATA_ITEM_LOOKUP = "wynndataItemLookup";
        public static final String WYNNTILS_PATREON = "wynntilsPatreon";
        public static final String WYNNTILS_REGISTER_TOKEN = "wynntilsRegisterToken";

        private static Map<String, UrlInfo> urlMap;

        static {
            init();
        }

        public static String getUrl(String urlKey) {
            return getUrl(urlKey);
        }

        public static String getAthenaAuthGetPublicKey() {
            return getUrl(ATHENA_AUTH_GET_PUBLIC_KEY);
        }

        public static String getAthenaAuthResponse() {
            return getUrl(ATHENA_AUTH_RESPONSE);
        }

        public static String getAthenaIngredientList() {
            return getUrl(ATHENA_INGREDIENT_LIST);
        }

        public static String getAthenaItemList() {
            return getUrl(ATHENA_ITEM_LIST);
        }

        public static String getAthenaServerList() {
            return getUrl(ATHENA_SERVER_LIST);
        }

        public static String getAthenaTerritoryList() {
            return getUrl(ATHENA_TERRITORY_LIST);
        }

        public static String getAthenaUserInfo() {
            return getUrl(ATHENA_USER_INFO);
        }

        public static String getDiscordInvite() {
            return getUrl(DISCORD_INVITE);
        }

        public static String getDiscoveries() {
            return getUrl(DISCOVERIES);
        }

        public static String getItemGuesses() {
            return getUrl(ITEM_GUESSES);
        }

        public static String getMaps() {
            return getUrl(MAPS);
        }

        public static String getOnlinePlayers() {
            return getUrl(ONLINE_PLAYERS);
        }

        public static String getPlaces() {
            return getUrl(PLACES);
        }

        public static String getServices() {
            return getUrl(SERVICES);
        }

        public static String getUpdateCheck() {
            return getUrl(UPDATE_CHECK);
        }

        public static String getWynntilsPatreon() {
            return getUrl(WYNNTILS_PATREON);
        }

        public static String createGoogleTranslation(String toLanguage, String message) {
            return String.format(
                    getUrl(GOOGLE_TRANSLATION),
                    StringUtils.encodeUrl(toLanguage),
                    StringUtils.encodeUrl(message));
        }

        public static String createPlayerStats(String playerName) {
            return String.format(getUrl(PLAYER_STATS), StringUtils.encodeUrl(playerName));
        }

        public static String createWikiTitleLookup(String pageTitle) {
            return String.format(getUrl(WIKI_TITLE_LOOKUP), StringUtils.encodeUrl(pageTitle));
        }

        public static String createWikiDiscoveryQuery(String name) {
            return String.format(getUrl(WIKI_DISCOVERY_QUERY), StringUtils.encodeUrl(name));
        }

        public static String createWikiQuestPageQuery(String name) {
            return String.format(getUrl(WIKI_QUEST_PAGE_QUERY), StringUtils.encodeUrl(name));
        }

        public static String createWynndataItemLookup(String unformattedName) {
            return String.format(getUrl(WYNNDATA_ITEM_LOOKUP), StringUtils.encodeUrl(unformattedName));
        }

        public static String createWynntilsRegisterToken(String token) {
            return String.format(getUrl(WYNNTILS_REGISTER_TOKEN), StringUtils.encodeUrl(token));
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
