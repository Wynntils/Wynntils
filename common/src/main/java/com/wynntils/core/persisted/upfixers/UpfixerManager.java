/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.upfixers.config.CustomBankQuickJumpsUpfixer;
import com.wynntils.core.persisted.upfixers.config.CustomCommandKeybindSlashStartUpfixer;
import com.wynntils.core.persisted.upfixers.config.CustomPoiIconEnumBugUpfixer;
import com.wynntils.core.persisted.upfixers.config.CustomPoiVisbilityUpfixer;
import com.wynntils.core.persisted.upfixers.config.EnumNamingUpfixer;
import com.wynntils.core.persisted.upfixers.config.GameBarOverlayMoveUpfixer;
import com.wynntils.core.persisted.upfixers.config.MapToMainMapRenamedConfigsUpfixer;
import com.wynntils.core.persisted.upfixers.config.OverlayConfigsIntegrationUpfixer;
import com.wynntils.core.persisted.upfixers.config.OverlayRestructuringUpfixer;
import com.wynntils.core.persisted.upfixers.config.QuestBookToContentRenamedConfigsUpfixer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UpfixerManager extends Manager {
    public static final String UPFIXER_JSON_MEMBER_NAME = "wynntils.upfixers";

    private final List<Upfixer> upfixers = new ArrayList<>();

    public UpfixerManager() {
        super(List.of());

        // Register upfixers here, in order of run priority
        registerUpfixer(new CustomPoiVisbilityUpfixer());
        registerUpfixer(new CustomCommandKeybindSlashStartUpfixer());
        registerUpfixer(new GameBarOverlayMoveUpfixer());
        registerUpfixer(new EnumNamingUpfixer());
        registerUpfixer(new CustomPoiIconEnumBugUpfixer());
        registerUpfixer(new QuestBookToContentRenamedConfigsUpfixer());
        registerUpfixer(new MapToMainMapRenamedConfigsUpfixer());
        registerUpfixer(new OverlayRestructuringUpfixer());
        registerUpfixer(new OverlayConfigsIntegrationUpfixer());
        registerUpfixer(new CustomBankQuickJumpsUpfixer());
    }

    private void registerUpfixer(Upfixer upfixer) {
        upfixers.add(upfixer);
    }

    /**
     * Runs all registered upfixers on the given config object.
     *
     * @param configObject  The config object to run upfixers on.
     * @param configs All registered configs
     */
    public boolean runUpfixers(JsonObject configObject, Set<PersistedValue<?>> configs) {
        List<Upfixer> missingUpfixers = getMissingUpfixers(configObject);

        boolean anyChange = false;

        for (Upfixer upfixer : missingUpfixers) {
            try {
                if (upfixer.apply(configObject, configs)) {
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

    private void addUpfixerToConfig(JsonObject configObject, Upfixer upfixer) {
        JsonArray upfixers = configObject.getAsJsonArray(UPFIXER_JSON_MEMBER_NAME);

        if (upfixers == null) {
            upfixers = new JsonArray();
            configObject.add(UPFIXER_JSON_MEMBER_NAME, upfixers);
        }

        upfixers.add(upfixer.getUpfixerName());
    }

    private List<Upfixer> getMissingUpfixers(JsonObject configObject) {
        if (!configObject.has(UPFIXER_JSON_MEMBER_NAME)) return upfixers;

        JsonArray upfixers = configObject.getAsJsonArray(UPFIXER_JSON_MEMBER_NAME);
        if (upfixers == null) return this.upfixers;

        List<String> appliedUpfixers = new ArrayList<>();
        for (JsonElement upfixer : upfixers) {
            appliedUpfixers.add(upfixer.getAsString());
        }

        return this.upfixers.stream()
                .filter(upfixer -> !appliedUpfixers.contains(upfixer.getUpfixerName()))
                .toList();
    }
}
