/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.territory;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.request.RequestBuilder;
import com.wynntils.core.webapi.request.RequestHandler;
import com.wynntils.mc.event.AdvancementUpdateEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.model.map.poi.Poi;
import com.wynntils.wynn.model.map.poi.TerritoryPoi;
import com.wynntils.wynn.model.territory.objects.TerritoryInfo;
import com.wynntils.wynn.objects.profiles.TerritoryProfile;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TerritoryManager extends CoreManager {
    private static final int TERRITORY_UPDATE_MS = 15000;
    private static final Gson TERRITORY_PROFILE_GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(TerritoryProfile.class, new TerritoryProfile.TerritoryDeserializer())
            .create();

    // This is territory POIs as returned by the advancement from Wynncraft
    private static Map<String, TerritoryPoi> territoryPoiMap = new ConcurrentHashMap<>();

    // This is the profiles as downloaded from Athena
    private static Map<String, TerritoryProfile> territoryProfileMap = new HashMap<>();

    // This is just a cache of TerritoryPois created for all territoryProfileMap values
    private static Set<TerritoryPoi> allTerritoryPois = new HashSet<>();

    private static TerritoryUpdateThread territoryUpdateThread;

    public static void init() {
        territoryUpdateThread = new TerritoryUpdateThread("Territory Update Thread");
        territoryUpdateThread.start();
    }

    public static TerritoryProfile getTerritoryProfile(String name) {
        return territoryProfileMap.get(name);
    }

    public static Stream<String> getTerritoryNames() {
        return territoryProfileMap.keySet().stream();
    }

    public static Set<TerritoryPoi> getTerritoryPois() {
        return allTerritoryPois;
    }

    public static List<Poi> getTerritoryPoisFromAdvancement() {
        return new ArrayList<>(territoryPoiMap.values());
    }

    public static TerritoryPoi getTerritoryPoiFromAdvancement(String name) {
        return territoryPoiMap.get(name);
    }

    @SubscribeEvent
    public static void onAdvancementUpdate(AdvancementUpdateEvent event) {
        Map<String, TerritoryInfo> tempMap = new HashMap<>();

        for (Map.Entry<ResourceLocation, Advancement.Builder> added :
                event.getAdded().entrySet()) {
            added.getValue().parent((ResourceLocation) null);
            Advancement built = added.getValue().build(added.getKey());

            if (built.getDisplay() == null) continue;

            String territoryName = ComponentUtils.getUnformatted(
                            built.getDisplay().getTitle())
                    .replace("[", "")
                    .replace("]", "")
                    .trim();

            // Do not parse same thing twice
            if (tempMap.containsKey(territoryName)) continue;

            // ignore empty display texts they are used to generate the "lines"
            if (territoryName.isEmpty()) continue;

            // headquarters frame is challenge
            boolean headquarters = built.getDisplay().getFrame() == FrameType.CHALLENGE;

            // description is a raw string with \n, so we have to split
            String description = ComponentUtils.getCoded(built.getDisplay().getDescription());
            String[] colored = description.split("\n");
            String[] raw = ComponentUtils.stripFormatting(description).split("\n");

            TerritoryInfo container = new TerritoryInfo(raw, colored, headquarters);
            tempMap.put(territoryName, container);
        }

        for (Map.Entry<String, TerritoryInfo> entry : tempMap.entrySet()) {
            TerritoryProfile territoryProfile = getTerritoryProfile(entry.getKey());

            if (territoryProfile == null) continue;

            territoryPoiMap.put(entry.getKey(), new TerritoryPoi(territoryProfile, entry.getValue()));
        }
    }

    public static boolean isTerritoryListLoaded() {
        return !territoryProfileMap.isEmpty();
    }

    public static boolean updateTerritoryProfileMap() {
        tryLoadTerritories(WebManager.getHandler());
        return isTerritoryListLoaded();
    }

    private static void tryLoadTerritories(RequestHandler handler) {
        if (WebManager.getApiUrls().isEmpty() || !WebManager.getApiUrls().get().hasKey("Athena")) return;

        String url = WebManager.getApiUrls().get().get("Athena") + "/cache/get/territoryList";

        handler.addAndDispatch(new RequestBuilder(url, "territory")
                .cacheTo(new File(WebManager.API_CACHE_ROOT, "territories.json"))
                .handleJsonObject(json -> {
                    if (!json.has("territories")) return false;

                    Type type = new TypeToken<HashMap<String, TerritoryProfile>>() {}.getType();
                    territoryProfileMap = TERRITORY_PROFILE_GSON.fromJson(json.get("territories"), type);
                    allTerritoryPois = territoryProfileMap.values().stream()
                            .map(TerritoryPoi::new)
                            .collect(Collectors.toSet());
                    return true;
                })
                .build());
    }

    private static class TerritoryUpdateThread extends Thread {
        public TerritoryUpdateThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            RequestHandler handler = new RequestHandler();

            try {
                Thread.sleep(TERRITORY_UPDATE_MS);
                while (!isInterrupted()) {
                    tryLoadTerritories(handler);
                    handler.dispatch();

                    // TODO: Add events
                    Thread.sleep(TERRITORY_UPDATE_MS);
                }
            } catch (InterruptedException ignored) {
            }

            WynntilsMod.info("Terminating territory update thread.");
        }
    }
}
