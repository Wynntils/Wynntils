/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.ingredients;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.concepts.Element;
import com.wynntils.models.concepts.ProfessionType;
import com.wynntils.models.concepts.Skill;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.GearDropType;
import com.wynntils.models.gear.type.GearMaterial;
import com.wynntils.models.gear.type.GearMetaInfo;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.stats.FixedStats;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import com.wynntils.utils.wynn.WynnUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class IngredientInfoRegistry {
    // This is a list of entries in the API that do not really reflect actual ingredients in Wynncraft
    // FIXME: This should be read from an external json file, and have more entries added to it
    private static final List<String> INVALID_ENTRIES = List.of();

    List<IngredientInfo> ingredientInfoRegistry = List.of();
    Map<String, IngredientInfo> ingredientInfoLookup = Map.of();
    Map<String, IngredientInfo> ingredientInfoLookupApiName = Map.of();

    public IngredientInfoRegistry() {
        loadRegistry();
    }

    public void reloadData() {
        loadRegistry();
    }

    private void loadRegistry() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_INGREDIENTS);
        dl.handleReader(reader -> {
            Gson ingredientInfoGson = new GsonBuilder()
                    .registerTypeHierarchyAdapter(IngredientInfo.class, new IngredientInfoDeserializer())
                    .create();
            WynncraftIngredientInfoResponse ingredientInfoResponse =
                    ingredientInfoGson.fromJson(reader, WynncraftIngredientInfoResponse.class);

            // Some entries are test entries etc and should be removed
            // FIXME: Is this really needed for ingredients?
            List<IngredientInfo> registry = ingredientInfoResponse.ingredients.stream()
                    .filter(ingredientInfo -> !INVALID_ENTRIES.contains(ingredientInfo.name()))
                    .toList();

            // Create fast lookup maps
            Map<String, IngredientInfo> lookupMap = new HashMap<>();
            Map<String, IngredientInfo> altLookupMap = new HashMap<>();
            for (IngredientInfo ingredientInfo : registry) {
                lookupMap.put(ingredientInfo.name(), ingredientInfo);
                if (ingredientInfo.apiName().isPresent()) {
                    altLookupMap.put(ingredientInfo.apiName().get(), ingredientInfo);
                }
            }

            // Make the result visisble to the world
            ingredientInfoRegistry = registry;
            ingredientInfoLookup = lookupMap;
            ingredientInfoLookupApiName = altLookupMap;
        });
    }

    private static final class IngredientInfoDeserializer implements JsonDeserializer<IngredientInfo> {
        @Override
        public IngredientInfo deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            String primaryName = WynnUtils.normalizeBadString(json.get("name").getAsString());
            String secondaryName = WynnUtils.normalizeBadString(JsonUtils.getNullableJsonString(json, "displayName"));

            // After normalization, we can end up with the same name. If so, treat this as not having
            // a secondary name.
            if (primaryName.equals(secondaryName)) {
                secondaryName = "";
            }

            // The real name (display name) is the secondaryName if it exists, otherwise it is
            // the primary name.
            // If the secondary name exists, the primary name is the apiName. If the apiName
            // does not exist, the api name is the same as the displayName.
            String name = secondaryName.isEmpty() ? primaryName : secondaryName;
            String apiName = secondaryName.isEmpty() ? null : primaryName;
            Optional<String> apiNameOpt = Optional.ofNullable(apiName);

            /*
            json fields:

                 "identifications"
            */

            int tier = JsonUtils.getNullableJsonInt(json, "tier");
            int level = json.get("level").getAsInt();

            List<ProfessionType> professions = new ArrayList<>();
            JsonArray professionsJson = json.get("skills").getAsJsonArray();
            for (JsonElement professionJson : professionsJson) {
                String professionName = professionJson.getAsString();
                ProfessionType professionType = ProfessionType.fromString(professionName);
                professions.add(professionType);
            }

            JsonObject sprite = json.get("sprite").getAsJsonObject();
            GearMaterial material;
            if (sprite != null
                    && !sprite.isJsonNull()
                    && sprite.getAsJsonObject().size() != 0) {
                int id = JsonUtils.getNullableJsonInt(sprite, "id");
                int damage = JsonUtils.getNullableJsonInt(sprite, "damage");

                material = GearMaterial.fromItemTypeCode(id, damage);
            } else {
                // FIXME: Bad?
                WynntilsMod.warn("Ingredient DB is missing sprite for " + name);
                material = null;
            }

            // Get consumables-only parts
            JsonObject consumableIdsJson = JsonUtils.getNullableJsonObject(json, "consumableOnlyIDs");
            int duration = JsonUtils.getNullableJsonInt(consumableIdsJson, "duration");
            int charges = JsonUtils.getNullableJsonInt(consumableIdsJson, "charges");

            // Get items-only parts
            List<Pair<Skill, Integer>> skillRequirements = new ArrayList<>();
            JsonObject itemIdsJson = JsonUtils.getNullableJsonObject(json, "itemOnlyIDs");
            int durabilityModifier = JsonUtils.getNullableJsonInt(itemIdsJson, "durabilityModifier");
            for (Skill skill : Skill.values()) {
                // In particular, note that the API spelling "defense" is not used
                String requirementName = skill.getDisplayName().toLowerCase(Locale.ROOT) + "Requirement";
                int requirementValue = JsonUtils.getNullableJsonInt(itemIdsJson, requirementName);
                if (requirementValue != 0) {
                    skillRequirements.add(Pair.of(skill, requirementValue));
                }
            }

            // Get recipe position format modifiers
            JsonObject positionsJson = JsonUtils.getNullableJsonObject(json, "ingredientPositionModifiers");
            Map<IngredientPosition, Integer> positionModifiers = new HashMap<>();
            for (IngredientPosition position : IngredientPosition.values()) {
                int value = JsonUtils.getNullableJsonInt(positionsJson, position.getApiName());
                if (value == 0) continue;

                positionModifiers.put(position, value);
            }

            List<Pair<StatType, StatPossibleValues>> variableStats = null; // parseVariableStats(json);

            return new IngredientInfo(
                    name,
                    tier,
                    level,
                    apiNameOpt,
                    material,
                    Collections.unmodifiableList(professions),
                    Collections.unmodifiableList(skillRequirements),
                    Collections.unmodifiableMap(positionModifiers),
                    duration,
                    charges,
                    durabilityModifier,
                    variableStats);
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

        private GearMetaInfo parseMetaInfo(JsonObject json, String apiName, GearType type) {
            GearRestrictions restrictions = parseRestrictions(json);
            GearMaterial material = parseMaterial(json, type);
            GearDropType dropType = GearDropType.fromString(json.get("dropType").getAsString());

            Optional<String> loreOpt = parseLore(json);
            Optional<String> apiNameOpt = Optional.ofNullable(apiName);

            JsonElement allowCraftsmanJson = json.get("allowCraftsman");
            boolean allowCraftsman = allowCraftsmanJson != null && allowCraftsmanJson.getAsBoolean();

            return new GearMetaInfo(restrictions, material, dropType, loreOpt, apiNameOpt, allowCraftsman);
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

            String materialType =
                    JsonUtils.getNullableJsonString(json, "armorType").toLowerCase(Locale.ROOT);

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

            return GearMaterial.fromArmorType(materialType, gearType, color);
        }

        private GearMaterial parseOtherMaterial(JsonObject json, GearType gearType) {
            String material = JsonUtils.getNullableJsonString(json, "material");
            if (material == null) {
                // We're screwed. The best we can do is to give a generic default representation
                // for this gear type
                return GearMaterial.fromGearType(gearType);
            }

            String[] materialArray = material.split(":");
            int itemTypeCode = Integer.parseInt(materialArray[0]);
            int damageCode = materialArray.length > 1 ? Integer.parseInt(materialArray[1]) : 0;
            return GearMaterial.fromItemTypeCode(itemTypeCode, damageCode);
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

            List<Pair<DamageType, RangedValue>> damages = parseDamages(json);
            List<Pair<Element, Integer>> defences = parseDefences(json);

            return new FixedStats(healthBuff, skillBonuses, attackSpeed, null, damages, defences);
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
            addDamageStat(list, DamageType.NEUTRAL, json.get("damage"));

            // Then check for elemental damage
            for (Element element : Element.values()) {
                String damageName = element.name().toLowerCase(Locale.ROOT) + "Damage";
                addDamageStat(list, DamageType.fromElement(element), json.get(damageName));
            }

            // Return an immutable list
            return List.copyOf(list);
        }

        private void addDamageStat(
                List<Pair<DamageType, RangedValue>> list, DamageType damageType, JsonElement damageJson) {
            if (damageJson == null) return;

            String rangeString = damageJson.getAsString();
            RangedValue range = RangedValue.fromString(rangeString);
            if (range.equals(RangedValue.NONE)) return;

            list.add(Pair.of(damageType, range));
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

            for (StatType statType : Models.Stat.getAllStatTypes()) {
                JsonElement statJson = json.get(statType.getApiName());
                if (statJson == null) continue;

                int baseValue = statJson.getAsInt();
                if (baseValue == 0) continue;

                // "Inverted" stats (i.e. spell costs) will be stored as a positive value,
                // and only converted to negative at display time.
                if (statType.showAsInverted()) {
                    baseValue = -baseValue;
                }
                // Range will always be stored such as "low" means "worst possible value" and
                // "high" means "best possible value".
                RangedValue range = StatCalculator.calculatePossibleValuesRange(baseValue, preIdentified);
                StatPossibleValues possibleValues = new StatPossibleValues(statType, range, baseValue, preIdentified);
                list.add(Pair.of(statType, possibleValues));
            }

            // Return an immutable list
            return List.copyOf(list);
        }
    }

    protected static class WynncraftIngredientInfoResponse {
        List<IngredientInfo> ingredients;
    }
}
