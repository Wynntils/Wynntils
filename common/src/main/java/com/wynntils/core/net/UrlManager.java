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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public enum NetUrls {
        API_ATHENA_AUTH_PUBLIC_KEY("apiAthenaAuthPublicKey"),
        API_ATHENA_AUTH_RESPONSE("apiAthenaAuthResponse"),
        API_ATHENA_UPDATE_CHECK("apiAthenaUpdateCheck"),
        API_ATHENA_USER_INFO("apiAthenaUserInfo"),
        API_GOOGLE_TRANSLATION("apiGoogleTranslation"),
        API_WIKI_DISCOVERY_QUERY("apiWikiDiscoveryQuery"),
        API_WIKI_QUEST_PAGE_QUERY("apiWikiQuestPageQuery"),

        // dataAthenaIngredientList is based on
        // https://api.wynncraft.com/v2/ingredient/search/skills/%5Etailoring,armouring,jeweling,cooking,woodworking,weaponsmithing,alchemism,scribing
        // but the data is massaged into another form, and additional "head textures" are added, which are hard-coded
        // in Athena
        DATA_ATHENA_INGREDIENT_LIST("dataAthenaIngredientList"),

        // dataAthenaItemList is based on
        // https://api.wynncraft.com/public_api.php?action=itemDB&category=all
        // but the data is massaged into another form, and wynnBuilderID is injected from
        // https://wynnbuilder.github.io/compress.json
        DATA_ATHENA_ITEM_LIST("dataAthenaItemList"),

        // dataAthenaServerList is based on
        // https://api.wynncraft.com/public_api.php?action=onlinePlayers
        // but injects a firstSeen timestamp when the server was first noticed by Athena
        DATA_ATHENA_SERVER_LIST("dataAthenaServerList"),

        // dataAthenaTerritoryList is based on
        // https://api.wynncraft.com/public_api.php?action=territoryList
        // but guild prefix is injected based on
        // https://api.wynncraft.com/public_api.php?action=guildStats&command=<guildName>
        // and guild color is injected based on values maintained on Athena, and a constant
        // level = 1 is also injected.
        DATA_ATHENA_TERRITORY_LIST("dataAthenaTerritoryList"),

        DATA_STATIC_COMBAT_LOCATIONS("dataStaticCombatLocations"),
        DATA_STATIC_DISCOVERIES("dataStaticDiscoveries"),
        DATA_STATIC_ITEM_GUESSES("dataStaticItemGuesses"),
        DATA_STATIC_MAPS("dataStaticMaps"),
        DATA_STATIC_PLACES("dataStaticPlaces"),
        DATA_STATIC_SERVICES("dataStaticServices"),
        DATA_STATIC_SPLASHES("dataStaticSplashes"),
        DATA_STATIC_URLS("dataStaticUrls"),
        LINK_WIKI_LOOKUP("linkWikiLookup"),
        LINK_WYNNCRAFT_PLAYER_STATS("linkWynncraftPlayerStats"),
        LINK_WYNNDATA_ITEM_LOOKUP("linkWynndataItemLookup"),
        LINK_WYNNTILS_DISCORD_INVITE("linkWynntilsDiscordInvite"),
        LINK_WYNNTILS_PATREON("linkWynntilsPatreon"),
        LINK_WYNNTILS_REGISTER_ACCOUNT("linkWynntilsRegisterAccount");

        private final String id;

        NetUrls(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    private static Map<String, UrlInfo> urlMap;

    public static String getUrl(NetUrls urlId2) {
        String urlId = urlId2.getId();
        // Verify that argument count is correct
        assert (urlMap.get(urlId).arguments == null
                || urlMap.get(urlId).arguments.size() == 0);

        return urlMap.get(urlId).url;
    }

    public static Optional<String> getMd5(String urlId) {
        return Optional.ofNullable(urlMap.get(urlId).md5);
    }

    public static List<String> getArguments(String urlId) {
        return urlMap.get(urlId).arguments;
    }

    public static String getMethod(NetUrls urlId2) {
        String urlId = urlId2.getId();
        return urlMap.get(urlId).method;
    }

    // FIXME: Not done. Also, replace all old buildUrl calls.
    public static String buildUrl(NetUrls urlId2, Map<String, String> arguments) {
        String urlId = urlId2.getId();
        // Verify that argument count is correct
        assert (urlMap.get(urlId).arguments != null
                && urlMap.get(urlId).arguments.size() == arguments.size());
        // FIXME: Verify that argument keys are exactly matching argument list in urlMap

        Map<String, String> encodedArguments = arguments.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry ->
                                // FIXME: Also call proper specific encoding!
                                StringUtils.encodeUrl(entry.getValue())));

        String str = urlMap.get(urlId).url;
        // Replace %{argKey} with arg value in URL string
        String result = encodedArguments.keySet().stream()
                .reduce(str, (s, argKey) -> s.replaceAll("%\\{" + argKey + "\\}", encodedArguments.get(argKey)));

        return result;
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
        String method;
        List<String> arguments;
        String md5;
        String encoding;
    }
}
