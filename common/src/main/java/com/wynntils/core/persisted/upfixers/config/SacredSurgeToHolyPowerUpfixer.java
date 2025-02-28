/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.config;

import com.wynntils.core.persisted.upfixers.RenamedPrefixesUpfixer;
import com.wynntils.utils.type.Pair;
import java.util.List;

public class SacredSurgeToHolyPowerUpfixer extends RenamedPrefixesUpfixer {
    private static final List<Pair<String, String>> RENAMED_PREFIXES = List.of(
            Pair.of("gameBarsOverlayFeature.sacredSurgeBarOverlay.", "gameBarsOverlayFeature.holyPowerBarOverlay."));

    @Override
    protected List<Pair<String, String>> getRenamedPrefixes() {
        return RENAMED_PREFIXES;
    }
}
