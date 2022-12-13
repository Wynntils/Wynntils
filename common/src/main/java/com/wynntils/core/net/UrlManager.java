/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.google.gson.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.utils.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class UrlManager extends CoreManager {
    private static Map<UrlId, UrlInfo> urlMap = Map.of();

    public static void init() {
        loadUrls();
    }

    public static UrlInfo getUrlInfo(UrlId urlId) {
        return urlMap.get(urlId);
    }

    public static String getUrl(UrlId urlId) {
        UrlInfo urlInfo = urlMap.get(urlId);

        // This is only valid for POST URLs, or GET URLs with no arguments
        assert (urlInfo.method() == Method.POST || urlInfo.arguments().isEmpty());

        return urlInfo.url();
    }

    public static String buildUrl(UrlId urlId, Map<String, String> arguments) {
        UrlInfo urlInfo = urlMap.get(urlId);

        return buildUrl(urlInfo, arguments);
    }

    public static String buildUrl(UrlInfo urlInfo, Map<String, String> arguments) {
        // Verify that arguments match with what is specified
        assert (arguments.keySet().equals(new HashSet<>(urlInfo.arguments())));

        // Replace %{argKey} with arg value in URL string
        return arguments.keySet().stream()
                .reduce(
                        urlInfo.url(),
                        (str, argKey) -> str.replaceAll(
                                "%\\{" + argKey + "\\}",
                                // First encode with specified encoder (if any), then finish by
                                // always url encoding arguments
                                StringUtils.encodeUrl(urlInfo.encoding().encode(arguments.get(argKey)))));
    }

    public static void reloadUrls() {
        loadUrls();
    }

    private static void loadUrls() {
        // First check if there is a copy in the local cache
        File cacheFile = NetManager.getCacheFile(UrlId.DATA_STATIC_URLS.getId());
        if (cacheFile.exists()) {
            // Yes, we have a cache. Use it for the initial population
            try (InputStream inputStream = new FileInputStream(cacheFile)) {
                readUrls(inputStream);
            } catch (IOException e) {
                // FIXME: Error handling
                throw new RuntimeException(e);
            }
        } else {
            // No cache. Start by reading the URLs from the resource embedded in the mod,
            // so we have something to rely on
            try (InputStream inputStream = WynntilsMod.getModResourceAsStream("urls.json")) {
                readUrls(inputStream);
            } catch (IOException e) {
                // FIXME: Error handling
                throw new RuntimeException(e);
            }
        }

        // In any case, trigger a re-download from the net to the cache
        Download dl = NetManager.download(UrlId.DATA_STATIC_URLS);
        dl.handleInputStream(inputStream -> {
            try {
                readUrls(inputStream);
            } catch (IOException e) {
                // FIXME: Error handling
                throw new RuntimeException(e);
            }
        });
        // If not, use the one included in the mod resources
    }

    private static void readUrls(InputStream inputStream) throws IOException {
        byte[] data = inputStream.readAllBytes();
        String json = new String(data, StandardCharsets.UTF_8);
        Type type = new TypeToken<List<UrlProfile>>() {}.getType();
        List<UrlProfile> urlProfiles = WynntilsMod.GSON.fromJson(json, type);

        Map<UrlId, UrlInfo> newMap = new HashMap<>();

        for (UrlProfile urlProfile : urlProfiles) {
            List<String> arguments = urlProfile.arguments == null ? List.of() : urlProfile.arguments;
            newMap.put(
                    UrlId.from(urlProfile.id),
                    new UrlInfo(
                            urlProfile.url,
                            arguments,
                            Method.from(urlProfile.method),
                            Encoding.from(urlProfile.encoding),
                            Optional.ofNullable(urlProfile.md5)));
        }

        // Sanity check that we got all ids
        if (newMap.size() != UrlId.values().length) {
            throw new IOException("Not all urlIds present in urls.json");
        }
        urlMap = newMap;
    }

    public enum Method {
        GET,
        POST;

        public static Method from(String str) {
            if (str == null || str.isEmpty()) return GET; // GET is default if unspecified
            return Method.valueOf(str.toUpperCase(Locale.ROOT));
        }
    }

    public enum Encoding {
        NONE(s -> s),
        CARGO(s -> "'" + s.replace("'", "\\'") + "'"),
        WIKI(s -> s.replace(" ", "_"));

        private final Function<String, String> encoder;

        Encoding(Function<String, String> encoder) {
            this.encoder = encoder;
        }

        public static Encoding from(String str) {
            if (str == null || str.isEmpty()) return NONE; // NONE is default if unspecified
            return Encoding.valueOf(str.toUpperCase(Locale.ROOT));
        }

        String encode(String input) {
            return encoder.apply(input);
        }
    }

    public record UrlInfo(String url, List<String> arguments, Method method, Encoding encoding, Optional<String> md5) {}

    private static final class UrlProfile {
        String id;
        String url;
        String method;
        List<String> arguments;
        String md5;
        String encoding;
    }
}
