/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.net.downloader.DownloadableResource;
import com.wynntils.core.net.downloader.Downloader;
import com.wynntils.mc.event.WebSetupEvent;
import com.wynntils.utils.StringUtils;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that parses a String into a specific format of "[Key] = Value"
 *
 * <p>Ex: https://api.wynntils.com/webapi provides such a format
 */
public class ApiUrls {
    public static final File API_CACHE_ROOT = WynntilsMod.getModStorageDir("apicache");
    private static final Pattern LINE_MATCHER = Pattern.compile("\\[(?<Key>[^\\[\\]]+)\\]\\s*=\\s*(?<Value>.+)");

    public static ApiUrls getApiUrls() {
        return apiUrls;
    }

    private static ApiUrls apiUrls = null;
    private static boolean setup = false;

    private Map<String, String> values;
    private Map<String, List<String>> lists;

    private static ApiUrls fromString(String data) {
        ApiUrls result = new ApiUrls();
        if (!result.parseData(data)) {
            return null;
        }

        return result;
    }

    public static void tryReloadApiUrls() {
        String url = "https://api.wynntils.com/webapi";
        DownloadableResource dl = Downloader.download(url, new File(API_CACHE_ROOT, "webapi.txt"), "webapi");
        dl.handleBytes(bytes -> {
            String string = new String(bytes, StandardCharsets.UTF_8);
            apiUrls = fromString(string);
            if (apiUrls == null) return false;
            if (!setup) {
                setup = true;
            }

            WynntilsMod.postEvent(new WebSetupEvent());
            return true;
        });
    }

    public static void reset() {
        apiUrls = null;
    }

    public static String getApiUrl(String key) {
        if (getApiUrls() == null) return null;

        return getApiUrls().get(key);
    }

    public static Optional<ApiUrls> getOptionalApiUrls() {
        return Optional.ofNullable(apiUrls);
    }

    public static boolean isSetup() {
        return setup;
    }

    public Map<String, String> getValues() {
        return values;
    }

    private boolean parseData(String data) {
        values = new HashMap<>();
        lists = new HashMap<>();

        for (String str : data.split("\\r?\\n")) {
            Matcher result = LINE_MATCHER.matcher(str);
            if (!result.find()) {
                WynntilsMod.error("Malformed data line: " + str);
                continue;
            }

            String key = result.group("Key");
            String value = result.group("Value");

            if (value.contains(",")) {
                List<String> values = StringUtils.parseStringToList(value);

                lists.put(key, values);
            } else {
                values.put(key, value);
            }
        }

        return true;
    }

    public String get(String key) {
        return values.getOrDefault(key, null);
    }

    public boolean hasKey(String key) {
        return values.containsKey(key);
    }

    public List<String> getList(String key) {
        return lists.getOrDefault(key, new ArrayList<>());
    }

    @Override
    public String toString() {
        return "WebReader{" + "values=" + values + ", lists=" + lists + '}';
    }
}
