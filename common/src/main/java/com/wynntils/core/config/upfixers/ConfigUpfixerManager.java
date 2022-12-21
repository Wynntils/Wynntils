/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.upfixers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.config.upfixers.impl.CustomCommandKeybindSlashStartUpfixer;
import com.wynntils.core.config.upfixers.impl.CustomPoiVisbilityUpfixer;
import java.util.ArrayList;
import java.util.List;

public class ConfigUpfixerManager extends Manager {
    public static final String UPFIXER_JSON_MEMBER_NAME = "wynntils.upfixers";

    private final List<ConfigUpfixer> configUpfixers = new ArrayList<>();

    public ConfigUpfixerManager() {
        super(List.of());

        // Register upfixers here, in order of run priority
        registerUpfixer(new CustomPoiVisbilityUpfixer());
        registerUpfixer(new CustomCommandKeybindSlashStartUpfixer());
    }

    public void registerUpfixer(ConfigUpfixer upfixer) {
        configUpfixers.add(upfixer);
    }

    /**
     * Runs all registered upfixers on the given config object.
     * @param configObject The config object to run upfixers on.
     */
    public boolean runUpfixers(JsonObject configObject) {
        List<ConfigUpfixer> missingUpfixers = getMissingUpfixers(configObject);

        boolean anyChange = false;

        for (ConfigUpfixer upfixer : missingUpfixers) {
            try {
                if (upfixer.apply(configObject)) {
                    anyChange = true;
                    addUpfixerToConfig(configObject, upfixer);
                    WynntilsMod.info("Applied upfixer \"" + upfixer.getUpfixerName() + "\" to config.");
                }
            } catch (Throwable t) {
                WynntilsMod.warn("Failed to apply upfixer \"" + upfixer.getUpfixerName() + "\" to config file!", t);
            }
        }

        return anyChange;
    }

    private void addUpfixerToConfig(JsonObject configObject, ConfigUpfixer upfixer) {
        JsonArray upfixers = configObject.getAsJsonArray(UPFIXER_JSON_MEMBER_NAME);

        if (upfixers == null) {
            upfixers = new JsonArray();
            configObject.add(UPFIXER_JSON_MEMBER_NAME, upfixers);
        }

        upfixers.add(upfixer.getUpfixerName());
    }

    private List<ConfigUpfixer> getMissingUpfixers(JsonObject configObject) {
        if (!configObject.has(UPFIXER_JSON_MEMBER_NAME)) return configUpfixers;

        JsonArray upfixers = configObject.getAsJsonArray(UPFIXER_JSON_MEMBER_NAME);
        if (upfixers == null) return configUpfixers;

        List<String> appliedUpfixers = new ArrayList<>();
        for (JsonElement upfixer : upfixers) {
            appliedUpfixers.add(upfixer.getAsString());
        }

        return configUpfixers.stream()
                .filter(upfixer -> !appliedUpfixers.contains(upfixer.getUpfixerName()))
                .toList();
    }
}
