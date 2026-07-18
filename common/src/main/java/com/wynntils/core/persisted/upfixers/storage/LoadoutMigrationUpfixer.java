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
        JsonObject abilityTrees = configObject.has(OLD_ABILITY_TREE_KEY)
                ? configObject.getAsJsonObject(OLD_ABILITY_TREE_KEY) : null;

        JsonObject aspects = configObject.has(OLD_ASPECT_KEY)
                ? configObject.getAsJsonObject(OLD_ASPECT_KEY) : null;

        JsonObject skillPoints = configObject.has(OLD_SKILL_POINT_KEY)
                ? configObject.getAsJsonObject(OLD_SKILL_POINT_KEY) : null;

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

        migrateGearNameArray(skillPointObject, "armourNames");
        migrateGearNameArray(skillPointObject, "accessoryNames");

        return skillPointObject;
    }

    private void migrateGearNameArray(JsonObject skillPointObject, String key) {
        if (!skillPointObject.has(key) || !skillPointObject.get(key).isJsonArray()) return;

        JsonArray oldNames = skillPointObject.getAsJsonArray(key);
        JsonArray newNames = new JsonArray();

        for (JsonElement nameElement : oldNames) {
            if (nameElement.isJsonNull()) {
                newNames.add(nameElement);
                continue;
            }

            String rawName = nameElement.getAsString();
            // Fall back to the raw name if lookup/encoding fails, rather than
            // silently dropping the item from the loadout
            newNames.add(encodeDefaultGearItem(rawName).orElse(rawName));
        }

        skillPointObject.add(key, newNames);
    }

    private static Optional<String> encodeDefaultGearItem(String rawName) {
        String cleanName = rawName.replaceFirst("§.", "");
        GearInfo gearInfo = Models.Gear.getGearInfoFromDisplayName(cleanName);
        if (gearInfo == null) {
            WynntilsMod.warn("Upfixer: no gear info found for " + cleanName);
            return Optional.empty();
        }

        List<StatActualValue> stats = new ArrayList<>();

        //make some stats so that items have some kind of stats.
        for (Map.Entry<StatType, StatPossibleValues> entry : gearInfo.getVariableStatsMap().entrySet()) {
            StatType statType = entry.getKey();
            StatPossibleValues val = entry.getValue();
            RangedValue internalRoll = StatCalculator.calculateInternalRollRange(val, val.baseValue(), 0);
            stats.add(new StatActualValue(statType, val.baseValue(), 0, internalRoll, false));
        }

        GearInstance gearInstance = new GearInstance(stats, List.of(), 0, Optional.empty(), Optional.empty(), true, Optional.empty());
        GearItem defaultGearItem = new GearItem(gearInfo, gearInstance);

        EncodingSettings encodingSettings = new EncodingSettings(true, true);
        ErrorOr<EncodedByteBuffer> errorOrEncoded =
                Models.ItemEncoding.encodeItem(defaultGearItem, encodingSettings);
        if (errorOrEncoded.hasError()) {
            WynntilsMod.warn("Upfixer: failed to encode " + cleanName + ": " + errorOrEncoded.getError());
            return Optional.empty();
        }

        return Optional.of(Models.ItemEncoding.makeItemString(defaultGearItem, errorOrEncoded.getValue()));
    }
}