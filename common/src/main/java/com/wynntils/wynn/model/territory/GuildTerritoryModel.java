/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.territory;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Model;
import com.wynntils.core.net.Reference;
import com.wynntils.core.net.downloader.DownloadableResource;
import com.wynntils.core.net.downloader.Downloader;
import com.wynntils.mc.event.AdvancementUpdateEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.model.map.poi.TerritoryPoi;
import com.wynntils.wynn.model.territory.objects.GuildTerritoryInfo;
import com.wynntils.wynn.netresources.profiles.TerritoryProfile;
import java.io.File;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuildTerritoryModel extends Model {
    private static final int TERRITORY_UPDATE_MS = 15000;

    private static Map<String, TerritoryPoi> guildTerritoryHashMap = new ConcurrentHashMap<>();
    private static Map<String, TerritoryProfile> territories = new HashMap<>();
    private static Set<TerritoryPoi> territoryPois = new HashSet<>();
    private static Thread territoryUpdateThread;

    public static void init() {
        guildTerritoryHashMap = new ConcurrentHashMap<>();

        resetLoadedTerritories();
        updateTerritoryThreadStatus(true);
    }

    public static void disable() {
        guildTerritoryHashMap = Map.of();
        resetLoadedTerritories();
    }

    @SubscribeEvent
    public static void onAdvancementUpdate(AdvancementUpdateEvent event) {
        Map<String, GuildTerritoryInfo> tempMap = new HashMap<>();

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

            GuildTerritoryInfo container = new GuildTerritoryInfo(raw, colored, headquarters);
            tempMap.put(territoryName, container);
        }

        for (Map.Entry<String, GuildTerritoryInfo> entry : tempMap.entrySet()) {
            TerritoryProfile territoryProfile = getTerritories().get(entry.getKey());

            if (territoryProfile == null) continue;

            guildTerritoryHashMap.put(entry.getKey(), new TerritoryPoi(territoryProfile, entry.getValue()));
        }
    }

    public static Collection<TerritoryPoi> getGuildTerritoryPois() {
        return guildTerritoryHashMap.values();
    }

    public static Map<String, TerritoryPoi> getGuildTerritoryMap() {
        return guildTerritoryHashMap;
    }

    public static boolean tryLoadTerritories() {
        String url = Reference.URLs.getAthena() + "/cache/get/territoryList";
        DownloadableResource dl =
                Downloader.download(url, Downloader.dlFile("territories.json"), "territory");
        dl.handleJsonObject(json -> {
            if (!json.has("territories")) return false;

            Type type = new TypeToken<HashMap<String, TerritoryProfile>>() {}.getType();

            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeHierarchyAdapter(TerritoryProfile.class, new TerritoryProfile.TerritoryDeserializer());
            Gson gson = builder.create();

            territories = gson.fromJson(json.get("territories"), type);
            territoryPois = territories.values().stream().map(TerritoryPoi::new).collect(Collectors.toSet());
            return true;
        });

        return isTerritoryListLoaded();
    }

    private static void updateTerritoryThreadStatus(boolean start) {
        if (start) {
            if (territoryUpdateThread == null) {
                territoryUpdateThread = new Thread(
                        () -> {
                            try {
                                Thread.sleep(TERRITORY_UPDATE_MS);
                                while (!Thread.interrupted()) {
                                    tryLoadTerritories();

                                    // TODO: Add events
                                    Thread.sleep(TERRITORY_UPDATE_MS);
                                }
                            } catch (InterruptedException ignored) {
                            }

                            WynntilsMod.info("Terminating territory update thread.");
                        },
                        "Territory Update Thread");
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

    private static void resetLoadedTerritories() {
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
}
