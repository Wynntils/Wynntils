/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.upfixers.Upfixer;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.items.encoding.type.EncodingSettings;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            return false;
        }

        JsonObject newLoadouts = new JsonObject();
        Set<String> allNames = new TreeSet<>();

        if (abilityTrees != null) allNames.addAll(abilityTrees.keySet());
        if (aspects != null) allNames.addAll(aspects.keySet());
        if (skillPoints != null) allNames.addAll(skillPoints.keySet());

        for (String name : allNames) {
            JsonObject savedLoadout = new JsonObject();

            JsonElement abilityTree = abilityTrees != null ? abilityTrees.get(name) : null;
            JsonElement aspect = aspects != null ? aspects.get(name) : null;
            JsonElement skillPoint = skillPoints != null ? skillPoints.get(name) : null;

            int componentCount = 0;
            if (abilityTree != null) componentCount++;
            if (aspect != null) componentCount++;
            if (skillPoint != null) componentCount++;

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
            savedLoadout.addProperty("favourited", false);

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

        JsonObject skillPointObject = skillPointElement.getAsJsonObject();

        if (skillPointObject.has("weapon") && !skillPointObject.get("weapon").isJsonNull()) {
            String weaponName = skillPointObject.get("weapon").getAsString();
            encodeDefaultGearItem(weaponName)
                    .ifPresentOrElse(
                            encoded -> skillPointObject.addProperty("weapon", encoded),
                            () -> WynntilsMod.warn("Upfixer: could not encode weapon " + weaponName));
        }

        migrateArmourNameArray(skillPointObject, "armourNames");
        migrateAccessoryNameArray(skillPointObject, "accessoryNames");

        return skillPointObject;
    }

    private void migrateArmourNameArray(JsonObject skillPointObject, String key) {
        if (!skillPointObject.has(key) || !skillPointObject.get(key).isJsonArray()) return;

        JsonArray oldNames = skillPointObject.getAsJsonArray(key);
        String[] slots = new String[4]; // 0=helmet, 1=chestplate, 2=leggings, 3=boots

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
                                encoded -> slots[slotIndex] = encoded,
                                () -> WynntilsMod.warn("Upfixer: could not encode armour " + rawName));
            });
        }

        JsonArray newNames = new JsonArray();
        for (String slot : slots) {
            newNames.add(slot);
        }
        skillPointObject.add(key, newNames);
    }

    private void migrateAccessoryNameArray(JsonObject skillPointObject, String key) {
        if (!skillPointObject.has(key) || !skillPointObject.get(key).isJsonArray()) return;

        JsonArray oldNames = skillPointObject.getAsJsonArray(key);
        String[] slots = new String[4]; // 0=ring1, 1=ring2, 2=bracelet, 3=necklace

        for (JsonElement nameElement : oldNames) {
            if (nameElement.isJsonNull()) continue;

            String rawName = nameElement.getAsString();
            resolveGearInfo(rawName).ifPresent(gearInfo -> {
                Integer slotIndex =
                        switch (gearInfo.type()) {
                            case RING -> slots[0] == null ? 0 : (slots[1] == null ? 1 : null);
                            case BRACELET -> slots[2] == null ? 2 : null;
                            case NECKLACE -> slots[3] == null ? 3 : null;
                            default -> null;
                        };

                if (slotIndex == null) {
                    WynntilsMod.warn(
                            "Upfixer: no available accessory slot for " + rawName + " (" + gearInfo.type() + ")");
                    return;
                }

                encodeGearItem(gearInfo)
                        .ifPresentOrElse(
                                encoded -> slots[slotIndex] = encoded,
                                () -> WynntilsMod.warn("Upfixer: could not encode accessory " + rawName));
            });
        }

        JsonArray newNames = new JsonArray();
        for (String slot : slots) {
            newNames.add(slot);
        }
        skillPointObject.add(key, newNames);
    }

    private static Optional<GearInfo> resolveGearInfo(String rawName) {
        String cleanName = rawName.replaceFirst("§.", "");
        GearInfo gearInfo = Models.Gear.getGearInfoFromDisplayName(cleanName);
        if (gearInfo == null) {
            WynntilsMod.warn("Upfixer: no gear info found for " + cleanName);
        }
        return Optional.ofNullable(gearInfo);
    }

    private static Optional<String> encodeGearItem(GearInfo gearInfo) {
        List<StatActualValue> stats = new ArrayList<>();

        for (Map.Entry<StatType, StatPossibleValues> entry :
                gearInfo.getVariableStatsMap().entrySet()) {
            StatType statType = entry.getKey();
            StatPossibleValues val = entry.getValue();
            RangedValue internalRoll = StatCalculator.calculateInternalRollRange(val, val.baseValue(), 0);
            stats.add(new StatActualValue(statType, val.baseValue(), 0, internalRoll, false));
        }

        GearInstance gearInstance =
                new GearInstance(stats, List.of(), 0, Optional.empty(), Optional.empty(), true, Optional.empty());
        GearItem defaultGearItem = new GearItem(gearInfo, gearInstance);

        EncodingSettings encodingSettings = new EncodingSettings(true, true);
        ErrorOr<EncodedByteBuffer> errorOrEncoded = Models.ItemEncoding.encodeItem(defaultGearItem, encodingSettings);
        if (errorOrEncoded.hasError()) {
            WynntilsMod.warn("Upfixer: failed to encode " + gearInfo.name() + ": " + errorOrEncoded.getError());
            return Optional.empty();
        }

        return Optional.of(Models.ItemEncoding.makeItemString(defaultGearItem, errorOrEncoded.getValue()));
    }

    private static Optional<String> encodeDefaultGearItem(String rawName) {
        return resolveGearInfo(rawName).flatMap(LoadoutMigrationUpfixer::encodeGearItem);
    }
}
