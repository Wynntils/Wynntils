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

    private static Map<String, UrlInfo> urlMap;

    public static String getUrl(UrlId urlId2) {
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

    public static String getMethod(UrlId urlId2) {
        String urlId = urlId2.getId();
        return urlMap.get(urlId).method;
    }

    // FIXME: Not done. Also, replace all old buildUrl calls.
    public static String buildUrl(UrlId urlId2, Map<String, String> arguments) {
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
