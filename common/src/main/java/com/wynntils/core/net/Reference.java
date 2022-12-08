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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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

        public static String getUrl(String urlId) {
            // Verify that argument count is correct
            assert (urlMap.get(urlId).numArguments == null || urlMap.get(urlId).numArguments == 0);

            return urlMap.get(urlId).url;
        }

        public static Optional<String> getMd5(String urlId) {
            return Optional.ofNullable(urlMap.get(urlId).md5);
        }

        public static String buildUrl(String urlId, String... arguments) {
            // Verify that argument count is correct
            assert (urlMap.get(urlId).numArguments != null && urlMap.get(urlId).numArguments == arguments.length);
            Function<String, String> encoding = getEncoding(urlMap.get(urlId).encoding);

            String[] encodedArguments = Arrays.stream(arguments)
                    .map(encoding)
                    .map(StringUtils::encodeUrl)
                    .toArray(String[]::new);
            return String.format(urlMap.get(urlId).url, encodedArguments);
        }

        private static Function<String, String> getEncoding(String encoding) {
            if (encoding == null || encoding.isEmpty()) return Function.identity();

            return switch (encoding) {
                case "cargo" -> (s -> "'" + s.replace("'", "\\'") + "'");
                case "wiki" -> (s -> s.replace(" ", "_"));
                default -> throw new RuntimeException("Unknown URL encoding: " + encoding);
            };
        }

        public static void reloadUrls() {
            // FIXME: Not implemented yet
        }

        private static void init() {
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
