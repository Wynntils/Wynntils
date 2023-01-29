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
import com.wynntils.core.components.Models;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.concepts.Element;
import com.wynntils.models.concepts.Skill;
import com.wynntils.models.gearinfo.type.GearAttackSpeed;
import com.wynntils.models.gearinfo.type.GearDropType;
import com.wynntils.models.gearinfo.type.GearInfo;
import com.wynntils.models.gearinfo.type.GearMajorId;
import com.wynntils.models.gearinfo.type.GearMaterial;
import com.wynntils.models.gearinfo.type.GearMetaInfo;
import com.wynntils.models.gearinfo.type.GearRequirements;
import com.wynntils.models.gearinfo.type.GearRestrictions;
import com.wynntils.models.gearinfo.type.GearTier;
import com.wynntils.models.gearinfo.type.GearType;
import com.wynntils.models.stats.FixedStats;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import com.wynntils.utils.wynn.GearUtils;
import com.wynntils.utils.wynn.WynnUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class GearInfoRegistry {
    List<GearMajorId> majorIds = List.of();
    List<GearInfo> gearInfoRegistry = List.of();
    Map<String, GearInfo> gearInfoLookup = Map.of();

    public GearInfoRegistry() {
        loadRegistry();
    }

    public void reloadData() {
        loadRegistry();
    }

    private void loadRegistry() {
        // We must download and parse Major IDs before attempting to parse the gear DB
        Download majorIdsDl = Managers.Net.download(UrlId.DATA_STATIC_MAJOR_IDS);
        majorIdsDl.handleReader(majorIdsReader -> {
            Type type = new TypeToken<List<GearMajorId>>() {}.getType();
            Gson majorIdGson = new GsonBuilder()
                    .registerTypeHierarchyAdapter(GearMajorId.class, new GearMajorIdDeserializer())
                    .create();
            majorIds = majorIdGson.fromJson(majorIdsReader, type);

            // Now we can do the gear DB
            Download dl = Managers.Net.download(UrlId.DATA_STATIC_GEAR);
            dl.handleReader(reader -> {
                Gson gearInfoGson = new GsonBuilder()
                        .registerTypeHierarchyAdapter(GearInfo.class, new GearInfoDeserializer(majorIds))
                        .create();
                WynncraftGearInfoResponse gearInfoResponse =
                        gearInfoGson.fromJson(reader, WynncraftGearInfoResponse.class);

                // Remove the dummy "default" entry
                List<GearInfo> registry = gearInfoResponse.items.stream()
                        .filter(gearInfo -> !gearInfo.name().equals("default"))
                        .toList();

                // Create a fast lookup map
                Map<String, GearInfo> lookupMap = new HashMap<>();
                for (GearInfo gearInfo : registry) {
                    lookupMap.put(gearInfo.name(), gearInfo);
                    if (gearInfo.metaInfo().altName().isPresent()) {
                        lookupMap.put(gearInfo.metaInfo().altName().get(), gearInfo);
                    }
                }

                // Make it visisble to the world
                gearInfoRegistry = registry;
                gearInfoLookup = lookupMap;
            });
        });
    }

    private static final class GearMajorIdDeserializer implements JsonDeserializer<GearMajorId> {
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

    private static final class GearInfoDeserializer implements JsonDeserializer<GearInfo> {
        private final List<GearMajorId> majorIds;

        private GearInfoDeserializer(List<GearMajorId> majorIds) {
            this.majorIds = majorIds;
        }

        @Override
        public GearInfo deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            String primaryName = WynnUtils.normalizeBadString(json.get("name").getAsString());
            String secondaryName = WynnUtils.normalizeBadString(JsonUtils.getNullableJsonString(json, "displayName"));

            // After normalization, we can end up with the same name. Don't treat this as an altName
            if (primaryName.equals(secondaryName)) {
                secondaryName = "";
            }

            // The real name is the secondaryName if it exists. If so, the primary name is the altName
            String name = secondaryName.isEmpty() ? primaryName : secondaryName;
            String altName = secondaryName.isEmpty() ? null : primaryName;

            GearType type = parseType(json);
            GearTier tier = GearTier.fromString(json.get("tier").getAsString());
            int powderSlots = json.get("sockets").getAsInt();

            GearMetaInfo metaInfo = parseMetaInfo(json, altName, type);
            GearRequirements requirements = parseRequirements(json, type);
            FixedStats fixedStats = parseFixedStats(json);
            List<Pair<StatType, StatPossibleValues>> variableStats = parseVariableStats(json);

            return new GearInfo(name, type, tier, powderSlots, metaInfo, requirements, fixedStats, variableStats);
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

            String armorType =
                    JsonUtils.getNullableJsonString(json, "armorType").toUpperCase(Locale.ROOT);

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
            return GearUtils.getItemFromCodeAndDamage(itemTypeCode, damageCode);
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

            return Optional.of(WynnUtils.normalizeBadString(questName));
        }

        private FixedStats parseFixedStats(JsonObject json) {
            JsonElement healthJson = json.get("health");
            int healthBuff = healthJson == null ? 0 : healthJson.getAsInt();
            List<Pair<Skill, Integer>> skillBonuses = parseSkillBonuses(json);
            JsonElement attackSpeedJson = json.get("attackSpeed");
            Optional<GearAttackSpeed> attackSpeed = (attackSpeedJson == null)
                    ? Optional.empty()
                    : Optional.of(GearAttackSpeed.valueOf(attackSpeedJson.getAsString()));

            List<GearMajorId> majorIds = parseMajorIds(json);
            List<Pair<DamageType, RangedValue>> damages = parseDamages(json);
            List<Pair<Element, Integer>> defences = parseDefences(json);

            return new FixedStats(healthBuff, skillBonuses, attackSpeed, majorIds, damages, defences);
        }

        private List<GearMajorId> parseMajorIds(JsonObject json) {
            JsonElement majorIdsJson = json.get("majorIds");
            if (majorIdsJson == null || majorIdsJson.isJsonNull()) return List.of();

            return majorIdsJson.getAsJsonArray().asList().stream()
                    .map(majorIdName -> getMajorIdFromId(majorIdName.getAsString()))
                    .filter(Objects::nonNull)
                    .toList();
        }

        public GearMajorId getMajorIdFromId(String majorIdId) {
            // Check the "id" field of the "majorId", hence "majodIdId"
            return this.majorIds.stream()
                    .filter(mId -> mId.id().equals(majorIdId))
                    .findFirst()
                    .orElse(null);
        }

        private List<Pair<Skill, Integer>> parseSkillBonuses(JsonObject json) {
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

        private List<Pair<DamageType, RangedValue>> parseDamages(JsonObject json) {
            List<Pair<DamageType, RangedValue>> list = new ArrayList<>();
            // First look for neutral damage, which has a non-standard name
            JsonElement neutralDamageJson = json.get("damage");
            if (neutralDamageJson != null) {
                String rangeString = neutralDamageJson.getAsString();
                RangedValue range = RangedValue.fromString(rangeString);
                if (!range.equals(RangedValue.NONE)) {
                    list.add(Pair.of(DamageType.NEUTRAL, range));
                }
            }

            // Then check for elemental damage
            for (Element element : Element.values()) {
                String damageJsonName = element.name().toLowerCase(Locale.ROOT) + "Damage";
                JsonElement damageJson = json.get(damageJsonName);
                if (damageJson == null) continue;

                String rangeString = damageJson.getAsString();
                RangedValue range = RangedValue.fromString(rangeString);
                if (range.equals(RangedValue.NONE)) continue;

                list.add(Pair.of(DamageType.fromElement(element), range));
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

        private List<Pair<StatType, StatPossibleValues>> parseVariableStats(JsonObject json) {
            List<Pair<StatType, StatPossibleValues>> list = new ArrayList<>();
            JsonElement identifiedJson = json.get("identified");
            boolean preIdentified = identifiedJson != null && identifiedJson.getAsBoolean();

            for (StatType stat : Models.Stat.getAllStatTypes()) {
                JsonElement statJson = json.get(stat.getApiName());
                if (statJson == null) continue;

                int baseValue = statJson.getAsInt();
                if (baseValue == 0) continue;

                // "Inverted" stats (i.e. spell costs) will be stored as a positive value,
                // and only converted to negative at display time.
                if (stat.showAsInverted()) {
                    baseValue = -baseValue;
                }
                // Range will always be stored such as "low" means "worst possible value" and
                // "high" means "best possible value".
                RangedValue range = GearCalculator.calculateRange(baseValue, preIdentified);
                StatPossibleValues possibleValues = new StatPossibleValues(stat, range, baseValue, preIdentified);
                list.add(Pair.of(stat, possibleValues));
            }

            // Return an immutable list
            return List.copyOf(list);
        }
    }

    public static class WynncraftGearInfoResponse {
        List<GearInfo> items;
    }
}
