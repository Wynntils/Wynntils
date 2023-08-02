/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.upfixers.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.upfixers.ConfigUpfixer;
import java.util.List;
import java.util.Set;

public class GameBarOverlayMoveUpfixer implements ConfigUpfixer {
    private static final List<String> KEYS_TO_CHANGE = List.of(
            "customBarsOverlayFeature.awakenedProgressBarOverlay.flip",
            "customBarsOverlayFeature.awakenedProgressBarOverlay.horizontalAlignmentOverride",
            "customBarsOverlayFeature.awakenedProgressBarOverlay.position",
            "customBarsOverlayFeature.awakenedProgressBarOverlay.shouldDisplayOriginal",
            "customBarsOverlayFeature.awakenedProgressBarOverlay.size",
            "customBarsOverlayFeature.awakenedProgressBarOverlay.textColor",
            "customBarsOverlayFeature.awakenedProgressBarOverlay.textShadow",
            "customBarsOverlayFeature.awakenedProgressBarOverlay.userEnabled",
            "customBarsOverlayFeature.awakenedProgressBarOverlay.verticalAlignmentOverride",
            "customBarsOverlayFeature.bloodPoolBarOverlay.flip",
            "customBarsOverlayFeature.bloodPoolBarOverlay.healthTexture",
            "customBarsOverlayFeature.bloodPoolBarOverlay.horizontalAlignmentOverride",
            "customBarsOverlayFeature.bloodPoolBarOverlay.position",
            "customBarsOverlayFeature.bloodPoolBarOverlay.shouldDisplayOriginal",
            "customBarsOverlayFeature.bloodPoolBarOverlay.size",
            "customBarsOverlayFeature.bloodPoolBarOverlay.textColor",
            "customBarsOverlayFeature.bloodPoolBarOverlay.textShadow",
            "customBarsOverlayFeature.bloodPoolBarOverlay.userEnabled",
            "customBarsOverlayFeature.bloodPoolBarOverlay.verticalAlignmentOverride",
            "customBarsOverlayFeature.corruptedBarOverlay.flip",
            "customBarsOverlayFeature.corruptedBarOverlay.horizontalAlignmentOverride",
            "customBarsOverlayFeature.corruptedBarOverlay.position",
            "customBarsOverlayFeature.corruptedBarOverlay.shouldDisplayOriginal",
            "customBarsOverlayFeature.corruptedBarOverlay.size",
            "customBarsOverlayFeature.corruptedBarOverlay.textColor",
            "customBarsOverlayFeature.corruptedBarOverlay.textShadow",
            "customBarsOverlayFeature.corruptedBarOverlay.userEnabled",
            "customBarsOverlayFeature.corruptedBarOverlay.verticalAlignmentOverride",
            "customBarsOverlayFeature.focusBarOverlay.flip",
            "customBarsOverlayFeature.focusBarOverlay.horizontalAlignmentOverride",
            "customBarsOverlayFeature.focusBarOverlay.position",
            "customBarsOverlayFeature.focusBarOverlay.shouldDisplayOriginal",
            "customBarsOverlayFeature.focusBarOverlay.size",
            "customBarsOverlayFeature.focusBarOverlay.textColor",
            "customBarsOverlayFeature.focusBarOverlay.textShadow",
            "customBarsOverlayFeature.focusBarOverlay.userEnabled",
            "customBarsOverlayFeature.focusBarOverlay.verticalAlignmentOverride",
            "customBarsOverlayFeature.healthBarOverlay.flip",
            "customBarsOverlayFeature.healthBarOverlay.healthTexture",
            "customBarsOverlayFeature.healthBarOverlay.horizontalAlignmentOverride",
            "customBarsOverlayFeature.healthBarOverlay.position",
            "customBarsOverlayFeature.healthBarOverlay.shouldDisplayOriginal",
            "customBarsOverlayFeature.healthBarOverlay.size",
            "customBarsOverlayFeature.healthBarOverlay.textColor",
            "customBarsOverlayFeature.healthBarOverlay.textShadow",
            "customBarsOverlayFeature.healthBarOverlay.userEnabled",
            "customBarsOverlayFeature.healthBarOverlay.verticalAlignmentOverride",
            "customBarsOverlayFeature.manaBankBarOverlay.flip",
            "customBarsOverlayFeature.manaBankBarOverlay.horizontalAlignmentOverride",
            "customBarsOverlayFeature.manaBankBarOverlay.manaTexture",
            "customBarsOverlayFeature.manaBankBarOverlay.position",
            "customBarsOverlayFeature.manaBankBarOverlay.shouldDisplayOriginal",
            "customBarsOverlayFeature.manaBankBarOverlay.size",
            "customBarsOverlayFeature.manaBankBarOverlay.textColor",
            "customBarsOverlayFeature.manaBankBarOverlay.textShadow",
            "customBarsOverlayFeature.manaBankBarOverlay.userEnabled",
            "customBarsOverlayFeature.manaBankBarOverlay.verticalAlignmentOverride",
            "customBarsOverlayFeature.manaBarOverlay.flip",
            "customBarsOverlayFeature.manaBarOverlay.horizontalAlignmentOverride",
            "customBarsOverlayFeature.manaBarOverlay.manaTexture",
            "customBarsOverlayFeature.manaBarOverlay.position",
            "customBarsOverlayFeature.manaBarOverlay.shouldDisplayOriginal",
            "customBarsOverlayFeature.manaBarOverlay.size",
            "customBarsOverlayFeature.manaBarOverlay.textColor",
            "customBarsOverlayFeature.manaBarOverlay.textShadow",
            "customBarsOverlayFeature.manaBarOverlay.userEnabled",
            "customBarsOverlayFeature.manaBarOverlay.verticalAlignmentOverride",
            "customBarsOverlayFeature.userEnabled");

    private static final String NEW_KEY_PREFIX = "gameBarsOverlayFeature.";

    @Override
    public boolean apply(JsonObject configObject, Set<Config<?>> configs) {
        for (String key : KEYS_TO_CHANGE) {
            if (!configObject.has(key)) continue;

            JsonElement jsonElement = configObject.get(key);
            configObject.remove(key);

            String newKey = NEW_KEY_PREFIX + key.substring(key.indexOf('.') + 1);
            configObject.add(newKey, jsonElement);
        }

        return true;
    }
}
