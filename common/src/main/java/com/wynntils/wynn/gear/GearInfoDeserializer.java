/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.core.components.Managers;
import com.wynntils.utils.Pair;
import com.wynntils.utils.RangedValue;
import com.wynntils.wynn.gear.types.GearMaterial;
import com.wynntils.wynn.gear.types.GearRestrictions;
import com.wynntils.wynn.gear.types.GearStat;
import com.wynntils.wynn.objects.ClassType;
import com.wynntils.wynn.objects.Skill;
import com.wynntils.wynn.objects.profiles.item.GearDropType;
import com.wynntils.wynn.objects.profiles.item.GearTier;
import com.wynntils.wynn.objects.profiles.item.GearType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class GearInfoDeserializer implements JsonDeserializer<GearInfo> {
    @Override
    public GearInfo deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        // Some names apparently has a random ֎ in them...
        JsonElement primaryName = json.get("name");
        JsonElement secondaryName = json.get("displayName");
        String name = (secondaryName == null ? primaryName : secondaryName)
                .getAsString()
                .replace("֎", "");
        GearType type = parseType(json);
        GearTier tier = GearTier.fromString(json.get("tier").getAsString());
        int powderSlots = json.get("sockets").getAsInt();

        String altName = (secondaryName == null ? null : primaryName.getAsString());
        GearMetaInfo metaInfo = parseMetaInfo(json, altName);
        GearRequirements requirements = parseRequirements(json, type);
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

    private GearMetaInfo parseMetaInfo(JsonObject json, String altName) {
        GearRestrictions restrictions = parseRestrictions(json);
        GearMaterial material = parseMaterial(json);
        GearDropType dropType = GearDropType.fromString(json.get("dropType").getAsString());

        JsonElement loreJson = json.get("lore");
        Optional<String> loreOpt = loreJson == null ? Optional.empty() : Optional.of(loreJson.getAsString());
        Optional<String> altNameOpt = Optional.ofNullable(altName);

        JsonElement allowCraftsmanJson = json.get("allowCraftsman");
        boolean allowCraftsman = allowCraftsmanJson != null && allowCraftsmanJson.getAsBoolean();

        return new GearMetaInfo(restrictions, material, dropType, loreOpt, altNameOpt, allowCraftsman);
    }

    private GearRestrictions parseRestrictions(JsonObject json) {
        JsonElement restrictionsJson = json.get("restrictions");
        if (restrictionsJson == null) return GearRestrictions.NONE;
        if (restrictionsJson.isJsonNull()) return GearRestrictions.NONE;

        return GearRestrictions.fromString(restrictionsJson.getAsString());
    }

    private GearMaterial parseMaterial(JsonObject json) {
        // FIXME: Needs to be done correctly
        return new GearMaterial();
    }

    private GearRequirements parseRequirements(JsonObject json, GearType type) {
        int level = json.get("level").getAsInt();
        Optional<ClassType> classType = parseClassType(json, type);
        List<Pair<Skill, Integer>> skills = parseSkills(json);
        Optional<String> quest = parseQuest(json);

        return new GearRequirements(level, classType, skills, quest);
    }

    private List<Pair<Skill, Integer>> parseSkills(JsonObject json) {
        List<Pair<Skill, Integer>> list = new ArrayList<>();
        for (Skill skill : Skill.values()) {
            String skillJsonName = skill.getApiName();
            JsonElement skillJson = json.get(skillJsonName);
            if (skillJson == null) continue;

            int minPoints = skillJson.getAsInt();
            if (minPoints == 0) continue;

            list.add(Pair.of(skill, minPoints));
        }

        // Return an immutable list
        return List.copyOf(list);
    }

    private Optional<String> parseQuest(JsonObject json) {
        JsonElement questJson = json.get("quest");
        if (questJson == null) return Optional.empty();
        if (questJson.isJsonNull()) return Optional.empty();

        // Apparently some quests got an extra "֎" added to the name
        Optional<String> quest = Optional.of(questJson.getAsString().replace("֎", ""));
        return quest;
    }

    private Optional<ClassType> parseClassType(JsonObject json, GearType type) {
        if (type.isWeapon()) {
            return Optional.of(type.getClassReq());
        }

        JsonElement classReq = json.get("classRequirement");
        if (classReq == null) return Optional.empty();
        if (classReq.isJsonNull()) return Optional.empty();

        return Optional.of(ClassType.fromName(classReq.getAsString()));
    }

    private GearStatsFixed parseStatsFixed(JsonObject json) {
        return null;
    }

    private List<Pair<GearStat, RangedValue>> parseStatsIdentified(JsonObject json) {
        List<Pair<GearStat, RangedValue>> list = new ArrayList<>();
        JsonElement identifiedJson = json.get("identified");
        boolean preIdentified = identifiedJson != null && identifiedJson.getAsBoolean();

        for (GearStat stat : Managers.GearInfo.registry) {
            JsonElement statJson = json.get(stat.apiName());
            if (statJson == null) continue;

            int baseValue = statJson.getAsInt();
            if (baseValue == 0) continue;

            RangedValue range = calculateRange(baseValue, preIdentified);
            list.add(Pair.of(stat, range));
        }

        // Return an immutable list
        return List.copyOf(list);
    }

    private RangedValue calculateRange(int baseValue, boolean preIdentified) {
        if (preIdentified) {
            // This is actually a single, fixed value
            return new RangedValue(baseValue, baseValue);
        } else {
            // FIXME: Do proper calculations
            return new RangedValue(0, baseValue);
        }
    }
}
