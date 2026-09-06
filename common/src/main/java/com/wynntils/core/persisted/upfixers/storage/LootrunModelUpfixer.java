/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.storage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.upfixers.Upfixer;
import java.util.Map;
import java.util.Set;

public class LootrunModelUpfixer implements Upfixer {
    private static final String DRY_PULLS_KEY_OLD = "models.lootrun.dryPulls";
    private static final String DRY_PULLS_KEY_NEW = "models.lootrunReward.dryPulls";
    private static final String EXPECTED_PULLS_KEY_OLD = "models.lootrun.expectedPulls";
    private static final String EXPECTED_PULLS_KEY_NEW = "models.lootrunReward.expectedPulls";

    private static final String LOOTRUN_DETAILS_KEY = "model.lootrun.lootrunDetailsStorage";
    private static final String LOOTRUN_BEACON_DETAILS_KEY = "model.lootrunBeacon.lootrunBeaconDetailsStorage";
    private static final String LOOTRUN_MISSION_DETAILS_KEY = "model.mission.lootrunMissionDetailsStorage";
    private static final String LOOTRUN_TRIAL_DETAILS_KEY = "model.trial.lootrunTrialDetailsStorage";

    @Override
    public boolean apply(JsonObject configObject, Set<PersistedValue<?>> persisteds) {
        if (configObject.has(DRY_PULLS_KEY_OLD)) {
            configObject.addProperty(
                    DRY_PULLS_KEY_NEW, configObject.get(DRY_PULLS_KEY_OLD).getAsInt());
            configObject.remove(DRY_PULLS_KEY_OLD);
        }

        if (configObject.has(EXPECTED_PULLS_KEY_OLD)) {
            configObject.addProperty(
                    EXPECTED_PULLS_KEY_NEW,
                    configObject.get(EXPECTED_PULLS_KEY_OLD).getAsInt());
            configObject.remove(EXPECTED_PULLS_KEY_OLD);
        }

        if (configObject.has(LOOTRUN_DETAILS_KEY)) {
            JsonObject lootrunDetailsStorage = configObject.getAsJsonObject(LOOTRUN_DETAILS_KEY);
            JsonObject lootrunBeaconDetailsStorage = new JsonObject();
            JsonObject lootrunMissionDetailsStorage = new JsonObject();
            JsonObject lootrunTrialDetailsStorage = new JsonObject();

            for (Map.Entry<String, JsonElement> entry : lootrunDetailsStorage.entrySet()) {
                String characterId = entry.getKey();
                JsonObject oldDetails = entry.getValue().getAsJsonObject();

                JsonObject newLootrunDetails = new JsonObject();
                newLootrunDetails.addProperty(
                        "sacrifices", oldDetails.get("sacrifices").getAsInt());
                newLootrunDetails.addProperty(
                        "rerolls", oldDetails.get("rerolls").getAsInt());

                JsonObject newBeaconDetails = new JsonObject();
                newBeaconDetails.add("selectedBeacons", oldDetails.get("selectedBeacons"));
                newBeaconDetails.add("lastTaskBeaconColor", oldDetails.get("lastTaskBeaconColor"));
                newBeaconDetails.addProperty(
                        "lastTaskVibrantBeacon",
                        oldDetails.get("lastTaskVibrantBeacon").getAsBoolean());
                newBeaconDetails.add("closestBeacon", oldDetails.get("closestBeacon"));
                newBeaconDetails.addProperty(
                        "redBeaconTaskCount",
                        oldDetails.get("redBeaconTaskCount").getAsInt());
                newBeaconDetails.add("orangeBeaconCounts", oldDetails.get("orangeBeaconCounts"));
                newBeaconDetails.addProperty(
                        "orangeAmount", oldDetails.get("orangeAmount").getAsInt());
                newBeaconDetails.addProperty(
                        "rainbowBeaconCount",
                        oldDetails.get("rainbowBeaconCount").getAsInt());
                newBeaconDetails.addProperty(
                        "rainbowAmount", oldDetails.get("rainbowAmount").getAsInt());

                JsonObject newMissionDetails = new JsonObject();
                newMissionDetails.add("missions", oldDetails.get("missions"));

                JsonObject newTrialDetails = new JsonObject();
                newTrialDetails.add("trials", oldDetails.get("trials"));

                lootrunDetailsStorage.add(characterId, newLootrunDetails);
                lootrunBeaconDetailsStorage.add(characterId, newBeaconDetails);
                lootrunMissionDetailsStorage.add(characterId, newMissionDetails);
                lootrunTrialDetailsStorage.add(characterId, newTrialDetails);
            }

            configObject.add(LOOTRUN_BEACON_DETAILS_KEY, lootrunBeaconDetailsStorage);
            configObject.add(LOOTRUN_MISSION_DETAILS_KEY, lootrunMissionDetailsStorage);
            configObject.add(LOOTRUN_TRIAL_DETAILS_KEY, lootrunTrialDetailsStorage);
        }

        return true;
    }
}
