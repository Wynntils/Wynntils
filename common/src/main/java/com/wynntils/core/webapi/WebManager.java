/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.webapi.account.WynntilsAccount;
import com.wynntils.core.webapi.request.RequestBuilder;
import com.wynntils.core.webapi.request.RequestHandler;
import com.wynntils.mc.event.WebSetupEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.model.discoveries.objects.DiscoveryInfo;
import com.wynntils.wynn.objects.profiles.DiscoveryProfile;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

/** Provides and loads web content on demand */
public final class WebManager extends CoreManager {

    public static final File API_CACHE_ROOT = WynntilsMod.getModStorageDir("apicache");
    private static final int REQUEST_TIMEOUT_MILLIS = 10000;

    private static boolean setup = false;
    private static final RequestHandler handler = new RequestHandler();

    public static WebReader apiUrls = null;

    private static final Gson gson = new Gson();

    private static List<DiscoveryInfo> discoveryInfoList = new ArrayList<>();

    private static WynntilsAccount account = null;

    private static final String USER_AGENT = String.format(
            "Wynntils Artemis\\%s (%s) %s",
            WynntilsMod.getVersion(),
            WynntilsMod.isDevelopmentEnvironment() ? "dev" : "client",
            WynntilsMod.getModLoader());

    public static void init() {
        tryReloadApiUrls(false);
        setupUserAccount();
    }

    public static boolean isLoggedIn() {
        return (account != null && account.isConnected());
    }

    public static void reset() {
        // tryReloadApiUrls
        apiUrls = null;
    }

    private static void setupUserAccount() {
        if (isLoggedIn()) return;

        account = new WynntilsAccount();
        boolean accountSetup = account.login();

        if (!accountSetup) {
            MutableComponent failed = new TextComponent(
                            "Welps! Trying to connect and set up the Wynntils Account with your data has failed. "
                                    + "Most notably, cloud config syncing will not work. To try this action again, run ")
                    .withStyle(ChatFormatting.GREEN);
            failed.append(new TextComponent("/wynntils reload")
                    .withStyle(Style.EMPTY
                            .withColor(ChatFormatting.AQUA)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wynntils reload"))));

            if (McUtils.player() == null) {
                WynntilsMod.error(ComponentUtils.getUnformatted(failed));
                return;
            }

            McUtils.sendMessageToClient(failed);
        }
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

    public static void updateDiscoveries() {
        if (apiUrls == null) return;

        String url = apiUrls.get("Discoveries");
        handler.addRequest(new RequestBuilder(url, "discoveries")
                .cacheTo(new File(API_CACHE_ROOT, "discoveries.json"))
                .handleJsonArray(discoveriesJson -> {
                    Type type = new TypeToken<ArrayList<DiscoveryProfile>>() {}.getType();

                    List<DiscoveryProfile> discoveries = gson.fromJson(discoveriesJson, type);
                    discoveryInfoList =
                            discoveries.stream().map(DiscoveryInfo::new).toList();
                    return true;
                })
                .build());
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

    public static boolean isAthenaOnline() {
        return (account != null && account.isConnected());
    }

    public static List<DiscoveryInfo> getDiscoveryInfoList() {
        return discoveryInfoList;
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

    public static Optional<WynntilsAccount> getAccount() {
        return Optional.ofNullable(account);
    }

    public static RequestHandler getHandler() {
        return handler;
    }
}
