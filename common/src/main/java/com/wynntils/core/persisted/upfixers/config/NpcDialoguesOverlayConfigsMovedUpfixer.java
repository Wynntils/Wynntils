/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.config;

import com.wynntils.core.persisted.upfixers.RenamedKeysUpfixer;
import com.wynntils.utils.type.Pair;
import java.util.List;

public class NpcDialoguesOverlayConfigsMovedUpfixer extends RenamedKeysUpfixer {
    // In a previous upfixer, we change "npcDialogueOverlayFeature." to "npcDialogueFeature.",
    // hence we need to name the old keys here accordingly.
    private static final List<Pair<String, String>> RENAMED_KEYS = List.of(
            Pair.of("npcDialogueFeature.npcDialogueOverlay.autoProgress", "npcDialogueFeature.autoProgress"),
            Pair.of(
                    "npcDialogueFeature.npcDialogueOverlay.dialogAutoProgressDefaultTime",
                    "npcDialogueFeature.dialogAutoProgressDefaultTime"),
            Pair.of(
                    "npcDialogueFeature.npcDialogueOverlay.dialogAutoProgressAdditionalTimePerWord",
                    "npcDialogueFeature.dialogAutoProgressAdditionalTimePerWord"));

    @Override
    protected List<Pair<String, String>> getRenamedKeys() {
        return RENAMED_KEYS;
    }
}
