/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo;

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
import com.wynntils.models.gearinfo.parsing.GearItemStackParser;
import com.wynntils.models.gearinfo.parsing.GearJsonLoreParser;
import com.wynntils.models.gearinfo.type.GearInstance;
import com.wynntils.models.gearinfo.type.GearMajorId;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.utils.JsonUtils;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import net.minecraft.world.item.ItemStack;

/**
 * Gear and stats are complex, have lots of corner cases and suffer from a general
 * lack of comprehensible, exhaustive, correct and authoritive documentation. :-(
 *
 * Here is a collection of generally helpful links:
 *
 * 2016 Guide: https://forums.wynncraft.com/threads/how-identifications-are-calculated.128923/
 * 2019 Guide: https://forums.wynncraft.com/threads/stats-and-identifications-guide.246308/
 * The Damage Bible: https://docs.google.com/document/d/1BXdLrMWj-BakPcAWnuqvSFbwiz7oGTOMcEEdC5vCWs4
 * WynnBuilder "Wynnfo": https://hppeng-wynn.github.io/wynnfo/, especially
 * Damage Calculations: https://hppeng-wynn.github.io/wynnfo/pdfs/Damage_calculation.pdf
 */
public final class GearInfoModel extends Model {
    private static final Gson GEAR_INFO_GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(GearInfo.class, new GearInfoDeserializer())
            .registerTypeHierarchyAdapter(GearMajorId.class, new GearMajorIdDeserializer())
            .create();

    private List<GearInfo> gearInfoRegistry = List.of();
    private Map<String, GearInfo> gearInfoLookup = new HashMap<>();
    private List<GearMajorId> majorIds;
    private GearItemStackParser gearItemStackParser = new GearItemStackParser();
    private GearJsonLoreParser gearJsonLoreParser = new GearJsonLoreParser();
    private GearChatEncoding gearChatEncoding = new GearChatEncoding();

    public GearInfoModel() {
        // FIXME: We are dependent on Stats model!!!!

        loadGearInfoRegistry();
    }

    public GearInstance fromItemStack(GearInfo gearInfo, ItemStack itemStack) {
        return gearItemStackParser.fromItemStack(gearInfo, itemStack);
    }

    public List<GearInfo> getGearInfoRegistry() {
        return gearInfoRegistry;
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
        // We must download and parse Major IDs before attempting to parse the gear DB
        Download majorIdsDl = Managers.Net.download(UrlId.DATA_STATIC_MAJOR_IDS);
        majorIdsDl.handleReader(majorIdsReader -> {
            Type type = new TypeToken<List<GearMajorId>>() {}.getType();
            majorIds = GEAR_INFO_GSON.fromJson(majorIdsReader, type);

            // Now we can do the gear DB
            Download dl = Managers.Net.download(UrlId.DATA_STATIC_GEAR);
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

    public GearItem fromJsonLore(ItemStack itemStack, GearInfo gearInfo) {
        return gearJsonLoreParser.fromJsonLore(itemStack, gearInfo);
    }

    public String toEncodedString(GearItem gearItem) {
        return gearChatEncoding.toEncodedString(gearItem);
    }

    public GearItem fromEncodedString(String encoded) {
        return gearChatEncoding.fromEncodedString(encoded);
    }

    public Matcher gearChatEncodingMatcher(String text) {
        return gearChatEncoding.gearChatEncodingMatcher(text);
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
