/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.webapi.request.RequestBuilder;
import com.wynntils.core.webapi.request.RequestHandler;
import com.wynntils.mc.event.WebSetupEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;

/** Provides and loads web content on demand */
public final class WebManager extends CoreManager {

    public static final File API_CACHE_ROOT = WynntilsMod.getModStorageDir("apicache");
    private static final int REQUEST_TIMEOUT_MILLIS = 10000;

    private static boolean setup = false;
    private static final RequestHandler handler = new RequestHandler();

    public static WebReader apiUrls = null;

    private static final String USER_AGENT = String.format(
            "Wynntils Artemis\\%s (%s) %s",
            WynntilsMod.getVersion(),
            WynntilsMod.isDevelopmentEnvironment() ? "dev" : "client",
            WynntilsMod.getModLoader());

    public static void init() {
        tryReloadApiUrls(false);
    }

    public static void reset() {
        // tryReloadApiUrls
        apiUrls = null;
    }

    private static void tryReloadApiUrls(boolean async) {
        handler.addRequest(new RequestBuilder("https://api.wynntils.com/webapi", "webapi")
                .cacheTo(new File(API_CACHE_ROOT, "webapi.txt"))
                .useCacheAsBackup()
                .handleWebReader(reader -> {
                    apiUrls = reader;
                    if (!setup) {
                        setup = true;
                    }

                    WynntilsMod.postEvent(new WebSetupEvent());
                    return true;
                })
                .build());

        handler.dispatch(async);
    }

    public static URLConnection generateURLRequest(String url) throws IOException {
        URLConnection st = new URL(url).openConnection();
        st.setRequestProperty("User-Agent", USER_AGENT);
        if (apiUrls != null && apiUrls.hasKey("WynnApiKey")) st.setRequestProperty("apikey", apiUrls.get("WynnApiKey"));
        st.setConnectTimeout(REQUEST_TIMEOUT_MILLIS);
        st.setReadTimeout(REQUEST_TIMEOUT_MILLIS);

        return st;
    }

    public static String getApiUrl(String key) {
        if (apiUrls == null) return null;

        return apiUrls.get(key);
    }

    public static String getUserAgent() {
        return USER_AGENT;
    }

    public static boolean isSetup() {
        return setup;
    }

    public static Optional<WebReader> getApiUrls() {
        return Optional.ofNullable(apiUrls);
    }

    public static RequestHandler getHandler() {
        return handler;
    }
}
