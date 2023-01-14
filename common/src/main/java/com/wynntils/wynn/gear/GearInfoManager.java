/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.NetManager;
import com.wynntils.core.net.UrlId;
import com.wynntils.utils.Pair;
import com.wynntils.utils.RangedValue;
import com.wynntils.wynn.gear.stats.DamageStatBuilder;
import com.wynntils.wynn.gear.stats.DefenceStatBuilder;
import com.wynntils.wynn.gear.stats.MiscStatBuilder;
import com.wynntils.wynn.gear.stats.SpellStatBuilder;
import com.wynntils.wynn.gear.stats.StatBuilder;
import com.wynntils.wynn.gear.types.GearStat;
import com.wynntils.wynn.objects.profiles.item.GearTier;
import com.wynntils.wynn.objects.profiles.item.GearType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GearInfoManager extends Manager {
    private static final Gson GEAR_INFO_GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(GearInfo.class, new GearInfoDeserializer())
            .create();

    private static final List<StatBuilder> STAT_BUILDERS =
            List.of(new MiscStatBuilder(), new DefenceStatBuilder(), new SpellStatBuilder(), new DamageStatBuilder());

    public final List<GearStat> registry = new ArrayList<>();
    public final Map<String, GearStat> lookup = new HashMap<>();
    private List<GearInfo> allGearInfos = List.of();

    public GearInfoManager(NetManager netManager) {
        super(List.of(netManager));

        for (StatBuilder builder : STAT_BUILDERS) {
            builder.buildStats(registry::add);
        }

        // Create a fast lookup map
        for (GearStat stat : registry) {
            String lookupName = stat.displayName() + stat.unit().getDisplayName();
            lookup.put(lookupName, stat);
        }

        loadInfoProfiles();
    }

    public GearStat getGearStat(String displayName, String unit) {
        String lookupName = displayName + unit;
        return lookup.get(lookupName);
    }

    private void loadInfoProfiles() {
        Download dl = Managers.Net.download(UrlId.DATA_WYNNCRAFT_GEARS);
        dl.handleReader(reader -> {
            Type type = new TypeToken<WynncraftGearInfoResponse>() {}.getType();
            WynncraftGearInfoResponse newGearInfoList = GEAR_INFO_GSON.fromJson(reader, type);
            allGearInfos = newGearInfoList.items;
        });
    }

    private static class WynncraftGearInfoResponse {
        List<GearInfo> items;
    }

    private static class GearInfoDeserializer implements JsonDeserializer<GearInfo> {
        @Override
        public GearInfo deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            // Some names apparently has a random ֎ in them...
            String name = json.get("name").getAsString().replace("֎", "");
            GearType type = parseType(json);
            GearTier tier = GearTier.fromString(json.get("tier").getAsString());
            int powderSlots = json.get("sockets").getAsInt();

            GearMetaInfo metaInfo = parseMetaInfo(json);
            GearRequirements requirements = parseRequirements(json);
            GearStatsFixed statsFixed = parseStatsFixed(json);
            List<Pair<GearStat, RangedValue>> statsIdentified = parseStatsIdentified(json);

            return new GearInfo(name, type, tier, powderSlots, metaInfo, requirements, statsFixed, statsIdentified);
        }

        private GearType parseType(JsonObject json) {
            String category = json.get("category").getAsString();
            String typeString;
            if (category.equals("accessory")) {
                typeString = json.get("accessoryType").getAsString();
            } else {
                typeString = json.get("type").getAsString();
            }
            return GearType.fromString(typeString);
        }

        private List<Pair<GearStat, RangedValue>> parseStatsIdentified(JsonObject json) {
            return List.of();
        }

        private GearStatsFixed parseStatsFixed(JsonObject json) {
            return null;
        }

        private GearRequirements parseRequirements(JsonObject json) {
            // When reading, strip "֎" from quest name.
            // classRequirement -> str to upper
            return null;
        }

        private GearMetaInfo parseMetaInfo(JsonObject json) {
            return null;
        }
    }
}
