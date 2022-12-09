/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.utils.StringUtils;
import java.io.File;
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

public final class UrlManager extends CoreManager {
    private static final File CACHE_DIR = WynntilsMod.getModStorageDir("cache");
    private static final Gson GSON = new Gson();

    /*
    URL ids are build up like <TYPE>_<PROVIDER>_<NAME>, where <TYPE> is one of:

    1) API -- this is a REST API call entry point, which will need to be done dynamically
    2) DATA -- this will download a json file which can be cached locally
    3) LINK -- this is a link that will be opened in the user's browser

    <PROVIDER> is a moniker for the provider, and <NAME> is a descriptive but short name
    of the kind of service, data or link this URL is dealing with.
     */
    public static final String API_ATHENA_AUTH_PUBLIC_KEY = "apiAthenaAuthPublicKey";
    public static final String API_ATHENA_AUTH_RESPONSE = "apiAthenaAuthResponse";
    public static final String API_ATHENA_UPDATE_CHECK = "apiAthenaUpdateCheck";
    public static final String API_ATHENA_USER_INFO = "apiAthenaUserInfo";
    public static final String API_GOOGLE_TRANSLATION = "apiGoogleTranslation";
    public static final String API_WIKI_DISCOVERY_QUERY = "apiWikiDiscoveryQuery";
    public static final String API_WIKI_QUEST_PAGE_QUERY = "apiWikiQuestPageQuery";
    public static final String API_WYNNCRAFT_ONLINE_PLAYERS = "apiWynncraftOnlinePlayers";
    public static final String DATA_ATHENA_INGREDIENT_LIST = "dataAthenaIngredientList";
    public static final String DATA_ATHENA_ITEM_LIST = "dataAthenaItemList";
    public static final String DATA_ATHENA_SERVER_LIST = "dataAthenaServerList";
    public static final String DATA_ATHENA_TERRITORY_LIST = "dataAthenaTerritoryList";
    public static final String DATA_STATIC_COMBAT_LOCATIONS = "dataStaticCombatLocations";
    public static final String DATA_STATIC_DISCOVERIES = "dataStaticDiscoveries";
    public static final String DATA_STATIC_ITEM_GUESSES = "dataStaticItemGuesses";
    public static final String DATA_STATIC_MAPS = "dataStaticMaps";
    public static final String DATA_STATIC_PLACES = "dataStaticPlaces";
    public static final String DATA_STATIC_SERVICES = "dataStaticServices";
    public static final String DATA_STATIC_SPLASHES = "dataStaticSplashes";
    public static final String DATA_STATIC_URLS = "dataStaticUrls";
    public static final String LINK_WIKI_LOOKUP = "linkWikiLookup";
    public static final String LINK_WYNNCRAFT_PLAYER_STATS = "linkWynncraftPlayerStats";
    public static final String LINK_WYNNDATA_ITEM_LOOKUP = "linkWynndataItemLookup";
    public static final String LINK_WYNNTILS_DISCORD_INVITE = "linkWynntilsDiscordInvite";
    public static final String LINK_WYNNTILS_PATREON = "linkWynntilsPatreon";
    public static final String LINK_WYNNTILS_REGISTER_ACCOUNT = "linkWynntilsRegisterAccount";

    private static Map<String, UrlInfo> urlMap;

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
        init();
    }

    public static void init() {
        try {
            // FIXME: First check if there is a local cache in CACHE_DIR; if so, use it
            // If not, use the one included in the mod resources
            // In both cases, trigger a re-download from the net to the cache

            // But right now, we just use the one from the resources
            InputStream inputStream = WynntilsMod.getModResourceAsStream("urls.json");
            readUrls(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void readUrls(InputStream inputStream) throws IOException {
        byte[] data = inputStream.readAllBytes();
        String json = new String(data, StandardCharsets.UTF_8);
        Type type = new TypeToken<List<UrlInfo>>() {}.getType();
        List<UrlInfo> urlInfos = GSON.fromJson(json, type);
        urlMap = new HashMap<>();
        for (UrlInfo urlInfo : urlInfos) {
            urlMap.put(urlInfo.id, urlInfo);
        }
    }

    private static final class UrlInfo {
        String id;
        String url;
        String md5;
        String encoding;
        Integer numArguments;
    }
}
