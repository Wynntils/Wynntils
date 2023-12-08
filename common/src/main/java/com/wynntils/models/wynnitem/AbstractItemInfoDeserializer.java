/*
 * Copyright © Wynntils 2023.
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
import com.wynntils.models.gear.type.GearDropRestrictions;
import com.wynntils.models.gear.type.GearMajorId;
import com.wynntils.models.gear.type.GearMetaInfo;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.FixedStats;
import com.wynntils.models.stats.type.SkillStatType;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.wynnitem.type.ItemMaterial;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import com.wynntils.models.wynnitem.type.ItemObtainType;
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
        } else {
            typeString = json.get("type").getAsString();
        }
        return GearType.fromString(typeString);
    }

    protected GearMetaInfo parseMetaInfo(JsonObject json, String name, String apiName, GearType type) {
        GearDropRestrictions dropRestrictions = parseDropRestrictions(json);
        GearRestrictions restrictions = parseRestrictions(json);
        ItemMaterial material = parseMaterial(json, type);

        List<ItemObtainInfo> obtainInfo = parseObtainInfo(json, name);

        Optional<StyledText> loreOpt = parseLore(json);
        Optional<String> apiNameOpt = Optional.ofNullable(apiName);

        boolean allowCraftsman = JsonUtils.getNullableJsonBoolean(json, "allowCraftsman");
        boolean preIdentifiedItem = JsonUtils.getNullableJsonBoolean(json, "identified");

        return new GearMetaInfo(
                dropRestrictions,
                restrictions,
                material,
                obtainInfo,
                loreOpt,
                apiNameOpt,
                allowCraftsman,
                preIdentifiedItem);
    }

    protected List<ItemObtainInfo> parseObtainInfo(JsonObject json, String name) {
        List<ItemObtainInfo> obtainInfo = new ArrayList<>();

        // Add crowd-sourced information
        List<ItemObtainInfo> obtainCrowdSourced = Models.WynnItem.getObtainInfo(name);
        obtainInfo.addAll(obtainCrowdSourced);

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

    protected GearDropRestrictions parseDropRestrictions(JsonObject json) {
        String restrictions = JsonUtils.getNullableJsonString(json, "dropRestriction");
        if (restrictions == null) return GearDropRestrictions.NORMAL;

        return GearDropRestrictions.fromString(restrictions);
    }

    protected GearRestrictions parseRestrictions(JsonObject json) {
        String restrictions = JsonUtils.getNullableJsonString(json, "restrictions");
        if (restrictions == null) return GearRestrictions.NONE;

        return GearRestrictions.fromString(restrictions);
    }

    protected ItemMaterial parseMaterial(JsonObject json, GearType type) {
        return type.isArmor() ? parseArmorType(json, type) : parseOtherMaterial(json, type);
    }

    protected List<ItemObtainType> parseObtainTypes(JsonObject json) {
        List<ItemObtainType> types = new ArrayList<>();

        JsonObject dropMeta = JsonUtils.getNullableJsonObject(json, "dropMeta");
        if (dropMeta.has("type")) {
            JsonElement type = dropMeta.get("type");

            // The type can be either a string or an array of strings
            if (type.isJsonArray()) {
                for (JsonElement typeElement : type.getAsJsonArray()) {
                    types.add(ItemObtainType.fromApiName(typeElement.getAsString()));
                }
            } else {
                types.add(ItemObtainType.fromApiName(type.getAsString()));
            }
        }

        // Return an immutable list
        return List.copyOf(types);
    }

    protected ItemMaterial parseArmorType(JsonObject json, GearType gearType) {
        // We might have a specified material (like a carved pumpkin or mob head),
        // if so this takes precedence
        String material = JsonUtils.getNullableJsonString(json, "material");
        if (material != null) {
            return parseOtherMaterial(json, gearType);
        }

        // Some helmets are player heads
        String skin = JsonUtils.getNullableJsonString(json, "skin");
        if (skin != null) {
            return ItemMaterial.fromPlayerHeadUUID(skin);
        }

        String materialType = JsonUtils.getNullableJsonString(json, "armorType").toLowerCase(Locale.ROOT);

        CustomColor color = null;
        if (materialType.equals("leather")) {
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

        return ItemMaterial.fromArmorType(materialType, gearType, color);
    }

    protected ItemMaterial parseOtherMaterial(JsonObject json, GearType gearType) {
        String material = JsonUtils.getNullableJsonString(json, "material");
        if (material == null) {
            // We're screwed. The best we can do is to give a generic default representation
            // for this gear type
            return ItemMaterial.fromGearType(gearType);
        }

        String[] materialArray = material.split(":");
        int itemTypeCode = Integer.parseInt(materialArray[0]);
        int damageCode = materialArray.length > 1 ? Integer.parseInt(materialArray[1]) : 0;
        return ItemMaterial.fromItemTypeCode(itemTypeCode, damageCode);
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

        int healthBuff = JsonUtils.getNullableJsonInt(baseStats, "health");
        List<Pair<Skill, Integer>> skillBonuses = parseSkillBonuses(identifications);
        String attackSpeedStr = JsonUtils.getNullableJsonString(json, "attackSpeed");
        Optional<GearAttackSpeed> attackSpeed = Optional.ofNullable(GearAttackSpeed.fromString(attackSpeedStr));

        Optional<GearMajorId> majorIds = parseMajorIds(json);
        List<Pair<DamageType, RangedValue>> damages = parseDamages(baseStats);
        List<Pair<Element, Integer>> defences = parseDefences(baseStats);

        return new FixedStats(healthBuff, skillBonuses, attackSpeed, majorIds, damages, defences);
    }

    protected Optional<GearMajorId> parseMajorIds(JsonObject json) {
        JsonObject majorIdsJson = JsonUtils.getNullableJsonObject(json, "majorIds");
        if (majorIdsJson == null || majorIdsJson.isJsonNull() || majorIdsJson.isEmpty()) return Optional.empty();

        return Optional.of(new GearMajorId(
                majorIdsJson.get("name").getAsString(),
                StyledText.fromString(
                        majorIdsJson.get("description").getAsString().replaceAll("&", "§"))));
    }

    protected List<Pair<Skill, Integer>> parseSkillBonuses(JsonObject json) {
        if (json.isEmpty()) return List.of();

        List<Pair<Skill, Integer>> list = new ArrayList<>();
        for (Skill skill : Skill.values()) {
            String skillBonusApiName = "raw" + com.wynntils.utils.StringUtils.capitalizeFirst(skill.getApiName());
            int minPoints = JsonUtils.getNullableJsonInt(json, skillBonusApiName);
            if (minPoints == 0) continue;

            list.add(Pair.of(skill, minPoints));
        }

        // Return an immutable list
        return List.copyOf(list);
    }

    protected List<Pair<DamageType, RangedValue>> parseDamages(JsonObject json) {
        List<Pair<DamageType, RangedValue>> list = new ArrayList<>();

        // First look for neutral damage, which has a non-standard name
        addDamageStat(list, DamageType.NEUTRAL, json.get("damage"));

        // Then check for elemental damage
        for (Element element : Models.Element.getGearElementOrder()) {
            String damageName = element.name().toLowerCase(Locale.ROOT) + "Damage";
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
            String defenceApiName = element.name().toLowerCase(Locale.ROOT) + "Defence";

            int minPoints = JsonUtils.getNullableJsonInt(json, defenceApiName);
            if (minPoints == 0) continue;

            list.add(Pair.of(element, minPoints));
        }

        // Return an immutable list
        return List.copyOf(list);
    }

    protected List<Pair<StatType, StatPossibleValues>> parseVariableStats(JsonObject json) {
        List<Pair<StatType, StatPossibleValues>> list = new ArrayList<>();

        if (!json.has("identifications")) {
            // No identifications, so no variable stats
            return List.of();
        }

        JsonObject identificationsJson = json.get("identifications").getAsJsonObject();
        boolean preIdentifiedItem = JsonUtils.getNullableJsonBoolean(json, "identified");

        for (Map.Entry<String, JsonElement> entry : identificationsJson.entrySet()) {
            StatType statType = Models.Stat.fromApiRollId(entry.getKey());

            if (statType == null) {
                WynntilsMod.warn("Item DB contains invalid stat type " + entry.getKey());
                continue;
            }

            if (statType instanceof SkillStatType) {
                // Skill stats are not variable for gear
                continue;
            }

            int baseValue;
            boolean preIdentified;

            // This is a pre-identified id, so there is no range
            if (preIdentifiedItem
                    || identificationsJson.get(statType.getApiName()).isJsonPrimitive()) {
                baseValue = JsonUtils.getNullableJsonInt(identificationsJson, statType.getApiName());

                // We have a pre-identified item, so there is no range
                preIdentified = true;
            } else {
                JsonObject statObject = entry.getValue().getAsJsonObject();

                baseValue = JsonUtils.getNullableJsonInt(statObject, "raw");
                preIdentified = false;
            }

            // If the base value is 0, this stat is not present on the item
            if (baseValue == 0) continue;

            // "Inverted" stats (i.e. spell costs) will be stored as a positive value,
            // and only converted to negative at display time.
            if (statType.showAsInverted()) {
                baseValue = -baseValue;
            }
            // Range will always be stored such as "low" means "worst possible value" and
            // "high" means "best possible value".
            RangedValue range = StatCalculator.calculatePossibleValuesRange(
                    baseValue, preIdentifiedItem, statType.showAsInverted());
            StatPossibleValues possibleValues = new StatPossibleValues(statType, range, baseValue, preIdentified);
            list.add(Pair.of(statType, possibleValues));
        }

        // Return an immutable list
        return List.copyOf(list);
    }
}
