/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.territory;

import com.wynntils.core.managers.Model;
import com.wynntils.core.webapi.TerritoryManager;
import com.wynntils.wynn.objects.profiles.TerritoryProfile;
import com.wynntils.mc.event.AdvancementUpdateEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.model.map.poi.TerritoryPoi;
import com.wynntils.wynn.model.territory.objects.GuildTerritoryInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuildTerritoryModel extends Model {
    private static Map<String, TerritoryPoi> guildTerritoryHashMap = new ConcurrentHashMap<>();

    public static void init() {
        guildTerritoryHashMap = new ConcurrentHashMap<>();
    }

    public static void disable() {
        guildTerritoryHashMap = Map.of();
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
            TerritoryProfile territoryProfile =
                    TerritoryManager.getTerritories().get(entry.getKey());

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
}
