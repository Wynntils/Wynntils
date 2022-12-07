/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.webapi.request.RequestBuilder;
import com.wynntils.core.webapi.request.RequestHandler;
import com.wynntils.wynn.model.map.poi.TerritoryPoi;
import com.wynntils.wynn.objects.profiles.TerritoryProfile;
import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TerritoryManager extends CoreManager {
    private static TerritoryUpdateThread territoryUpdateThread;
    private static Map<String, TerritoryProfile> territories = new HashMap<>();
    private static Set<TerritoryPoi> territoryPois = new HashSet<>();

    public static void init() {
        reset();

        updateTerritoryThreadStatus(true);
    }

    public static void disable() {
        reset();
    }

    public static boolean tryLoadTerritories() {
        return tryLoadTerritories(WebManager.getHandler());
    }

    public static boolean tryLoadTerritories(RequestHandler handler) {
        if (WebManager.getApiUrls().isEmpty() || !WebManager.getApiUrls().get().hasKey("Athena")) return false;

        String url = WebManager.getApiUrls().get().get("Athena") + "/cache/get/territoryList";

        handler.addAndDispatch(new RequestBuilder(url, "territory")
                .cacheTo(new File(WebManager.API_CACHE_ROOT, "territories.json"))
                .handleJsonObject(json -> {
                    if (!json.has("territories")) return false;

                    Type type = new TypeToken<HashMap<String, TerritoryProfile>>() {}.getType();

                    GsonBuilder builder = new GsonBuilder();
                    builder.registerTypeHierarchyAdapter(
                            TerritoryProfile.class, new TerritoryProfile.TerritoryDeserializer());
                    Gson gson = builder.create();

                    territories = gson.fromJson(json.get("territories"), type);
                    territoryPois =
                            territories.values().stream().map(TerritoryPoi::new).collect(Collectors.toSet());
                    return true;
                })
                .build());

        return isTerritoryListLoaded();
    }

    private static void updateTerritoryThreadStatus(boolean start) {
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

    private static void reset() {
        // tryLoadTerritories
        territories.clear();
        territoryPois.clear();

        updateTerritoryThreadStatus(false);
    }

    public static boolean isTerritoryListLoaded() {
        return !territories.isEmpty();
    }

    public static Map<String, TerritoryProfile> getTerritories() {
        return territories;
    }

    public static Set<TerritoryPoi> getTerritoryPois() {
        return territoryPois;
    }

    private static class TerritoryUpdateThread extends Thread {
        private static final int TERRITORY_UPDATE_MS = 15000;

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
