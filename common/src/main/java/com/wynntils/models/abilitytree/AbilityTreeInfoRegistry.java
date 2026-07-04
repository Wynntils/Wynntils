/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.abilitytree.type.AbilityTreeInfo;
import com.wynntils.models.abilitytree.type.AbilityTreeLocation;
import com.wynntils.models.abilitytree.type.AbilityTreeNodeType;
import com.wynntils.models.abilitytree.type.AbilityTreeSkillNode;
import com.wynntils.models.abilitytree.type.ArchetypeRequirement;
import com.wynntils.models.character.type.ClassType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbilityTreeInfoRegistry {
    private Map<ClassType, AbilityTreeInfo> abilityTreeMap = Map.of();
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(AbilityTreeInfo.class, new AbilityTreeInfoDeserializer())
            .create();

    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_ABILITIES).handleJsonObject(this::handleAbilities);
    }

    private void handleAbilities(JsonObject json) {
        Map<ClassType, AbilityTreeInfo> tempMap = new HashMap<>();

        for (Map.Entry<String, JsonElement> classEntry : json.entrySet()) {
            ClassType classType = ClassType.fromName(classEntry.getKey());

            if (classType == null) {
                WynntilsMod.warn("Unknown ability tree class type: " + classEntry.getKey());
                continue;
            }

            JsonObject classJsonObject = classEntry.getValue().getAsJsonObject();
            AbilityTreeInfo abilityTreeInfo = GSON.fromJson(classJsonObject, AbilityTreeInfo.class);

            tempMap.put(classType, abilityTreeInfo);
        }

        abilityTreeMap = tempMap;
    }

    public AbilityTreeInfo getAbilityTree(ClassType classType) {
        return abilityTreeMap.get(classType);
    }

    public AbilityTreeSkillNode getNodeFromNameAndClass(String name, ClassType classType) {
        AbilityTreeInfo treeInfo = getAbilityTree(classType);
        if (treeInfo == null || treeInfo.nodes() == null) return null;

        for (AbilityTreeSkillNode node : treeInfo.nodes()) {
            if (node.name().equals(name)) {
                return node;
            }
        }
        return null;
    }

    private static final class AbilityTreeInfoDeserializer implements JsonDeserializer<AbilityTreeInfo> {
        @Override
        public AbilityTreeInfo deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            List<AbilityTreeSkillNode> nodes = new ArrayList<>();
            for (JsonElement nodeElement : json.getAsJsonArray("nodes")) {
                nodes.add(new AbilityTreeSkillNodeDeserializer()
                        .deserialize(nodeElement, AbilityTreeSkillNode.class, context));
            }

            return new AbilityTreeInfo(nodes);
        }
    }

    private static final class AbilityTreeSkillNodeDeserializer implements JsonDeserializer<AbilityTreeSkillNode> {
        @Override
        public AbilityTreeSkillNode deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            int id = json.get("id").getAsInt();
            String name = json.get("name").getAsString();
            String formattedName = json.get("formattedName").getAsString();

            String nodeTypeKey =
                    "abilityTree." + json.get("abilityTreeNodeType").getAsString();
            AbilityTreeNodeType abilityTreeNodeType = null;
            for (AbilityTreeNodeType t : AbilityTreeNodeType.values()) {
                if (t.getKey().equals(nodeTypeKey)) {
                    abilityTreeNodeType = t;
                    break;
                }
            }

            List<String> description = new ArrayList<>();
            for (JsonElement element : json.getAsJsonArray("description")) {
                description.add(element.getAsString());
            }

            int cost = json.get("cost").getAsInt();

            List<String> willBlock = new ArrayList<>();
            for (JsonElement element : json.getAsJsonArray("willBlock")) {
                willBlock.add(element.getAsString());
            }

            List<String> blockedBy = new ArrayList<>();
            for (JsonElement element : json.getAsJsonArray("blockedBy")) {
                blockedBy.add(element.getAsString());
            }

            String requiredAbility =
                    json.has("requiredAbility") && !json.get("requiredAbility").isJsonNull()
                            ? json.get("requiredAbility").getAsString()
                            : null;

            ArchetypeRequirement requiredArchetype = null;
            if (json.has("requiredArchetype") && !json.get("requiredArchetype").isJsonNull()) {
                JsonObject archetypeJson = json.getAsJsonObject("requiredArchetype");
                requiredArchetype = new ArchetypeRequirement(
                        archetypeJson.get("name").getAsString(),
                        archetypeJson.get("required").getAsInt());
            }

            int requiredLevel = json.get("requiredLevel").getAsInt();

            String archetype = json.has("archetype") && !json.get("archetype").isJsonNull()
                    ? json.get("archetype").getAsString()
                    : null;

            JsonObject locationJson = json.getAsJsonObject("location");
            AbilityTreeLocation location = new AbilityTreeLocation(
                    locationJson.get("page").getAsInt(),
                    locationJson.get("row").getAsInt(),
                    locationJson.get("col").getAsInt());

            List<Integer> connections = new ArrayList<>();
            for (JsonElement element : json.getAsJsonArray("connections")) {
                connections.add(element.getAsInt());
            }

            return new AbilityTreeSkillNode(
                    id,
                    name,
                    formattedName,
                    abilityTreeNodeType,
                    description,
                    cost,
                    willBlock,
                    blockedBy,
                    requiredAbility,
                    requiredArchetype,
                    requiredLevel,
                    archetype,
                    location,
                    connections);
        }
    }
}
