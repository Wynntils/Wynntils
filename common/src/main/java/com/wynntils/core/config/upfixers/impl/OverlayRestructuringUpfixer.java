/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.upfixers.impl;

import com.wynntils.core.config.upfixers.RenamedPrefixesUpfixer;
import com.wynntils.utils.type.Pair;
import java.util.List;

public class OverlayRestructuringUpfixer extends RenamedPrefixesUpfixer {
    private static final List<Pair<String, String>> RENAMED_PREFIXES = List.of(
            Pair.of(
                    "spellCastRenderFeature.spellCastMessageOverlay.",
                    "spellCastMessageOverlayFeature.spellCastMessageOverlay."),
            Pair.of("spellCastRenderFeature.", "spellCastVignetteFeature."),
            Pair.of("tokenTrackerFeature.tokenBarsOverlay.", "tokenBarsOverlayFeature.tokenBarsOverlay."),
            Pair.of("tokenTrackerFeature.", "tokenTrackerBellFeature."),
            Pair.of("arrowShieldTrackingFeature.", "arrowShieldTrackerOverlayFeature."),
            Pair.of("dustomPlayerListFeature.", "customPlayerListOverlayFeature."),
            Pair.of("guildAttackTimerOverlayFeature.", "territoryAttackTimerOverlayFeature."),
            Pair.of("hadesPartyOverlayFeature.", "partyMembersOverlayFeature."),
            Pair.of("mobTotemTrackingFeature.", "mobTotemTimerOverlayFeature."),
            Pair.of("shamanMasksOverlayFeature.", "shamanMaskOverlayFeature."));

    @Override
    protected List<Pair<String, String>> getRenamedPrefixes() {
        return RENAMED_PREFIXES;
    }
}
