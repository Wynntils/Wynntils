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
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.Pair;
import com.wynntils.utils.RangedValue;
import com.wynntils.wynn.gear.types.GearDamageType;
import com.wynntils.wynn.gear.types.GearMajorId;
import com.wynntils.wynn.gear.types.GearMaterial;
import com.wynntils.wynn.gear.types.GearRestrictions;
import com.wynntils.wynn.gear.types.GearStat;
import com.wynntils.wynn.objects.ClassType;
import com.wynntils.wynn.objects.Element;
import com.wynntils.wynn.objects.Skill;
import com.wynntils.wynn.objects.profiles.item.GearAttackSpeed;
import com.wynntils.wynn.objects.profiles.item.GearDropType;
import com.wynntils.wynn.objects.profiles.item.GearTier;
import com.wynntils.wynn.objects.profiles.item.GearType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

class GearInfoDeserializer implements JsonDeserializer<GearInfo> {
    // For reference, uncommonly used item types:
    // stick: Breaker Bar, Cracked Oak Wand, Sharpened Stylus, Valix
    // bone: Wybel Paw
    // pumpkin: Pumpkin Helmet
    private static final Map<Integer, String> KNOWN_USED_ITEM_CODES = Map.of(
            256,
            "iron_shovel",
            259,
            "flint_and_steel",
            261,
            "bow",
            269,
            "wooden_shovel",
            273,
            "stone_shovel",
            359,
            "shears",
            280,
            "stick",
            352,
            "bone",
            86,
            "pumpkin");

    @Override
    public GearInfo deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        JsonElement primaryName = json.get("name");
        JsonElement secondaryName = json.get("displayName");
        // Some names apparently has a random ֎ in them...
        String name = (secondaryName == null ? primaryName : secondaryName)
                .getAsString()
                .replace("֎", "");
        GearType type = parseType(json);
        GearTier tier = GearTier.fromString(json.get("tier").getAsString());
        int powderSlots = json.get("sockets").getAsInt();

        String altName = (secondaryName == null ? null : primaryName.getAsString());
        GearMetaInfo metaInfo = parseMetaInfo(json, altName, type);
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

    private GearMetaInfo parseMetaInfo(JsonObject json, String altName, GearType type) {
        GearRestrictions restrictions = parseRestrictions(json);
        GearMaterial material = parseMaterial(json, type);
        GearDropType dropType = GearDropType.fromString(json.get("dropType").getAsString());

        Optional<String> loreOpt = parseLore(json);
        Optional<String> altNameOpt = Optional.ofNullable(altName);

        JsonElement allowCraftsmanJson = json.get("allowCraftsman");
        boolean allowCraftsman = allowCraftsmanJson != null && allowCraftsmanJson.getAsBoolean();

        return new GearMetaInfo(restrictions, material, dropType, loreOpt, altNameOpt, allowCraftsman);
    }

    private Optional<String> parseLore(JsonObject json) {
        String lore = JsonUtils.getNullableJsonString(json, "addedLore");
        if (lore == null) return Optional.empty();

        // Some lore contain like "\\[Community Event Winner\\]", fix that
        return Optional.of(StringUtils.replaceEach(lore, new String[] {"\\[", "\\]"}, new String[] {"[", "]"}));
    }

    private GearRestrictions parseRestrictions(JsonObject json) {
        String restrictions = JsonUtils.getNullableJsonString(json, "restrictions");
        if (restrictions == null) return GearRestrictions.NONE;

        return GearRestrictions.fromString(restrictions);
    }

    private GearMaterial parseMaterial(JsonObject json, GearType type) {
        return type.isArmour() ? parseArmorType(json, type) : parseOtherMaterial(json, type);
    }

    private GearMaterial parseArmorType(JsonObject json, GearType gearType) {
        // We might have a specified material (like a carved pumpkin or mob head),
        // if so this takes precedence
        String material = JsonUtils.getNullableJsonString(json, "material");
        if (material != null) {
            return parseOtherMaterial(json, gearType);
        }

        String armorType = JsonUtils.getNullableJsonString(json, "armorType").toUpperCase(Locale.ROOT);

        CustomColor color = null;
        if (armorType.equals("LEATHER")) {
            String colorStr = JsonUtils.getNullableJsonString(json, "armorColor");
            // Oddly enough a lot of items has a "dummy" color value of "160,101,64"; ignore them
            if (colorStr != null && !colorStr.equals("160,101,64")) {
                String[] colorArray = colorStr.split("[, ]");
                if (colorArray.length == 3) {
                    int r = Integer.parseInt(colorArray[0]);
                    int g = Integer.parseInt(colorArray[0]);
                    int b = Integer.parseInt(colorArray[0]);
                    color = new CustomColor(r, g, b);
                }
            }
        }

        return new GearMaterial(armorType, gearType, color);
    }

    private GearMaterial parseOtherMaterial(JsonObject json, GearType gearType) {
        String material = JsonUtils.getNullableJsonString(json, "material");
        if (material == null) {
            // We're screwed. The best we can do is to give a generic default representation
            // for this gear type
            return new GearMaterial(gearType);
        }

        String[] materialArray = material.split(":");
        int itemTypeCode = Integer.parseInt(materialArray[0]);
        int damageCode = materialArray.length > 1 ? Integer.parseInt(materialArray[1]) : 0;
        return getItemFromCodeAndDamage(itemTypeCode, damageCode);
    }

    private GearMaterial getItemFromCodeAndDamage(int itemTypeCode, int damageCode) {
        String itemId;
        if (itemTypeCode == 392 && damageCode == 2) {
            // Special case for Mama Zomble's memory
            itemId = "zombie_head";
        } else {
            // This is not ideal, but in practice just a subset of all mincraft items are used
            itemId = KNOWN_USED_ITEM_CODES.get(itemTypeCode);
            if (itemId == null) {
                WynntilsMod.warn("Could not convert item id: " + itemTypeCode + " from gear database");
                itemId = "bedrock"; // whatever...
            }
        }
        return new GearMaterial(itemId, damageCode);
    }

    private GearRequirements parseRequirements(JsonObject json, GearType type) {
        int level = json.get("level").getAsInt();
        Optional<ClassType> classType = parseClassType(json, type);
        List<Pair<Skill, Integer>> skills = parseSkills(json);
        Optional<String> quest = parseQuest(json);

        return new GearRequirements(level, classType, skills, quest);
    }

    private Optional<ClassType> parseClassType(JsonObject json, GearType type) {
        if (type.isWeapon()) {
            return Optional.of(type.getClassReq());
        }

        String classReq = JsonUtils.getNullableJsonString(json, "classRequirement");
        if (classReq == null) return Optional.empty();

        return Optional.of(ClassType.fromName(classReq));
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
        String questName = JsonUtils.getNullableJsonString(json, "quest");
        if (questName == null) return Optional.empty();

        // Apparently some quests got an extra "֎" added to the name
        return Optional.of(questName.replace("֎", ""));
    }

    private GearStatsFixed parseStatsFixed(JsonObject json) {
        JsonElement healthJson = json.get("health");
        int healthBuff = healthJson == null ? 0 : healthJson.getAsInt();
        List<Pair<Skill, Integer>> skillBuffs = parseSkillBuffs(json);
        JsonElement attackSpeedJson = json.get("attackSpeed");
        Optional<GearAttackSpeed> attackSpeed = (attackSpeedJson == null)
                ? Optional.empty()
                : Optional.of(GearAttackSpeed.valueOf(attackSpeedJson.getAsString()));

        List<GearMajorId> majorIds = parseMajorIds(json);
        List<Pair<GearDamageType, RangedValue>> damages = parseDamages(json);
        List<Pair<Element, Integer>> defences = parseDefences(json);

        return new GearStatsFixed(healthBuff, skillBuffs, attackSpeed, majorIds, damages, defences);
    }

    private List<GearMajorId> parseMajorIds(JsonObject json) {
        JsonElement majorIdsJson = json.get("majorIds");
        if (majorIdsJson == null || majorIdsJson.isJsonNull()) return List.of();

        return majorIdsJson.getAsJsonArray().asList().stream()
                .map(majorIdName -> Managers.GearInfo.getMajorIdFromId(majorIdName.getAsString()))
                .filter(majorId -> majorId != null)
                .toList();
    }

    private List<Pair<Skill, Integer>> parseSkillBuffs(JsonObject json) {
        List<Pair<Skill, Integer>> list = new ArrayList<>();
        for (Skill skill : Skill.values()) {
            String skillJsonName = skill.getApiName() + "Points";
            JsonElement skillJson = json.get(skillJsonName);
            if (skillJson == null) continue;

            int minPoints = skillJson.getAsInt();
            if (minPoints == 0) continue;

            list.add(Pair.of(skill, minPoints));
        }

        // Return an immutable list
        return List.copyOf(list);
    }

    private List<Pair<GearDamageType, RangedValue>> parseDamages(JsonObject json) {
        List<Pair<GearDamageType, RangedValue>> list = new ArrayList<>();
        // First look for elemental damage
        for (Element element : Element.values()) {
            String damageJsonName = element.name().toLowerCase(Locale.ROOT) + "Damage";
            JsonElement damageJson = json.get(damageJsonName);
            if (damageJson == null) continue;

            String rangeString = damageJson.getAsString();
            RangedValue range = RangedValue.fromString(rangeString);
            if (range.equals(RangedValue.NONE)) continue;

            list.add(Pair.of(GearDamageType.fromElement(element), range));
        }
        // Then check neutral damage, which has a non-standard name
        JsonElement damageJson = json.get("damage");
        if (damageJson != null) {
            String rangeString = damageJson.getAsString();
            RangedValue range = RangedValue.fromString(rangeString);
            if (!range.equals(RangedValue.NONE)) {
                list.add(Pair.of(GearDamageType.NEUTRAL, range));
            }
        }

        // Return an immutable list
        return List.copyOf(list);
    }

    private List<Pair<Element, Integer>> parseDefences(JsonObject json) {
        List<Pair<Element, Integer>> list = new ArrayList<>();
        for (Element element : Element.values()) {
            String skillJsonName = element.name().toLowerCase(Locale.ROOT) + "Defense";
            JsonElement skillJson = json.get(skillJsonName);
            if (skillJson == null) continue;

            int minPoints = skillJson.getAsInt();
            if (minPoints == 0) continue;

            list.add(Pair.of(element, minPoints));
        }

        // Return an immutable list
        return List.copyOf(list);
    }

    private List<Pair<GearStat, RangedValue>> parseStatsIdentified(JsonObject json) {
        List<Pair<GearStat, RangedValue>> list = new ArrayList<>();
        JsonElement identifiedJson = json.get("identified");
        boolean preIdentified = identifiedJson != null && identifiedJson.getAsBoolean();

        for (GearStat stat : Managers.GearInfo.gearStatRegistry) {
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
            return RangedValue.of(baseValue, baseValue);
        } else {
            if (baseValue > 0) {
                // Between 30% and 130% of base value, always at least 1
                int min = Math.max((int) Math.round(baseValue * 0.3), 1);
                int max = (int) Math.round(baseValue * 1.3);
                return RangedValue.of(min, max);
            } else {
                // Between 70% and 130% of base value, always at most -1
                // Round ties towards positive infinity (confirmed on Wynncraft)
                int min = (int) Math.round(baseValue * 1.3);
                int max = Math.min((int) Math.round(baseValue * 0.7), -1);
                return RangedValue.of(min, max);
            }
        }
    }
}
