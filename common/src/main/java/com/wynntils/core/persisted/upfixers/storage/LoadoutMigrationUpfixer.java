/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.upfixers.Upfixer;
import com.wynntils.models.character.type.SavableGear;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.gear.type.GearInstanceRequirements;
import com.wynntils.models.items.encoding.type.EncodingSettings;
import com.wynntils.models.items.encoding.type.ItemType;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.RangedValue;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class LoadoutMigrationUpfixer implements Upfixer {
    private static final String OLD_ABILITY_TREE_KEY = "model.abilityTree.abilityTreeLoadouts";
    private static final String OLD_ASPECT_KEY = "model.aspect.aspectLoadouts";
    private static final String OLD_SKILL_POINT_KEY = "model.skillPoint.skillPointLoadouts";
    private static final String NEW_LOADOUT_KEY = "service.loadout.loadouts";

    @Override
    public boolean apply(JsonObject configObject, Set<PersistedValue<?>> persisteds) {
        JsonObject abilityTrees =
                configObject.has(OLD_ABILITY_TREE_KEY) ? configObject.getAsJsonObject(OLD_ABILITY_TREE_KEY) : null;

        JsonObject aspects = configObject.has(OLD_ASPECT_KEY) ? configObject.getAsJsonObject(OLD_ASPECT_KEY) : null;

        JsonObject skillPoints =
                configObject.has(OLD_SKILL_POINT_KEY) ? configObject.getAsJsonObject(OLD_SKILL_POINT_KEY) : null;

        if (abilityTrees == null && aspects == null && skillPoints == null) {
            return true;
        }

        JsonObject newLoadouts = new JsonObject();
        Set<String> allNames = new TreeSet<>();

        if (abilityTrees != null) {
            allNames.addAll(abilityTrees.keySet());
        }
        if (aspects != null) {
            allNames.addAll(aspects.keySet());
        }
        if (skillPoints != null) {
            allNames.addAll(skillPoints.keySet());
        }

        for (String name : allNames) {
            JsonObject savedLoadout = new JsonObject();

            JsonElement abilityTree = abilityTrees != null ? abilityTrees.get(name) : null;
            JsonElement aspect = aspects != null ? aspects.get(name) : null;
            JsonElement skillPoint = skillPoints != null ? skillPoints.get(name) : null;

            int componentCount =
                    (abilityTree != null ? 1 : 0) + (aspect != null ? 1 : 0) + (skillPoint != null ? 1 : 0);

            String type;
            if (componentCount > 1) {
                type = "build";
            } else if (abilityTree != null) {
                type = "abilityTree";
            } else if (aspect != null) {
                type = "aspect";
            } else {
                type = "skillPoint";
            }

            savedLoadout.addProperty("name", name);
            savedLoadout.addProperty("type", type);

            if (skillPoint != null) {
                savedLoadout.add("skillPoints", migrateSkillPointGearNames(skillPoint));
            }
            if (abilityTree != null) {
                savedLoadout.add("abilityTree", abilityTree);
            }
            if (aspect != null) {
                savedLoadout.add("aspects", aspect);
            }

            savedLoadout.add("tomes", null);
            savedLoadout.addProperty("favorited", false);

            newLoadouts.add(name, savedLoadout);
        }

        configObject.add(NEW_LOADOUT_KEY, newLoadouts);
        configObject.remove(OLD_ABILITY_TREE_KEY);
        configObject.remove(OLD_ASPECT_KEY);
        configObject.remove(OLD_SKILL_POINT_KEY);

        return true;
    }

    private JsonElement migrateSkillPointGearNames(JsonElement skillPointElement) {
        if (skillPointElement == null || !skillPointElement.isJsonObject()) return skillPointElement;

        JsonObject oldSkillPointObject = skillPointElement.getAsJsonObject();
        JsonObject skillPointObject = new JsonObject();

        skillPointObject.addProperty(
                "strength",
                oldSkillPointObject.has("strength")
                        ? oldSkillPointObject.get("strength").getAsInt()
                        : 0);
        skillPointObject.addProperty(
                "dexterity",
                oldSkillPointObject.has("dexterity")
                        ? oldSkillPointObject.get("dexterity").getAsInt()
                        : 0);
        skillPointObject.addProperty(
                "intelligence",
                oldSkillPointObject.has("intelligence")
                        ? oldSkillPointObject.get("intelligence").getAsInt()
                        : 0);
        skillPointObject.addProperty(
                "defence",
                oldSkillPointObject.has("defence")
                        ? oldSkillPointObject.get("defence").getAsInt()
                        : 0);
        skillPointObject.addProperty(
                "agility",
                oldSkillPointObject.has("agility")
                        ? oldSkillPointObject.get("agility").getAsInt()
                        : 0);

        skillPointObject.add("weapon", JsonNull.INSTANCE);

        if (oldSkillPointObject.has("weapon")
                && !oldSkillPointObject.get("weapon").isJsonNull()) {
            String weaponName = oldSkillPointObject.get("weapon").getAsString();
            encodeDefaultGearItem(weaponName)
                    .ifPresentOrElse(
                            savableGear -> skillPointObject.add("weapon", savableItemToJson(savableGear)),
                            () -> WynntilsMod.warn("Upfixer: could not encode weapon " + weaponName));
        }

        JsonArray armourArray = migrateArmourNameArray(oldSkillPointObject);
        JsonArray accessoryArray = migrateAccessoryNameArray(oldSkillPointObject);

        skillPointObject.add("armourNames", Objects.requireNonNullElseGet(armourArray, JsonArray::new));
        skillPointObject.add("accessoryNames", Objects.requireNonNullElseGet(accessoryArray, JsonArray::new));

        return skillPointObject;
    }

    private JsonArray migrateArmourNameArray(JsonObject oldSkillPointObject) {
        if (!oldSkillPointObject.has("armourNames")
                || !oldSkillPointObject.get("armourNames").isJsonArray()) return null;

        JsonArray oldNames = oldSkillPointObject.getAsJsonArray("armourNames");
        // 0=helmet, 1=chestplate, 2=leggings, 3=boots
        SavableGear[] slots = {new SavableGear(), new SavableGear(), new SavableGear(), new SavableGear()};

        for (JsonElement nameElement : oldNames) {
            if (nameElement.isJsonNull()) continue;

            String rawName = nameElement.getAsString();
            resolveGearInfo(rawName).ifPresent(gearInfo -> {
                int slotIndex =
                        switch (gearInfo.type()) {
                            case HELMET -> 0;
                            case CHESTPLATE -> 1;
                            case LEGGINGS -> 2;
                            case BOOTS -> 3;
                            default -> -1;
                        };

                if (slotIndex == -1) {
                    WynntilsMod.warn("Upfixer: unexpected gear type for armour " + rawName + ": " + gearInfo.type());
                    return;
                }

                encodeGearItem(gearInfo)
                        .ifPresentOrElse(
                                savableGear -> slots[slotIndex] = savableGear,
                                () -> WynntilsMod.warn("Upfixer: could not encode armour " + rawName));
            });
        }

        JsonArray newNames = new JsonArray();
        for (SavableGear slot : slots) {
            newNames.add(savableItemToJson(slot));
        }
        return newNames;
    }

    private JsonArray migrateAccessoryNameArray(JsonObject oldSkillPointObject) {
        if (!oldSkillPointObject.has("accessoryNames")
                || !oldSkillPointObject.get("accessoryNames").isJsonArray()) return null;

        JsonArray oldNames = oldSkillPointObject.getAsJsonArray("accessoryNames");
        // 0=ring1, 1=ring2, 2=bracelet, 3=necklace
        SavableGear[] slots = {new SavableGear(), new SavableGear(), new SavableGear(), new SavableGear()};

        for (JsonElement nameElement : oldNames) {
            if (nameElement.isJsonNull()) continue;

            String rawName = nameElement.getAsString();
            resolveGearInfo(rawName).ifPresent(gearInfo -> {
                Integer slotIndex =
                        switch (gearInfo.type()) {
                            case RING ->
                                slots[0].encoded().isEmpty()
                                        ? 0
                                        : (slots[1].encoded().isEmpty() ? 1 : null);
                            case BRACELET -> slots[2].encoded().isEmpty() ? 2 : null;
                            case NECKLACE -> slots[3].encoded().isEmpty() ? 3 : null;
                            default -> null;
                        };

                if (slotIndex == null) {
                    WynntilsMod.warn(
                            "Upfixer: no available accessory slot for " + rawName + " (" + gearInfo.type() + ")");
                    return;
                }

                encodeGearItem(gearInfo)
                        .ifPresentOrElse(
                                savableGear -> slots[slotIndex] = savableGear,
                                () -> WynntilsMod.warn("Upfixer: could not encode accessory " + rawName));
            });
        }

        JsonArray newNames = new JsonArray();
        for (SavableGear slot : slots) {
            newNames.add(savableItemToJson(slot));
        }
        return newNames;
    }

    private static Optional<GearInfo> resolveGearInfo(String rawName) {
        String removeFormatting = rawName.replaceAll("§.", "");
        String cleanName = WynnUtils.stripItemNameMarkers(removeFormatting);
        GearInfo gearInfo = Models.Gear.getGearInfoFromDisplayName(cleanName);
        if (gearInfo == null) {
            WynntilsMod.warn("Upfixer: no gear info found for " + cleanName);
        }
        return Optional.ofNullable(gearInfo);
    }

    private static Optional<SavableGear> encodeGearItem(GearInfo gearInfo) {
        List<StatActualValue> stats = new ArrayList<>();

        for (Map.Entry<StatType, StatPossibleValues> entry :
                gearInfo.getVariableStatsMap().entrySet()) {
            StatType statType = entry.getKey();
            StatPossibleValues val = entry.getValue();
            RangedValue internalRoll = StatCalculator.calculateInternalRollRange(val, val.baseValue(), false);
            stats.add(new StatActualValue(statType, val.baseValue(), false, internalRoll, false));
        }

        GearInstance gearInstance = new GearInstance(
                stats,
                List.of(),
                0,
                Optional.empty(),
                Optional.empty(),
                GearInstanceRequirements.UNKNOWN,
                Optional.empty());
        GearItem defaultGearItem = new GearItem(gearInfo, gearInstance);

        EncodingSettings encodingSettings = new EncodingSettings(true, true);
        ErrorOr<EncodedByteBuffer> errorOrEncoded = Models.ItemEncoding.encodeItem(defaultGearItem, encodingSettings);
        if (errorOrEncoded.hasError()) {
            WynntilsMod.warn("Upfixer: failed to encode " + gearInfo.name() + ": " + errorOrEncoded.getError());
            return Optional.empty();
        }

        return Optional.of(new SavableGear(errorOrEncoded.getValue().toBase64String(), gearInfo.name()));
    }

    private static Optional<SavableGear> encodeDefaultGearItem(String rawName) {
        return resolveGearInfo(rawName).flatMap(LoadoutMigrationUpfixer::encodeGearItem);
    }

    private static JsonObject savableItemToJson(SavableGear savableGear) {
        JsonObject json = new JsonObject();
        json.addProperty("encoded", savableGear.encoded());
        json.addProperty("itemName", savableGear.itemName());

        String itemType = "gear";
        if (savableGear.itemType() == ItemType.CRAFTED_GEAR) {
            itemType = "craftedGear";
        }

        json.addProperty("itemType", itemType);
        return json;
    }
}
