/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.config;

import com.wynntils.core.persisted.upfixers.RenamedPrefixesUpfixer;
import com.wynntils.utils.type.Pair;
import java.util.List;

public class OverlayRestructuringUpfixer extends RenamedPrefixesUpfixer {
    private static final List<Pair<String, String>> RENAMED_PREFIXES = List.of(
            Pair.of(
                    "spellCastRenderFeature.spellCastOverlay.",
                    "spellCastMessageOverlayFeature.spellCastMessageOverlay."),
            Pair.of("spellCastRenderFeature.", "spellCastVignetteFeature."),
            Pair.of("tokenTrackerFeature.tokenBarsOverlay.", "tokenBarsOverlayFeature.tokenBarsOverlay."),
            Pair.of("tokenTrackerFeature.", "tokenTrackerBellFeature."),
            Pair.of(
                    "shamanTotemTrackingFeature.shamanTotemTimerOverlay1.",
                    "shamanTotemTimerOverlayFeature.shamanTotemTimerOverlay1."),
            Pair.of(
                    "auraTimerOverlayFeature.auraTimerOverlay1.",
                    "towerAuraTimerOverlayFeature.towerAuraTimerOverlay1."),
            Pair.of("auraTimerOverlayFeature.", "towerAuraVignetteFeature."),
            Pair.of("arrowShieldTrackingFeature.", "arrowShieldTrackerOverlayFeature."),
            Pair.of("customPlayerListFeature.", "customPlayerListOverlayFeature."),
            Pair.of("guildAttackTimerOverlayFeature.", "territoryAttackTimerOverlayFeature."),
            Pair.of("hadesPartyOverlayFeature.", "partyMembersOverlayFeature."),
            Pair.of("mobTotemTrackingFeature.", "mobTotemTimerOverlayFeature."),
            Pair.of("shamanMasksOverlayFeature.", "shamanMaskOverlayFeature."),
            Pair.of("statusOverlayFeature.statusOverlay.", "statusEffectsOverlayFeature.statusEffectsOverlay."),
            Pair.of("statusOverlayFeature.", "statusEffectsOverlayFeature."));

    @Override
    protected List<Pair<String, String>> getRenamedPrefixes() {
        return RENAMED_PREFIXES;
    }
}
