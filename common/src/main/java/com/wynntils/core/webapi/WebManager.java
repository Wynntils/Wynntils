/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.webapi.account.WynntilsAccount;
import com.wynntils.core.webapi.profiles.DiscoveryProfile;
import com.wynntils.core.webapi.profiles.ItemGuessProfile;
import com.wynntils.core.webapi.profiles.ServerProfile;
import com.wynntils.core.webapi.profiles.ingredient.IngredientProfile;
import com.wynntils.core.webapi.profiles.item.IdentificationProfile;
import com.wynntils.core.webapi.profiles.item.ItemProfile;
import com.wynntils.core.webapi.profiles.item.ItemType;
import com.wynntils.core.webapi.profiles.item.MajorIdentification;
import com.wynntils.core.webapi.request.Request;
import com.wynntils.core.webapi.request.RequestBuilder;
import com.wynntils.core.webapi.request.RequestHandler;
import com.wynntils.mc.event.WebSetupEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.Utils;
import com.wynntils.wynn.event.DiscoveriesUpdatedEvent;
import com.wynntils.wynn.item.IdentificationOrderer;
import com.wynntils.wynn.objects.account.PlayerAccount;
import java.io.File;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

/**
 * Provides and loads web content on demand
 */
public final class WebManager extends CoreManager {

    public static final File API_CACHE_ROOT = WynntilsMod.getModStorageDir("apicache");
    private static final int REQUEST_TIMEOUT_MILLIS = 10000;

    private static boolean setup = false;
    private static final RequestHandler handler = new RequestHandler();

    private static WebReader apiUrls = null;

    private static final Gson gson = new Gson();

    private static HashMap<String, ItemProfile> items = new HashMap<>();
    private static Collection<ItemProfile> directItems = new ArrayList<>();
    private static HashMap<String, ItemGuessProfile> itemGuesses = new HashMap<>();
    private static HashMap<String, String> translatedReferences = new HashMap<>();
    private static HashMap<String, String> internalIdentifications = new HashMap<>();
    private static HashMap<String, MajorIdentification> majorIds = new HashMap<>();
    private static HashMap<ItemType, String[]> materialTypes = new HashMap<>();

    private static HashMap<String, IngredientProfile> ingredients = new HashMap<>();
    private static Collection<IngredientProfile> directIngredients = new ArrayList<>();
    private static HashMap<String, String> ingredientHeadTextures = new HashMap<>();
    private static PlayerAccount playerAccount = null;

    private static List<DiscoveryProfile> discoveries = new ArrayList<>();

    private static String currentSplash = "";

    private static WynntilsAccount account = null;

    private static final String USER_AGENT = String.format(
            "Wynntils Artemis\\%s (%s) %s",
            WynntilsMod.getVersion(),
            WynntilsMod.isDevelopmentEnvironment() ? "dev" : "client",
            WynntilsMod.getModLoader());

    public static void init() {
        tryReloadApiUrls(false);
        setupUserAccount();

        WebManager.updateCurrentSplash();

        loadCommonObjects();
    }

    private static void loadCommonObjects() {
        WebManager.tryLoadItemList();
        WebManager.tryLoadItemGuesses();
        WebManager.tryLoadIngredientList();
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

    private static void tryLoadItemGuesses() {
        if (apiUrls == null || !apiUrls.hasKey("ItemGuesses")) return;
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
    }

    public static void updatePlayerStats() {
        String url =
                "https://api.wynncraft.com/v2/player/" + McUtils.mc().getUser().getName() + "/stats";
        handler.addAndDispatch(new RequestBuilder(url, "player_profile")
                .cacheTo(new File(API_CACHE_ROOT, "player_stats.json"))
                .handleJsonObject(json -> {
                    Type type = new TypeToken<PlayerAccount>() {}.getType();

                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(type, new PlayerAccount.PlayerAccountDeserializer());
                    Gson gson = gsonBuilder.create();

                    playerAccount = gson.fromJson(json, type);
                    return true;
                })
                .useCacheAsBackup()
                .build());
    }

    private static void tryLoadItemList() {
        if (apiUrls == null || !apiUrls.hasKey("Athena")) return;
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

                    // FIXME: We should not be doing Singleton housekeeping for IdentificationOrderer!
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
    }

    public static void tryLoadIngredientList() {
        if (apiUrls == null || !apiUrls.hasKey("Athena")) return;

        handler.addRequest(new RequestBuilder(apiUrls.get("Athena") + "/cache/get/ingredientList", "ingredientList")
                .cacheTo(new File(API_CACHE_ROOT, "ingredient_list.json"))
                .useCacheAsBackup()
                .handleJsonObject(j -> {
                    Type hashmapType = new TypeToken<HashMap<String, String>>() {}.getType();
                    ingredientHeadTextures = gson.fromJson(j.getAsJsonObject("headTextures"), hashmapType);

                    IngredientProfile[] gItems =
                            gson.fromJson(j.getAsJsonArray("ingredients"), IngredientProfile[].class);
                    HashMap<String, IngredientProfile> cingredients = new HashMap<>();

                    for (IngredientProfile prof : gItems) {
                        cingredients.put(prof.getDisplayName(), prof);
                    }

                    ingredients = cingredients;
                    directIngredients = cingredients.values();

                    return true;
                })
                .build());
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

    /**
     * Request all online players to WynnAPI
     *
     * @return a {@link HashMap} who the key is the server and the value is an array containing all
     * players on it
     * @throws IOException thrown by URLConnection
     */
    public static Map<String, List<String>> getOnlinePlayers() throws IOException {
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

    public static void getServerList(Consumer<HashMap<String, ServerProfile>> onReceive) {
        if (apiUrls == null || !isAthenaOnline()) return;
        String url = apiUrls.get("Athena") + "/cache/get/serverList";

        Request request = new RequestBuilder(url, "serverList")
                .handleJsonObject((con, json) -> {
                    JsonObject servers = json.getAsJsonObject("servers");
                    HashMap<String, ServerProfile> result = new HashMap<>();

                    long serverTime = Long.parseLong(con.getHeaderField("timestamp"));
                    for (Map.Entry<String, JsonElement> entry : servers.entrySet()) {
                        ServerProfile profile = gson.fromJson(entry.getValue(), ServerProfile.class);
                        profile.matchTime(serverTime);

                        result.put(entry.getKey(), profile);
                    }

                    onReceive.accept(result);
                    return true;
                })
                .build();
        handler.addAndDispatch(request, true);
    }

    public static void updateDiscoveries() {
        if (apiUrls == null) return;

        String url = apiUrls.get("Discoveries");
        handler.addRequest(new RequestBuilder(url, "discoveries")
                .cacheTo(new File(API_CACHE_ROOT, "discoveries.json"))
                .handleJsonArray(discoveriesJson -> {
                    Type type = new TypeToken<ArrayList<DiscoveryProfile>>() {}.getType();

                    discoveries = gson.fromJson(discoveriesJson, type);
                    WynntilsMod.postEvent(new DiscoveriesUpdatedEvent.Api());
                    return true;
                })
                .build());
    }

    private static void updateCurrentSplash() {
        if (apiUrls == null || apiUrls.getList("Splashes") == null) return;

        List<String> splashes = apiUrls.getList("Splashes");
        currentSplash = splashes.get(Utils.getRandom().nextInt(splashes.size()));
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

    public static HashMap<String, ItemGuessProfile> getItemGuesses() {
        return itemGuesses;
    }

    public static Collection<ItemProfile> getItemsCollection() {
        return directItems;
    }

    public static HashMap<String, ItemProfile> getItemsMap() {
        return items;
    }

    public static HashMap<ItemType, String[]> getMaterialTypes() {
        return materialTypes;
    }

    public static HashMap<String, MajorIdentification> getMajorIds() {
        return majorIds;
    }

    public static HashMap<String, String> getInternalIdentifications() {
        return internalIdentifications;
    }

    public static HashMap<String, String> getTranslatedReferences() {
        return translatedReferences;
    }

    public static Collection<IngredientProfile> getIngredientsCollection() {
        return directIngredients;
    }

    public static HashMap<String, IngredientProfile> getIngredients() {
        return ingredients;
    }

    public static HashMap<String, String> getIngredientHeadTextures() {
        return ingredientHeadTextures;
    }

    public static PlayerAccount getPlayerAccount() {
        return playerAccount;
    }

    public static List<DiscoveryProfile> getDiscoveries() {
        return discoveries;
    }

    public static String getUserAgent() {
        return USER_AGENT;
    }

    public static boolean isSetup() {
        return setup;
    }

    public static String getCurrentSplash() {
        return currentSplash;
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
