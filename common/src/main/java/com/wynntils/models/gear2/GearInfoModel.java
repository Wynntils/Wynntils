/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear2;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.gear2.stats.DamageStatBuilder;
import com.wynntils.models.gear2.stats.DefenceStatBuilder;
import com.wynntils.models.gear2.stats.MiscStatBuilder;
import com.wynntils.models.gear2.stats.SpellStatBuilder;
import com.wynntils.models.gear2.stats.StatBuilder;
import com.wynntils.models.gear2.types.GearMajorId;
import com.wynntils.models.gear2.types.GearStat;
import com.wynntils.utils.JsonUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GearInfoModel extends Model {
    private static final Gson GEAR_INFO_GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(GearInfo.class, new GearInfoDeserializer())
            .registerTypeHierarchyAdapter(GearMajorId.class, new GearMajorIdDeserializer())
            .create();

    private static final List<StatBuilder> STAT_BUILDERS =
            List.of(new MiscStatBuilder(), new DefenceStatBuilder(), new SpellStatBuilder(), new DamageStatBuilder());

    public final List<GearStat> gearStatRegistry = new ArrayList<>();
    public final Map<String, GearStat> gearStatLookup = new HashMap<>();
    private List<GearInfo> gearInfoRegistry = List.of();
    private Map<String, GearInfo> gearInfoLookup = new HashMap<>();
    private List<GearMajorId> majorIds;

    public GearInfoModel() {
        for (StatBuilder builder : STAT_BUILDERS) {
            builder.buildStats(gearStatRegistry::add);
        }

        // Create a fast lookup map
        for (GearStat stat : gearStatRegistry) {
            String lookupName = stat.displayName() + stat.unit().getDisplayName();
            gearStatLookup.put(lookupName, stat);
        }

        loadGearInfoRegistry();
    }

    public GearStat getGearStat(String displayName, String unit) {
        String lookupName = displayName + (unit == null ? "" : unit);
        return gearStatLookup.get(lookupName);
    }

    public GearInfo getGearInfo(String gearName) {
        return gearInfoLookup.get(gearName);
    }

    public GearMajorId getMajorIdFromId(String majorIdId) {
        // Check the "id" field of the "majorId", hence "majodIdId"
        return majorIds.stream()
                .filter(mId -> mId.id().equals(majorIdId))
                .findFirst()
                .orElse(null);
    }

    private void loadGearInfoRegistry() {
        Download majorIdsDl = Managers.Net.download(UrlId.DATA_STATIC_MAJOR_IDS);
        majorIdsDl.handleReader(majorIdsReader -> {
            Type type = new TypeToken<List<GearMajorId>>() {}.getType();
            majorIds = GEAR_INFO_GSON.fromJson(majorIdsReader, type);

            // We must download and parse Major IDs before attempting to parse the gear DB
            Download dl = Managers.Net.download(UrlId.DATA_WYNNCRAFT_GEARS);
            dl.handleReader(reader -> {
                WynncraftGearInfoResponse gearInfoResponse =
                        GEAR_INFO_GSON.fromJson(reader, WynncraftGearInfoResponse.class);

                // Remove the dummy "default" entry
                List<GearInfo> registry = gearInfoResponse.items.stream()
                        .filter(gearInfo -> !gearInfo.name().equals("default"))
                        .toList();

                // Create a fast lookup map
                Map<String, GearInfo> lookupMap = new HashMap<>();
                for (GearInfo gearInfo : registry) {
                    lookupMap.put(gearInfo.name(), gearInfo);
                }

                // Make it visisble to the world
                gearInfoRegistry = registry;
                gearInfoLookup = lookupMap;
            });
        });
    }

    public GearStat getGearStatFromLore(String id) {
        for (GearStat stat : gearStatRegistry) {
            if (stat.loreName().equals(id)) return stat;
        }
        return null;
    }

    private static class WynncraftGearInfoResponse {
        List<GearInfo> items;
    }

    private static class GearMajorIdDeserializer implements JsonDeserializer<GearMajorId> {
        @Override
        public GearMajorId deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            return new GearMajorId(
                    JsonUtils.getNullableJsonString(json, "id"),
                    JsonUtils.getNullableJsonString(json, "name"),
                    JsonUtils.getNullableJsonString(json, "lore"));
        }
    }
}
