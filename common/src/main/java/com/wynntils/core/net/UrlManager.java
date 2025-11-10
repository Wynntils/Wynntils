/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.MalformedJsonException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.net.event.UrlProcessingFinishedEvent;
import com.wynntils.core.properties.Property;
import com.wynntils.utils.FileUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.Pair;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Manages the URLs used by the mod.
 *
 * <h2>URL sources:</h2>
 * <ol>
 *     <li>Resource bundled with the mod</li>
 *     <li>Local cache</li>
 *     <li>Online source</li>
 * </ol>
 *
 * <p>
 *     The sources are loaded in the order listed above, and the first valid source is used.
 *     If a source is loaded later, it can override the previous sources, if the version is higher.
 * </p>
 * <p>
 *     The URLs are stored in a JSON file, which is read and parsed into a map of id - url pairs.
 * </p>
 *
 * <h3>Handling data hash conflicts between different data sources</h3>
 * <p>
 *     It's possible for the URL sources to point to different hashes, even when their version matches.
 *     As we can't guarantee that all 3 of the url lists load, and that they are up to date,
 *     some rules have to be applied to make sure outdated hashes are not being used.
 * </p>
 * <p>
 *     The rules are as follows:
 * </p>
 * <ol>
 *     <li>
 *         If the online sources are available, use all hashes available in it. If an url was removed from the online list,
 *         fall back to using the local cache, or the bundled list.
 *     </li>
 *     <li>
 *         If the online sources are not available, use the local cache. If the local cache's hash and the bundled hash
 *         are different, we assume both of the hashes are incorrect, as the local cache may be from an older version,
 *         and the bundled list may have just been updated. This case should be rare, and is usually resolved without
 *         any user interactions, as it's very likely that the remote download won't fail again.
 *     </li>
 *     <li>
 *         If no other sources are available, use the bundled urls, but don't trust any hashes,
 *         to try to be as up-to-date as possible.
 *     </li>
 * </ol>
 *
 * <p>It's also possible to override these rules with JVM flags.</p>
 * <p>See the list of flags for alternative hash conflict resolution below.</p>
 * <ul>
 *     <li>
 *         <code>wynntils.url.force.type</code> - Force the source to be used, regardless of the version.
 *         Possible values are <code>bundled</code>, <code>local</code>, <code>remote</code>.
 *     </li>
 * </ul>
 *
 * <h3>URL override</h3>
 * <p>
 *     It is possible to override the URL list by setting the java property `wynntils.url.list.override.link` to a valid URL.
 *     This will override the bundled URL list. This is useful for regions that block Github CDN or for private purposes.
 * </p>
 * <p>
 *     It is also possible to ignore the cache by setting the java property `wynntils.url.list.ignore.cache` to true.
 *     This will force the mod to download the URL list from the online source, even if the cache is valid.
 *     This is useful for development purposes, or for regions where the url list tends to change frequently.
 * </p>
 */
public final class UrlManager extends Manager {
    private final Property<URI> urlListOverride = createProperty(URI.class, "override.link");
    private final Property<Boolean> ignoreCache = createProperty(Boolean.class, "ignore.cache", false);
    private final Property<Boolean> debugLogs = createProperty(Boolean.class, "log.debug", false);
    private final Property<UrlMapperType> urlMapperForceType = createProperty(UrlMapperType.class, "force.type");
    private final Map<UrlMapperType, UrlMapper> urlMappersByType = new ConcurrentHashMap<>();

    private UrlMapper urlMapper = UrlMapper.EMPTY;

    public UrlManager(NetManager netManager) {
        super(List.of(netManager));

        // Log the settings
        if (urlListOverride.get() == null) {
            WynntilsMod.info("Loading urls.json in the normal mode. Url cache is "
                    + (ignoreCache.get() ? "ignored" : "used") + ".");
        } else {
            WynntilsMod.info("Loading urls.json from " + urlListOverride.get() + ". Url cache is "
                    + (ignoreCache.get() ? "ignored" : "used") + ".");
        }
    }

    public UrlInfo getUrlInfo(UrlId urlId) {
        return urlMapper.get(urlId);
    }

    public String getUrl(UrlId urlId) {
        UrlInfo urlInfo = urlMapper.get(urlId);

        // This is only valid for POST URLs, or GET URLs with no arguments
        assert (urlInfo.method() == Method.POST || urlInfo.arguments().isEmpty());

        return urlInfo.url();
    }

    public String buildUrl(UrlId urlId, Map<String, String> arguments) {
        UrlInfo urlInfo = urlMapper.get(urlId);

        return buildUrl(urlInfo, arguments);
    }

    public String buildUrl(UrlInfo urlInfo, Map<String, String> arguments) {
        // Verify that arguments match with what is specified
        assert (arguments.keySet().equals(new HashSet<>(urlInfo.arguments())))
                : "Arguments mismatch for " + urlInfo.url + ", expected: " + urlInfo.arguments() + " got: "
                        + arguments.keySet();

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

    @Override
    public void reloadData() {
        // Specifically do NOT reload the URLs here, as they require special handling every time,
        // unlike other core components.
    }

    public void loadUrls() {
        // Reset the URL lists
        urlMappersByType.clear();
        urlMapper = UrlMapper.EMPTY;

        // Figure out where to load the URLs from initially

        // If we don't have an override, read the URLs in a normal way
        if (urlListOverride.get() == null) {
            // Start by reading the URLs from the resource embedded in the mod, so we have something to rely on
            readEmbeddedUrls();

            if (!ignoreCache.get()) {
                // Then try to read the URLs from the local cache
                readLocalUrlCache();
            }

            // Then trigger a (re-)download from the net to the cache
            // We need to do the urlInfo lookup ourself, since we might have
            // a embryonic netManager which can't do much.
            UrlInfo urlInfo = urlMappersByType
                    .getOrDefault(
                            UrlMapperType.BUNDLED,
                            urlMappersByType.getOrDefault(UrlMapperType.LOCAL_CACHE, UrlMapper.EMPTY))
                    .get(UrlId.DATA_STATIC_URLS);
            if (urlInfo == null) {
                WynntilsMod.error("ERROR: Failed to load baseline URL list. Try deleting Wynntils cache.");
                throw new RuntimeException("Missing DATA_STATIC_URLS from cached and bundled urls.json");
            }

            URI uri = URI.create(urlInfo.url());
            downloadAndReadRemoteUrls(uri);
        } else {
            // Start by reading the URLs from the resource embedded in the mod, so we have something to rely on
            readEmbeddedUrls();

            if (!ignoreCache.get()) {
                // Try to read the URLs from the local cache
                readLocalUrlCache();
            }

            // Then trigger a (re-)download from the net to the cache
            downloadAndReadRemoteUrls(urlListOverride.get());
        }
    }

    private void readEmbeddedUrls() {
        try {
            readInputStreamForUrl(getBundledInputStream(), UrlMapperType.BUNDLED);
        } catch (IOException | JsonSyntaxException e) {
            // We can't load the url list bundled. This is a catastrophic failure.
            // Nothing will work after this, so just abort.
            throw new RuntimeException(
                    "ERROR: Bundled JSON has invalid syntax or is malformed, or it could not be read. This might be because of a corrupt download. Try updating Wynntils.",
                    e);
        }
    }

    private void readLocalUrlCache() {
        Pair<FileInputStream, File> localCache = getLocalCacheInputStreams();
        if (localCache != null) {
            try {
                readInputStreamForUrl(localCache.key(), UrlMapperType.LOCAL_CACHE);
            } catch (IOException | JsonSyntaxException e) {
                // The local cache is corrupt. Delete it and try again.
                WynntilsMod.warn("Problem reading URL list from local cache, deleting it.", e);
                FileUtils.deleteFile(localCache.value());
            }
        }
    }

    private void downloadAndReadRemoteUrls(URI uri) {
        String localFileName = UrlId.DATA_STATIC_URLS.getId();

        Download dl = Managers.Net.download(uri, localFileName);
        dl.handleInputStream(
                inputStream -> {
                    try {
                        UrlMapper urlMapper = readUrlMapper(inputStream);
                        urlMappersByType.put(UrlMapperType.REMOTE, urlMapper);

                        // Merge the lists with the remote list as the primary source
                        mergeUrlMappers();
                    } catch (IllegalStateException e) {
                        // IllegalStateExceptions are thrown on critical errors detected by the manager
                        WynntilsMod.error("Critical error while updating URL list from online source", e);
                        throw e;
                    } catch (IOException e) {
                        WynntilsMod.warn("Problem updating URL list from online source", e);

                        // Merge the lists we could load, even if the remote list failed
                        mergeUrlMappers();
                    }
                },
                throwable -> WynntilsMod.warn("Failed to download URL list from online source", throwable));
    }

    private void readInputStreamForUrl(InputStream tryStream, UrlMapperType listType)
            throws JsonSyntaxException, IOException {
        try (InputStream inputStream = tryStream) {
            UrlMapper urlMapper = readUrlMapper(inputStream);
            urlMappersByType.put(listType, urlMapper);
        } catch (MalformedJsonException e) {
            // This is handled by the caller, so just rethrow
            throw new MalformedJsonException(e);
        }
    }

    private void mergeUrlMappers() {
        // In theory, this method shouldn't be called concurrently ever, but just in case
        synchronized (urlMapper) {
            int currentVersion = -1;
            Map<UrlId, UrlInfo> currentUrls = new LinkedHashMap<>();

            // Start by using the bundled urls as a baseline
            UrlMapper bundledList = urlMappersByType.get(UrlMapperType.BUNDLED);

            if (urlMapperForceType.get() == null || urlMapperForceType.get() == UrlMapperType.BUNDLED) {
                currentVersion = bundledList.version();
                currentUrls.putAll(bundledList.urls());
            }

            if (debugLogs.get()) {
                WynntilsMod.info("Bundled URL list version: " + currentVersion + ", URLs: " + currentUrls.size());
            }

            // Then check if we have a local cache
            if (urlMapperForceType.get() == null || urlMapperForceType.get() == UrlMapperType.LOCAL_CACHE) {
                currentVersion = checkLocalCache(currentVersion, currentUrls);
            }

            // Once we are done with the local cache, try to use the remote list as much as we can
            if (urlMapperForceType.get() == null || urlMapperForceType.get() == UrlMapperType.REMOTE) {
                currentVersion = checkRemoteList(currentVersion, currentUrls);
            }

            // Finally, set the new URL list
            urlMapper = new UrlMapper(currentVersion, currentUrls);
        }

        if (urlMapper.urls().isEmpty()) {
            throw new IllegalStateException(
                    """
                                 URL list is empty after merging. This means all three of the URL sources failed to load.
                                 If you have set a custom url loading mode, this means that it failed to load.
                                 Otherwise, this is a critical error, try contacting the developers.
                            """);
        }

        // Fire the event that we have finished processing the URLs
        // Move the loading back to the main thread, even if that's not strictly necessary
        WynntilsMod.info("Merged URL list. Version: " + urlMapper.version + ", URLs: " + urlMapper.urls.size());
        WynntilsMod.postEventOnMainThread(new UrlProcessingFinishedEvent());

        // Also trigger a reload for all components, as they might depend on the URLs which they couldn't load before
        WynntilsMod.reloadAllComponentData();
    }

    private int checkLocalCache(int currentVersion, Map<UrlId, UrlInfo> currentUrls) {
        if (urlMappersByType.containsKey(UrlMapperType.LOCAL_CACHE)) {
            UrlMapper localCacheList = urlMappersByType.get(UrlMapperType.LOCAL_CACHE);

            // Firstly, check for version differences between the lists
            if (localCacheList.version() >= currentVersion) {
                currentVersion = localCacheList.version();

                // We do have a local cache. Let's merge it in.
                // Two main rules are applied:
                // 1. If an URL is only present in the local cache, it is added to the list, without a hash.
                // 2. If an URL is present in both the local cache and the bundled list, remove the hash info,
                //    to ensure that the most up-to-date data is downloaded, as we have no way of knowing which one
                //    is correct.

                // Add all URLs from the local cache that are not in the current list
                localCacheList.urls.entrySet().stream()
                        .filter(entry -> !currentUrls.containsKey(entry.getKey()))
                        .forEach(entry -> currentUrls.put(entry.getKey(), entry.getValue()));

                // Remove the hashes from the URLs that are in both the local cache and the bundled list
                localCacheList.urls().forEach((key, value) -> {
                    if (!currentUrls.containsKey(key)) return;

                    UrlInfo urlInfo = currentUrls.get(key);

                    // If the hashes are different, remove the hash
                    if (!urlInfo.md5().equals(value.md5())) {
                        currentUrls.put(key, value.withoutMd5());

                        if (debugLogs.get()) {
                            WynntilsMod.info("Bundled and local hashes differ for " + key + ". Removing hash. ("
                                    + urlInfo.md5().orElse("null") + " -> null)");
                        }
                    }
                });
            }
        } else {
            WynntilsMod.warn(
                    "No URL cache found. This is normal if you are running Wynntils for the first time. Otherwise, this likely indicates a problem.");
        }
        return currentVersion;
    }

    private int checkRemoteList(int currentVersion, Map<UrlId, UrlInfo> currentUrls) {
        if (urlMappersByType.containsKey(UrlMapperType.REMOTE)) {
            UrlMapper remoteList = urlMappersByType.get(UrlMapperType.REMOTE);

            // Firstly, check for version differences between the lists
            // It's very weird for the remote list to have a lower version than the local cache, but it's
            // possible
            if (remoteList.version() < currentVersion) {
                throw new IllegalStateException(
                        "Remote URL list has a lower version than the local cache. This should not happen.");
            }

            currentVersion = remoteList.version();

            // Use the remote list's hashes for all URLs, if they are present
            remoteList.urls().forEach((key, value) -> {
                if (currentUrls.containsKey(key)) {
                    UrlInfo oldInfo = currentUrls.put(key, value);

                    if (debugLogs.get() && oldInfo != null && !oldInfo.md5().equals(value.md5())) {
                        WynntilsMod.info("Remote hash differs for " + key + ". Using remote hash. (" + oldInfo.md5()
                                + " -> " + value.md5().orElse("null") + ")");
                    }
                } else {
                    currentUrls.put(key, value);
                }
            });
        } else {
            WynntilsMod.warn("No remote URL list available. Falling back to local sources.");
        }
        return currentVersion;
    }

    private InputStream getBundledInputStream() {
        return WynntilsMod.getModResourceAsStream("urls.json");
    }

    private Pair<FileInputStream, File> getLocalCacheInputStreams() {
        // First check if there is a copy in the local cache
        File cacheFile = Managers.Net.getCacheFile(UrlId.DATA_STATIC_URLS.getId());
        if (cacheFile.exists() && cacheFile.length() > 0) {
            // Yes, we have a cache. Use it to populate the map
            try {
                return Pair.of(new FileInputStream(cacheFile), cacheFile);
            } catch (FileNotFoundException e) {
                // This should not happens since we just checked, but fall through
                // to bundled case if so
            }
        }

        // No usable cache.
        return null;
    }

    private UrlMapper readUrlMapper(InputStream inputStream) throws IOException, JsonSyntaxException {
        byte[] data = inputStream.readAllBytes();
        String json = new String(data, StandardCharsets.UTF_8);
        Type type = new TypeToken<List<UrlProfile>>() {}.getType();
        List<UrlProfile> urlProfiles = WynntilsMod.GSON.fromJson(json, type);

        Map<UrlId, UrlInfo> newMap = new HashMap<>();

        int version = 0;
        for (UrlProfile urlProfile : urlProfiles) {
            if (urlProfile.version != 0) {
                // This is the special version record
                version = urlProfile.version;
                continue;
            }
            List<String> arguments = urlProfile.arguments == null ? List.of() : urlProfile.arguments;
            Optional<UrlId> urlId = UrlId.from(urlProfile.id);

            if (urlId.isEmpty()) {
                // This is a URL we don't know about. Ignore it.
                continue;
            }

            newMap.put(
                    urlId.get(),
                    new UrlInfo(
                            urlProfile.url,
                            arguments,
                            Method.from(urlProfile.method),
                            Encoding.from(urlProfile.encoding),
                            Optional.ofNullable(urlProfile.md5)));
        }

        // Sanity check that we got all ids
        for (UrlId urlId : UrlId.values()) {
            if (!newMap.containsKey(urlId)) {
                WynntilsMod.warn("Missing URL in urls.json: " + urlId);
                return UrlMapper.EMPTY;
            }
        }

        return new UrlMapper(version, newMap);
    }

    public enum Method {
        GET,
        POST;

        private static Method from(String str) {
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

        private static Encoding from(String str) {
            if (str == null || str.isEmpty()) return NONE; // NONE is default if unspecified
            return Encoding.valueOf(str.toUpperCase(Locale.ROOT));
        }

        String encode(String input) {
            return encoder.apply(input);
        }
    }

    public record UrlInfo(String url, List<String> arguments, Method method, Encoding encoding, Optional<String> md5) {
        public UrlInfo withoutMd5() {
            return new UrlInfo(url, arguments, method, encoding, Optional.empty());
        }
    }

    private static final class UrlProfile {
        int version;
        String id;
        String url;
        String method;
        List<String> arguments;
        String md5;
        String encoding;
    }

    private record UrlMapper(int version, Map<UrlId, UrlInfo> urls) {
        public static final UrlMapper EMPTY = new UrlMapper(-1, Map.of());

        public UrlInfo get(UrlId urlId) {
            return urls.get(urlId);
        }
    }

    private enum UrlMapperType {
        BUNDLED,
        LOCAL_CACHE,
        REMOTE
    }
}
