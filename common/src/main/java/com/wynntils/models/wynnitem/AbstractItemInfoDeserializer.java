/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.GearMajorId;
import com.wynntils.models.gear.type.GearMetaInfo;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.FixedStats;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.wynnitem.type.ItemMaterial;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import com.wynntils.models.wynnitem.type.ItemObtainType;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractItemInfoDeserializer<T> implements JsonDeserializer<T> {
    protected Pair<String, String> parseNames(JsonObject json) {
        // Wynncraft API has two fields: name and internalName. The former is a display name,
        // the latter is a static internal name that never changes.
        String displayName = json.get("name").getAsString();
        String internalName = JsonUtils.getNullableJsonString(json, "internalName");

        // If the display name is the same as the internal name,
        // we treat the internal name as null,
        // and override it, if the displayed name needs to be normalized
        if (displayName.equals(internalName)) {
            String normalizedApiName = WynnUtils.normalizeBadString(displayName);
            if (!normalizedApiName.equals(displayName)) {
                // Normalization removed a ֎ from the name. This means we want to
                // treat the name as internalName and the normalized name as display name
                displayName = normalizedApiName;
            }
        }

        return Pair.of(displayName, internalName);
    }

    protected GearType parseType(JsonObject json) {
        String typeString;
        if (json.has("accessoryType")) {
            typeString = json.get("accessoryType").getAsString();
        } else if (json.has("weaponType")) {
            typeString = json.get("weaponType").getAsString();
        } else if (json.has("armourType")) {
            typeString = json.get("armourType").getAsString();
        } else {
            typeString = json.get("type").getAsString();
        }
        return GearType.fromString(typeString);
    }

    protected GearMetaInfo parseMetaInfo(JsonObject json, String apiName, GearType type) {
        GearRestrictions restrictions = parseRestrictions(json);
        ItemMaterial material = parseMaterial(json, type);

        if (material == null || material.itemStack().isEmpty()) {
            WynntilsMod.warn("Failed to parse material for " + json.get("name").getAsString());
            material = ItemMaterial.fromItemId("minecraft:air", 0);
        }

        List<ItemObtainInfo> obtainInfo = parseObtainInfo(json);

        Optional<StyledText> loreOpt = parseLore(json);
        Optional<String> apiNameOpt = Optional.ofNullable(apiName);

        boolean allowCraftsman = JsonUtils.getNullableJsonBoolean(json, "allowCraftsman");
        boolean preIdentifiedItem = JsonUtils.getNullableJsonBoolean(json, "identified");

        return new GearMetaInfo(
                restrictions, material, obtainInfo, loreOpt, apiNameOpt, allowCraftsman, preIdentifiedItem);
    }

    protected List<ItemObtainInfo> parseObtainInfo(JsonObject json) {
        List<ItemObtainInfo> obtainInfo = new ArrayList<>();

        // Add API-obtained information
        String apiObtainName =
                JsonUtils.getNullableJsonString(JsonUtils.getNullableJsonObject(json, "dropMeta"), "name");
        List<ItemObtainType> apiObtainTypes = parseObtainTypes(json);
        for (ItemObtainType apiObtainType : apiObtainTypes) {
            switch (apiObtainType) {
                case NORMAL_MOB_DROP -> {
                    // Only add this if we do not have more specific information
                    boolean hasSpecialMob =
                            obtainInfo.stream().anyMatch(o -> o.sourceType() == ItemObtainType.SPECIAL_MOB_DROP);
                    if (!hasSpecialMob) {
                        obtainInfo.add(new ItemObtainInfo(ItemObtainType.NORMAL_MOB_DROP, Optional.empty()));
                    }
                }
                case DUNGEON_RAIN -> {
                    // Only add this if we do not have more specific information
                    boolean hasDungeon =
                            obtainInfo.stream().anyMatch(o -> o.sourceType().isDungeon());
                    if (!hasDungeon) {
                        obtainInfo.add(new ItemObtainInfo(ItemObtainType.DUNGEON_RAIN, Optional.empty()));
                    }
                }
                default -> {
                    // Add the API-obtained information
                    obtainInfo.add(new ItemObtainInfo(apiObtainType, Optional.ofNullable(apiObtainName)));
                }
            }
        }

        if (obtainInfo.isEmpty()) {
            // We have no information on how to obtain this
            obtainInfo.add(new ItemObtainInfo(ItemObtainType.UNKNOWN, Optional.empty()));
        }

        return List.copyOf(obtainInfo);
    }

    protected Optional<StyledText> parseLore(JsonObject json) {
        String lore = JsonUtils.getNullableJsonString(json, "lore");
        if (lore == null) return Optional.empty();

        return Optional.of(StyledText.fromString(lore));
    }

    protected GearRestrictions parseRestrictions(JsonObject json) {
        String restrictions = JsonUtils.getNullableJsonString(json, "restrictions");
        if (restrictions == null) return GearRestrictions.NONE;

        return GearRestrictions.fromString(restrictions);
    }

    protected ItemMaterial parseMaterial(JsonObject json, GearType type) {
        boolean parseAsArmor = type.isArmor() && json.has("armourMaterial");
        return parseAsArmor ? parseArmorType(json, type) : parseOtherMaterial(json);
    }

    protected List<ItemObtainType> parseObtainTypes(JsonObject json) {
        List<ItemObtainType> types = new ArrayList<>();

        JsonObject dropMeta = JsonUtils.getNullableJsonObject(json, "dropMeta");
        if (dropMeta.has("type")) {
            JsonElement type = dropMeta.get("type");

            // The type can be either a string or an array of string
            try {
                if (type.isJsonArray()) {
                    for (JsonElement typeElement : type.getAsJsonArray()) {
                        types.add(ItemObtainType.fromApiName(typeElement.getAsString()));
                    }
                } else {
                    types.add(ItemObtainType.fromApiName(type.getAsString()));
                }
            } catch (Exception e) {
                WynntilsMod.warn(
                        "Failed to parse obtain types for " + json.get("name").getAsString());
                WynntilsMod.warn("Obtain types: " + type.toString());
                return List.of();
            }
        }

        // Return an immutable list
        return List.copyOf(types);
    }

    protected ItemMaterial parseArmorType(JsonObject json, GearType gearType) {
        String armourMaterial =
                JsonUtils.getNullableJsonString(json, "armourMaterial").toLowerCase(Locale.ROOT);

        // FIXME: As of writing, v3.3 API forgot to add armor colors to leather armor
        CustomColor color = null;
        if (armourMaterial.equals("leather")) {
            String colorStr = JsonUtils.getNullableJsonString(json, "armorColor");
            // Oddly enough a lot of items has a "dummy" color value of "160,101,64"; ignore them
            if (colorStr != null && !colorStr.equals("160,101,64")) {
                String[] colorArray = colorStr.split("[, ]");
                if (colorArray.length == 3) {
                    int r = Integer.parseInt(colorArray[0]);
                    int g = Integer.parseInt(colorArray[1]);
                    int b = Integer.parseInt(colorArray[2]);
                    color = new CustomColor(r, g, b);
                }
            }
        }

        return ItemMaterial.fromArmorType(armourMaterial, gearType, color);
    }

    protected ItemMaterial parseOtherMaterial(JsonObject json) {
        if (!json.has("icon")) {
            WynntilsMod.warn(
                    "Item DB does not contain an icon for " + json.get("name").getAsString());
            return ItemMaterial.fromItemId("minecraft:air", 0);
        }

        JsonObject icon = json.getAsJsonObject("icon");

        String iconFormat = icon.get("format").getAsString();

        switch (iconFormat) {
            case "attribute" -> {
                JsonObject value = icon.get("value").getAsJsonObject();
                JsonElement customModelData = value.get("customModelData");

                // The API is inconsistent, and sometimes returns a string instead of an int
                int customModelDataInt = customModelData.isJsonPrimitive()
                        ? customModelData.getAsInt()
                        : Integer.parseInt(customModelData.getAsString());

                return ItemMaterial.fromItemId(value.get("id").getAsString(), customModelDataInt);
            }
            case "skin" -> {
                return ItemMaterial.fromPlayerHeadUUID(icon.get("value").getAsString());
            }
            case "legacy" -> {
                String material = icon.get("value").getAsString();
                String[] materialArray = material.split(":");
                int itemTypeCode = Integer.parseInt(materialArray[0]);
                int damageCode = materialArray.length > 1 ? Integer.parseInt(materialArray[1]) : 0;
                return ItemMaterial.fromItemTypeCode(itemTypeCode, damageCode);
            }
        }

        return null;
    }

    protected GearRequirements parseRequirements(JsonObject json, GearType type) {
        JsonObject requirements = JsonUtils.getNullableJsonObject(json, "requirements");
        if (requirements == null) {
            // No requirements
            return new GearRequirements(0, Optional.empty(), List.of(), Optional.empty());
        }

        int level = requirements.get("level").getAsInt();
        Optional<ClassType> classType = parseClassType(requirements, type);
        List<Pair<Skill, Integer>> skills = parseSkills(requirements);
        Optional<String> quest = parseQuest(requirements);

        return new GearRequirements(level, classType, skills, quest);
    }

    protected Optional<ClassType> parseClassType(JsonObject json, GearType type) {
        if (type.isWeapon()) {
            return Optional.of(type.getClassReq());
        }

        String classReq = JsonUtils.getNullableJsonString(json, "classRequirement");
        if (classReq == null) return Optional.empty();

        return Optional.of(ClassType.fromName(classReq));
    }

    protected List<Pair<Skill, Integer>> parseSkills(JsonObject json) {
        List<Pair<Skill, Integer>> list = new ArrayList<>();
        for (Skill skill : Skill.values()) {
            int minPoints = JsonUtils.getNullableJsonInt(json, skill.getApiName());
            if (minPoints == 0) continue;

            list.add(Pair.of(skill, minPoints));
        }

        // Return an immutable list
        return List.copyOf(list);
    }

    protected Optional<String> parseQuest(JsonObject json) {
        String questName = JsonUtils.getNullableJsonString(json, "quest");
        if (questName == null) return Optional.empty();

        return Optional.of(WynnUtils.normalizeBadString(questName));
    }

    protected FixedStats parseFixedStats(JsonObject json) {
        JsonObject baseStats = JsonUtils.getNullableJsonObject(json, "base");
        JsonObject identifications = JsonUtils.getNullableJsonObject(json, "identifications");

        int healthBuff = JsonUtils.getNullableJsonInt(baseStats, "baseHealth");
        String attackSpeedStr = JsonUtils.getNullableJsonString(json, "attackSpeed");
        Optional<GearAttackSpeed> attackSpeed = Optional.ofNullable(GearAttackSpeed.fromString(attackSpeedStr));

        Optional<GearMajorId> majorIds = parseMajorIds(json);
        List<Pair<DamageType, RangedValue>> damages = parseDamages(baseStats);
        List<Pair<Element, Integer>> defences = parseDefences(baseStats);

        return new FixedStats(healthBuff, attackSpeed, majorIds, damages, defences);
    }

    protected Optional<GearMajorId> parseMajorIds(JsonObject json) {
        JsonObject majorIdsJson = JsonUtils.getNullableJsonObject(json, "majorIds");
        if (majorIdsJson == null || majorIdsJson.isJsonNull() || majorIdsJson.isEmpty()) return Optional.empty();

        Map<String, JsonElement> majorIdMap = majorIdsJson.asMap();

        if (majorIdMap.size() > 1) {
            WynntilsMod.warn("Item DB contains multiple major IDs for "
                    + json.get("name").getAsString());
        }

        if (majorIdMap.isEmpty()) return Optional.empty();

        Map.Entry<String, JsonElement> majorIdElement =
                majorIdMap.entrySet().iterator().next();

        // Wynncraft API now ships HTML tags in the description (as they have a custom markup language internally)
        StyledText description =
                StyledText.fromString(majorIdElement.getValue().getAsString().replaceAll("<[^>]*>", ""));

        return Optional.of(new GearMajorId(majorIdElement.getKey(), description));
    }

    protected List<Pair<DamageType, RangedValue>> parseDamages(JsonObject json) {
        List<Pair<DamageType, RangedValue>> list = new ArrayList<>();

        // First look for neutral damage, which has a non-standard name
        addDamageStat(list, DamageType.NEUTRAL, json.get("baseDamage"));

        // Then check for elemental damage
        for (Element element : Models.Element.getGearElementOrder()) {
            String damageName = "base" + EnumUtils.toNiceString(element) + "Damage";
            addDamageStat(list, DamageType.fromElement(element), json.get(damageName));
        }

        // Return an immutable list
        return List.copyOf(list);
    }

    protected void addDamageStat(
            List<Pair<DamageType, RangedValue>> list, DamageType damageType, JsonElement damageJson) {
        if (damageJson == null) return;

        JsonObject rangeObject = damageJson.getAsJsonObject();
        RangedValue range = RangedValue.of(
                rangeObject.get("min").getAsInt(), rangeObject.get("max").getAsInt());
        if (range.equals(RangedValue.NONE)) return;

        list.add(Pair.of(damageType, range));
    }

    protected List<Pair<Element, Integer>> parseDefences(JsonObject json) {
        List<Pair<Element, Integer>> list = new ArrayList<>();
        for (Element element : Models.Element.getGearElementOrder()) {
            String defenceApiName = "base" + element.getDisplayName() + "Defence";

            int minPoints = JsonUtils.getNullableJsonInt(json, defenceApiName);
            if (minPoints == 0) continue;

            list.add(Pair.of(element, minPoints));
        }

        // Return an immutable list
        return List.copyOf(list);
    }

    protected List<Pair<StatType, StatPossibleValues>> parseVariableStats(
            JsonObject json, String identificationsObjectKey) {
        List<Pair<StatType, StatPossibleValues>> list = new ArrayList<>();

        if (!json.has(identificationsObjectKey)) {
            // No identifications, so no variable stats
            return List.of();
        }

        JsonObject identificationsJson = json.get(identificationsObjectKey).getAsJsonObject();
        boolean preIdentifiedItem = JsonUtils.getNullableJsonBoolean(json, "identified");

        for (Map.Entry<String, JsonElement> entry : identificationsJson.entrySet()) {
            String statApiName = entry.getKey();

            if (statApiName.equals("elementalDefense")) {
                // The API is inconsistent, and rarely usese "elementalDefense" instead of "elementalDefence"
                statApiName = "elementalDefence";
            }

            StatType statType = Models.Stat.fromApiName(statApiName);

            if (statType == null) {
                WynntilsMod.warn("Item DB contains invalid stat type " + statApiName);
                continue;
            }

            int baseValue;
            boolean preIdentified;

            // The new API has a range for each stat,
            // we still like to manually calculate,
            // but it is great for verification
            RangedValue apiRange;

            // This is a pre-identified id, so there is no range
            if (preIdentifiedItem || identificationsJson.get(entry.getKey()).isJsonPrimitive()) {
                baseValue = JsonUtils.getNullableJsonInt(identificationsJson, entry.getKey());

                // We have a pre-identified item, so there is no range
                preIdentified = true;

                apiRange = RangedValue.of(baseValue, baseValue);
            } else {
                JsonObject statObject = entry.getValue().getAsJsonObject();

                baseValue = JsonUtils.getNullableJsonInt(statObject, "raw");
                preIdentified = false;

                if (statObject.has("min") && statObject.has("max")) {
                    apiRange = RangedValue.of(
                            statObject.get("min").getAsInt(),
                            statObject.get("max").getAsInt());
                } else {
                    // Sometimes, the api does not return min or max values, so we can't do verification
                    apiRange = RangedValue.NONE;
                }
            }

            // If the base value is 0, this stat is not present on the item
            if (baseValue == 0) continue;

            // "Inverted" stats (i.e. spell costs) are calculated
            // as inverted, but are later changed back
            if (statType.calculateAsInverted()) {
                baseValue = -baseValue;

                // If the stat is inverted, the API range is also inverted,
                // so the check below does not fail
                apiRange = RangedValue.of(-apiRange.low(), -apiRange.high());
            }

            // Range will always be stored such as "low" means "worst possible value" and
            // "high" means "best possible value".
            RangedValue range = StatCalculator.calculatePossibleValuesRange(baseValue, preIdentified, statType);

            // Verify that the calculated range matches the API's range
            if (apiRange.equals(RangedValue.NONE)) {
                WynntilsMod.warn(json.get("name").getAsString() + "'s stat " + statType.getApiName()
                        + " has it's min-max range missing in the API");
            } else if (!apiRange.equals(range)) {
                WynntilsMod.warn(json.get("name").getAsString() + "'s stat " + statType.getApiName()
                        + " has a range mismatch: API " + apiRange + " vs calculated " + range);
            }

            StatPossibleValues possibleValues = new StatPossibleValues(statType, range, baseValue, preIdentified);
            list.add(Pair.of(statType, possibleValues));
        }

        // Return an immutable list
        return List.copyOf(list);
    }
}
