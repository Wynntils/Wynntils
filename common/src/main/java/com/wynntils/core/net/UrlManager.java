/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.google.gson.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.utils.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class UrlManager extends Manager {
    private Map<UrlId, UrlInfo> urlMap = Map.of();

    public UrlManager() {
        super(List.of());
        // This is a way of resolving the circular dependencies between UrlManager and
        // NetManager. UrlManager needs Net to download the urls.json file, but NetManager
        // needs UrlManager to resolve the URL for the source urls.json file to download.
        loadUrls(new NetManager(this));
    }

    public UrlInfo getUrlInfo(UrlId urlId) {
        return urlMap.get(urlId);
    }

    public String getUrl(UrlId urlId) {
        UrlInfo urlInfo = urlMap.get(urlId);

        // This is only valid for POST URLs, or GET URLs with no arguments
        assert (urlInfo.method() == Method.POST || urlInfo.arguments().isEmpty());

        return urlInfo.url();
    }

    public String buildUrl(UrlId urlId, Map<String, String> arguments) {
        UrlInfo urlInfo = urlMap.get(urlId);

        return buildUrl(urlInfo, arguments);
    }

    public String buildUrl(UrlInfo urlInfo, Map<String, String> arguments) {
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

    public void reloadUrls() {
        // If we reload URLs after initial bootstrapping, use normal manager
        loadUrls(Managers.Net);
    }

    private void loadUrls(NetManager netManager) {
        // Figure out where to load the URLs from initially
        try (InputStream inputStream = getLocalInputStream(netManager)) {
            readUrls(inputStream);
        } catch (IOException e) {
            // We can't load the url list from local disk. This is a catastrophic failure.
            // Nothing will work after this, so just abort.
            WynntilsMod.error("ERROR: Cannot load URLs from local disk. Try deleting Wynntils cache.");
            throw new RuntimeException(e);
        }

        // Then trigger a (re-)download from the net to the cache
        // We need to do the urlInfo lookup ourself, since we might have
        // a embryonic netManager which can't do much.
        UrlManager.UrlInfo urlInfo = getUrlInfo(UrlId.DATA_STATIC_URLS);
        URI uri = URI.create(urlInfo.url());
        String localFileName = UrlId.DATA_STATIC_URLS.getId();

        Download dl = netManager.download(uri, localFileName);
        dl.handleInputStream(inputStream -> {
            try {
                readUrls(inputStream);
            } catch (IOException e) {
                WynntilsMod.warn("Problem updating URL list from online source", e);
            }
        });
    }

    private InputStream getLocalInputStream(NetManager netManager) {
        // First check if there is a copy in the local cache
        File cacheFile = netManager.getCacheFile(UrlId.DATA_STATIC_URLS.getId());
        if (cacheFile.exists() && cacheFile.length() > 0) {
            // Yes, we have a cache. Use it to populate the map
            try {
                return new FileInputStream(cacheFile);
            } catch (FileNotFoundException e) {
                // This should not happens since we just checked, but use fallback if so
                return WynntilsMod.getModResourceAsStream("urls.json");
            }
        } else {
            // No cache. Start by reading the URLs from the resource embedded in the mod,
            // so we have something to rely on
            return WynntilsMod.getModResourceAsStream("urls.json");
        }
    }

    private void readUrls(InputStream inputStream) throws IOException {
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
