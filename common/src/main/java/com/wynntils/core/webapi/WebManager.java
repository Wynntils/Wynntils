/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.net.downloader.DownloadableResource;
import com.wynntils.core.net.downloader.Downloader;
import com.wynntils.core.webapi.account.WynntilsAccount;
import com.wynntils.wynn.model.discoveries.objects.DiscoveryInfo;
import com.wynntils.wynn.netresources.ItemProfilesManager;
import com.wynntils.wynn.netresources.SplashManager;
import com.wynntils.wynn.netresources.profiles.DiscoveryProfile;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Provides and loads web content on demand */
public final class WebManager extends CoreManager {

    private static final int REQUEST_TIMEOUT_MILLIS = 10000;

    public static final Gson gson = new Gson();

    private static List<DiscoveryInfo> discoveryInfoList = new ArrayList<>();

    private static final String USER_AGENT = String.format(
            "Wynntils Artemis\\%s (%s) %s",
            WynntilsMod.getVersion(),
            WynntilsMod.isDevelopmentEnvironment() ? "dev" : "client",
            WynntilsMod.getModLoader());

    public static void init() {
        ApiUrls.tryReloadApiUrls();
        WynntilsAccount.setupUserAccount();

        SplashManager.init();

        ItemProfilesManager.loadCommonObjects();
    }

    public static void reset() {
        ApiUrls.reset();

        ItemProfilesManager.reset();
    }

    /**
     * Request all online players to WynnAPI
     *
     * @return a {@link HashMap} who the key is the server and the value is an array containing all
     *     players on it
     * @throws IOException thrown by URLConnection
     */
    public static Map<String, List<String>> getOnlinePlayers() throws IOException {
        if (ApiUrls.getApiUrls() == null || !ApiUrls.getApiUrls().hasKey("OnlinePlayers")) return new HashMap<>();

        URLConnection st = generateURLRequest(ApiUrls.getApiUrls().get("OnlinePlayers"));
        InputStreamReader stInputReader = new InputStreamReader(st.getInputStream(), StandardCharsets.UTF_8);
        JsonObject main = JsonParser.parseReader(stInputReader).getAsJsonObject();

        if (!main.has("message")) {
            main.remove("request");

            Type type = new TypeToken<LinkedHashMap<String, ArrayList<String>>>() {}.getType();

            return gson.fromJson(main, type);
        } else {
            return new HashMap<>();
        }
    }

    public static void updateDiscoveries() {
        if (ApiUrls.getApiUrls() == null) return;

        String url = ApiUrls.getApiUrls().get("Discoveries");
        DownloadableResource dl =
                Downloader.download(url, new File(ApiUrls.API_CACHE_ROOT, "discoveries.json"), "discoveries");
        dl.handleJsonObject(json -> {
            Type type = new TypeToken<ArrayList<DiscoveryProfile>>() {}.getType();

            List<DiscoveryProfile> discoveries = gson.fromJson(json, type);
            discoveryInfoList = discoveries.stream().map(DiscoveryInfo::new).toList();
            return true;
        });
    }

    public static URLConnection generateURLRequest(String url) throws IOException {
        URLConnection st = new URL(url).openConnection();
        st.setRequestProperty("User-Agent", getUserAgent());
        if (ApiUrls.getApiUrls() != null && ApiUrls.getApiUrls().hasKey("WynnApiKey"))
            st.setRequestProperty("apikey", ApiUrls.getApiUrls().get("WynnApiKey"));
        st.setConnectTimeout(REQUEST_TIMEOUT_MILLIS);
        st.setReadTimeout(REQUEST_TIMEOUT_MILLIS);

        return st;
    }

    public static List<DiscoveryInfo> getDiscoveryInfoList() {
        return discoveryInfoList;
    }

    public static String getUserAgent() {
        return USER_AGENT;
    }

    public static boolean isSetup() {
        return ApiUrls.setup;
    }

}
