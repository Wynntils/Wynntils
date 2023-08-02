/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.config;

import com.wynntils.core.persisted.upfixers.RenamedKeysUpfixer;
import com.wynntils.utils.type.Pair;
import java.util.List;

public class QuestBookToContentRenamedConfigsUpfixer extends RenamedKeysUpfixer {
    private static final List<Pair<String, String>> RENAMED_KEYS = List.of(
            Pair.of("questInfoOverlayFeature.autoTrackQuestCoordinates", "contentTrackerFeature.autoTrackCoordinates"),
            Pair.of(
                    "questInfoOverlayFeature.disableQuestTrackingOnScoreboard",
                    "contentTrackerOverlayFeature.disableTrackerOnScoreboard"),
            Pair.of("wynntilsQuestBookFeature.playSoundOnUpdate", "contentTrackerFeature.playSoundOnUpdate"));

    @Override
    protected List<Pair<String, String>> getRenamedKeys() {
        return RENAMED_KEYS;
    }
}
