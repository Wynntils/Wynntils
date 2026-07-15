package com.wynntils.core.persisted.upfixers.storage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.upfixers.Upfixer;
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
                savedLoadout.add("skillPoints", skillPoint);
            }
            if (abilityTree != null) {
                savedLoadout.add("abilityTree", abilityTree);
            }
            if (aspect != null) {
                savedLoadout.add("aspects", aspect);
            }

            savedLoadout.addProperty("favourited", false);
            savedLoadout.addProperty("createdAt", System.currentTimeMillis());

            newLoadouts.add(name, savedLoadout);
        }

        configObject.add(NEW_LOADOUT_KEY, newLoadouts);
        configObject.remove(OLD_ABILITY_TREE_KEY);
        configObject.remove(OLD_ASPECT_KEY);
        configObject.remove(OLD_SKILL_POINT_KEY);

        return true;
    }
}