/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.webapi.account.WynntilsAccount;
import com.wynntils.core.webapi.profiles.ItemGuessProfile;
import com.wynntils.core.webapi.profiles.MapProfile;
import com.wynntils.core.webapi.profiles.TerritoryProfile;
import com.wynntils.core.webapi.profiles.item.IdentificationProfile;
import com.wynntils.core.webapi.profiles.item.ItemProfile;
import com.wynntils.core.webapi.profiles.item.ItemType;
import com.wynntils.core.webapi.profiles.item.MajorIdentification;
import com.wynntils.core.webapi.request.RequestBuilder;
import com.wynntils.core.webapi.request.RequestHandler;
import com.wynntils.mc.EventFactory;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.objects.MD5Verification;
import com.wynntils.wc.utils.IdentificationOrderer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/** Provides and loads web content on demand */
public class WebManager {
    public static final File API_CACHE_ROOT = WynntilsMod.getModStorageDir("apicache");
    private static final int REQUEST_TIMEOUT_MILLIS = 16000;

    private static boolean setup = false;
    private static final RequestHandler handler = new RequestHandler();

    public static @Nullable WebReader apiUrls = null;

    private static final Gson gson = new Gson();

    private static @Nullable HashMap<String, ItemProfile> items = null;
    private static @Nullable Collection<ItemProfile> directItems = null;
    private static @Nullable HashMap<String, ItemGuessProfile> itemGuesses = null;
    private static @Nullable HashMap<String, String> translatedReferences = null;
    private static @Nullable HashMap<String, String> internalIdentifications = null;
    private static @Nullable HashMap<String, MajorIdentification> majorIds = null;
    private static @Nullable HashMap<ItemType, String[]> materialTypes = null;

    private static TerritoryUpdateThread territoryUpdateThread;
    private static final HashMap<String, TerritoryProfile> territories = new HashMap<>();

    private static @Nullable WynntilsAccount account = null;

    private static @Nullable MapProfile map = null;

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

        // tryLoadItemGuesses
        itemGuesses = null;

        // tryLoadItemList
        items = null;
        directItems = null;
        translatedReferences = null;
        internalIdentifications = null;
        majorIds = null;
        materialTypes = null;

        // tryLoadTerritories
        territories.clear();

        updateTerritoryThreadStatus(false);
    }

    public static void setupUserAccount() {
        if (isLoggedIn()) return;

        account = new WynntilsAccount();
        boolean accountSetup = account.login();

        if (!accountSetup) {
            MutableComponent failed = new TextComponent(
                            "Welps! Trying to connect and set up the Wynntils Account with your data has failed. "
                                    + "Most notably, configs will not be loaded. To try this action again, run ")
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

    public static boolean tryLoadTerritories() {
        return tryLoadTerritories(handler);
    }

    public static boolean tryLoadTerritories(RequestHandler handler) {
        if (apiUrls == null || !apiUrls.hasKey("Athena")) return false;
        String url = apiUrls.get("Athena") + "/cache/get/territoryList";
        handler.addAndDispatch(new RequestBuilder(url, "territory")
                .cacheTo(new File(API_CACHE_ROOT, "territories.json"))
                .handleJsonObject(json -> {
                    if (!json.has("territories")) return false;

                    Type type = new TypeToken<HashMap<String, TerritoryProfile>>() {}.getType();

                    GsonBuilder builder = new GsonBuilder();
                    builder.registerTypeHierarchyAdapter(
                            TerritoryProfile.class, new TerritoryProfile.TerritoryDeserializer());
                    Gson gson = builder.create();

                    territories.clear();
                    territories.putAll(gson.fromJson(json.get("territories"), type));
                    return true;
                })
                .build());

        return isTerritoryListLoaded();
    }

    public static void updateTerritoryThreadStatus(boolean start) {
        if (start) {
            if (territoryUpdateThread == null) {
                territoryUpdateThread = new TerritoryUpdateThread("Territory Update Thread");
                territoryUpdateThread.start();
                return;
            }
            return;
        }

        if (territoryUpdateThread != null) {
            territoryUpdateThread.interrupt();
        }
        territoryUpdateThread = null;
    }

    public static boolean tryLoadItemGuesses() {
        if (apiUrls == null || !apiUrls.hasKey("ItemGuesses")) return false;
        handler.addAndDispatch(new RequestBuilder(apiUrls.get("ItemGuesses"), "item_guesses")
                .cacheTo(new File(API_CACHE_ROOT, "item_guesses.json"))
                .handleJsonObject(json -> {
                    Type type = new TypeToken<HashMap<String, ItemGuessProfile>>() {}.getType();

                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeHierarchyAdapter(
                            HashMap.class, new ItemGuessProfile.ItemGuessDeserializer());
                    Gson gson = gsonBuilder.create();

                    itemGuesses = new HashMap<>();
                    itemGuesses.putAll(gson.fromJson(json, type));

                    return true;
                })
                .useCacheAsBackup()
                .build());

        // Check for success
        return isItemGuessesLoaded();
    }

    public static boolean tryLoadItemList() {
        if (apiUrls == null || !apiUrls.hasKey("Athena")) return false;
        handler.addAndDispatch(new RequestBuilder(apiUrls.get("Athena") + "/cache/get/itemList", "item_list")
                .cacheTo(new File(API_CACHE_ROOT, "item_list.json"))
                .handleJsonObject(json -> {
                    Type hashmapType = new TypeToken<HashMap<String, String>>() {}.getType();
                    translatedReferences = gson.fromJson(json.getAsJsonObject("translatedReferences"), hashmapType);
                    internalIdentifications =
                            gson.fromJson(json.getAsJsonObject("internalIdentifications"), hashmapType);

                    Type majorIdsType = new TypeToken<HashMap<String, MajorIdentification>>() {}.getType();
                    majorIds = gson.fromJson(json.getAsJsonObject("majorIdentifications"), majorIdsType);
                    Type materialTypesType = new TypeToken<HashMap<ItemType, String[]>>() {}.getType();
                    materialTypes = gson.fromJson(json.getAsJsonObject("materialTypes"), materialTypesType);

                    IdentificationOrderer.INSTANCE =
                            gson.fromJson(json.getAsJsonObject("identificationOrder"), IdentificationOrderer.class);

                    ItemProfile[] gItems = gson.fromJson(json.getAsJsonArray("items"), ItemProfile[].class);

                    HashMap<String, ItemProfile> citems = new HashMap<>();
                    for (ItemProfile prof : gItems) {
                        prof.getStatuses().values().forEach(IdentificationProfile::calculateMinMax);
                        prof.addMajorIds(majorIds);
                        citems.put(prof.getDisplayName(), prof);
                    }

                    citems.values().forEach(ItemProfile::registerIdTypes);

                    directItems = citems.values();
                    items = citems;

                    return true;
                })
                .useCacheAsBackup()
                .build());

        // Check for success
        return isItemListLoaded();
    }

    public static CompletableFuture<Boolean> tryLoadMap() {
        if (apiUrls == null || !apiUrls.hasKey("MainMap")) return CompletableFuture.completedFuture(false);

        final File mapDirectory = new File(API_CACHE_ROOT, "map");

        mapDirectory.mkdirs();

        final CompletableFuture<Boolean> result = new CompletableFuture<>();

        handler.addAndDispatch(new RequestBuilder(apiUrls.get("MainMap"), "main_map.info")
                .cacheTo(new File(mapDirectory, "main-map.txt"))
                .handleWebReader(reader -> {
                    double rightX = Double.parseDouble(reader.get("CenterX"));
                    double rightZ = Double.parseDouble(reader.get("CenterZ"));

                    final String md5 = reader.get("MD5");

                    final File mapFile = new File(mapDirectory, "main-map.png");

                    // hack: Caching is done manually
                    if (mapFile.exists() && new MD5Verification(mapFile).equals(md5)) {
                        NativeImage nativeImage;
                        try (FileInputStream in = new FileInputStream(mapFile)) {
                            nativeImage = NativeImage.read(in);
                        } catch (IOException e) {
                            e.printStackTrace();
                            result.complete(false);
                            return true;
                        }

                        ResourceLocation resource = new ResourceLocation("wynntils", "main-map.png");

                        McUtils.mc().getTextureManager().register(resource, new DynamicTexture(nativeImage));

                        map = new MapProfile(resource, rightX, rightZ, nativeImage.getWidth(), nativeImage.getHeight());

                        result.complete(true);
                        return true;
                    }

                    // TODO DownloaderManager?
                    handler.addAndDispatchAsync(new RequestBuilder(reader.get("DownloadLocation"), "main_map.png")
                            .onError(() -> result.complete(false))
                            .useCacheAsBackup()
                            .handle(bytes -> {
                                NativeImage nativeImage;

                                try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
                                    nativeImage = NativeImage.read(in);
                                    nativeImage.writeToFile(mapFile); // cache
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    result.complete(false);
                                    return true;
                                }

                                ResourceLocation resource = new ResourceLocation("wynntils", "main-map.png");

                                McUtils.mc()
                                        .getTextureManager()
                                        .register(resource, new DynamicTexture(nativeImage));

                                map = new MapProfile(
                                        resource, rightX, rightZ, nativeImage.getWidth(), nativeImage.getHeight());

                                result.complete(true);
                                return true;
                            })
                            .build());

                    return true;
                })
                .build());

        return result;
    }

    public static boolean tryReloadApiUrls(boolean async) {
        handler.addRequest(new RequestBuilder("https://api.wynntils.com/webapi", "webapi")
                .cacheTo(new File(API_CACHE_ROOT, "webapi.txt"))
                .handleWebReader(reader -> {
                    apiUrls = reader;
                    if (!setup) {
                        setup = true;
                    }

                    EventFactory.onWebSetup();
                    return true;
                })
                .build());

        handler.dispatch(async);

        tryLoadMap();

        return setup;
    }

    /**
     * Request all online players to WynnAPI
     *
     * @return a {@link HashMap} who the key is the server and the value is an array containing all
     *     players on it
     * @throws IOException thrown by URLConnection
     */
    public static HashMap<String, List<String>> getOnlinePlayers() throws IOException {
        if (apiUrls == null || !apiUrls.hasKey("OnlinePlayers")) return new HashMap<>();

        URLConnection st = generateURLRequest(apiUrls.get("OnlinePlayers"));
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

    private static URLConnection generateURLRequest(String url) throws IOException {
        URLConnection st = new URL(url).openConnection();
        st.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316" + " Firefox/3.6.2");
        if (apiUrls != null && apiUrls.hasKey("WynnApiKey")) st.setRequestProperty("apikey", apiUrls.get("WynnApiKey"));
        st.setConnectTimeout(REQUEST_TIMEOUT_MILLIS);
        st.setReadTimeout(REQUEST_TIMEOUT_MILLIS);

        return st;
    }

    public static boolean isItemGuessesLoaded() {
        return itemGuesses != null;
    }

    public static boolean isItemListLoaded() {
        return items != null
                && directItems != null
                && translatedReferences != null
                && internalIdentifications != null
                && majorIds != null
                && materialTypes != null;
    }

    public static boolean isTerritoryListLoaded() {
        return !territories.isEmpty();
    }

    public static boolean isMapLoaded() {
        return map != null;
    }

    public static @Nullable HashMap<String, ItemGuessProfile> getItemGuesses() {
        return itemGuesses;
    }

    public static @Nullable Collection<ItemProfile> getItemsCollection() {
        return directItems;
    }

    public static @Nullable HashMap<String, ItemProfile> getItemsMap() {
        return items;
    }

    public static @Nullable HashMap<ItemType, String[]> getMaterialTypes() {
        return materialTypes;
    }

    public static @Nullable HashMap<String, MajorIdentification> getMajorIds() {
        return majorIds;
    }

    public static @Nullable HashMap<String, String> getInternalIdentifications() {
        return internalIdentifications;
    }

    public static @Nullable HashMap<String, String> getTranslatedReferences() {
        return translatedReferences;
    }

    public static HashMap<String, TerritoryProfile> getTerritories() {
        return territories;
    }

    public static boolean isSetup() {
        return setup;
    }

    public static @Nullable WebReader getApiUrls() {
        return apiUrls;
    }

    public static @Nullable WynntilsAccount getAccount() {
        return account;
    }

    public static RequestHandler getHandler() {
        return handler;
    }
}
