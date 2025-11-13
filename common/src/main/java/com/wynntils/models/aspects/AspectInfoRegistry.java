/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.aspects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.aspects.type.AspectInfo;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.wynnitem.AbstractItemInfoDeserializer;
import com.wynntils.models.wynnitem.type.ItemMaterial;
import com.wynntils.utils.type.Pair;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class AspectInfoRegistry {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(AspectInfo.class, new AspectInfoDeserializer())
            .create();

    private List<AspectInfo> aspectInfoRegistry = List.of();
    private Map<ClassType, Map<String, AspectInfo>> aspectInfoLookup = Map.of();

    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_ASPECTS).handleJsonObject(this::handleAspects);
    }

    public AspectInfo getFromClassAndDisplayName(ClassType classType, String aspectName) {
        Map<String, AspectInfo> aspectInfoMap = aspectInfoLookup.get(classType);

        if (aspectInfoMap == null) {
            WynntilsMod.warn("No aspect info found for class type: " + classType);
            return null;
        }

        return aspectInfoMap.get(aspectName);
    }

    public AspectInfo getFromDisplayName(String aspectName) {
        return Arrays.stream(ClassType.values())
                .map(classType -> getFromClassAndDisplayName(classType, aspectName))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public Stream<AspectInfo> getAllAspectInfos() {
        return aspectInfoRegistry.stream();
    }

    private void handleAspects(JsonObject json) {
        // Create fast lookup maps
        List<AspectInfo> registry = new ArrayList<>();

        Map<ClassType, Map<String, AspectInfo>> lookupMap = new HashMap<>();

        for (Map.Entry<String, JsonElement> classEntry : json.entrySet()) {
            ClassType classType = ClassType.fromName(classEntry.getKey());

            if (classType == null) {
                WynntilsMod.warn("Unknown aspect class type: " + classEntry.getKey());
                continue;
            }

            Map<String, AspectInfo> classLookupMap = new HashMap<>();
            JsonObject classAspectsObject = classEntry.getValue().getAsJsonObject();

            for (Map.Entry<String, JsonElement> aspectEntry : classAspectsObject.entrySet()) {
                JsonObject aspectJsonObject = aspectEntry.getValue().getAsJsonObject();

                // Deserialize the item
                AspectInfo aspectInfo = GSON.fromJson(aspectJsonObject, AspectInfo.class);

                // Add the item to the registry
                registry.add(aspectInfo);
                classLookupMap.put(aspectInfo.name(), aspectInfo);
            }

            lookupMap.put(classType, classLookupMap);
        }

        // Make the result visible to the world
        aspectInfoRegistry = registry;
        aspectInfoLookup = lookupMap;
    }

    private static final class AspectInfoDeserializer extends AbstractItemInfoDeserializer<AspectInfo> {
        @Override
        public AspectInfo deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            String name = json.get("name").getAsString();
            GearTier gearTier = GearTier.fromString(json.get("rarity").getAsString());
            ClassType classType = ClassType.fromName(json.get("requiredClass").getAsString());
            ItemMaterial itemMaterial = parseMaterial(json);

            List<Pair<Integer, List<StyledText>>> tiers = new ArrayList<>();

            JsonObject jsonTiers = json.getAsJsonObject("tiers");
            for (Map.Entry<String, JsonElement> entry : jsonTiers.entrySet()) {
                JsonObject tier = entry.getValue().getAsJsonObject();
                int threshold = tier.get("threshold").getAsInt();

                List<StyledText> description = new ArrayList<>();
                for (JsonElement element : tier.get("description").getAsJsonArray()) {
                    description.add(StyledText.fromJson(element.getAsJsonArray()));
                }

                tiers.add(Pair.of(threshold, description));
            }

            return new AspectInfo(name, gearTier, classType, tiers, itemMaterial);
        }
    }
}
