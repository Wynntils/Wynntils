/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.upfixers.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.upfixers.ConfigUpfixer;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Set;

public class RenamedConfigsUpfixer implements ConfigUpfixer {
    private static final List<Pair<String, String>> RENAMED_KEYS = List.of(
            Pair.of("questInfoOverlayFeature.autoTrackQuestCoordinates", "contentTrackerFeature.autoTrackCoordinates"),
            Pair.of(
                    "questInfoOverlayFeature.disableQuestTrackingOnScoreboard",
                    "contentTrackerOverlayFeature.disableTrackerOnScoreboard"),
            Pair.of("wynntilsQuestBookFeature.playSoundOnUpdate", "contentTrackerFeature.playSoundOnUpdate"));

    @Override
    public boolean apply(JsonObject configObject, Set<ConfigHolder> configHolders) {
        for (Pair<String, String> renamePair : RENAMED_KEYS) {
            String oldName = renamePair.a();
            String newName = renamePair.b();
            if (!configObject.has(oldName)) continue;

            JsonElement jsonElement = configObject.get(oldName);
            configObject.remove(oldName);

            configObject.add(newName, jsonElement);
        }

        return true;
    }
}
