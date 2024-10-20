/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.beacons;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.beacons.type.BeaconMarkerKind;
import java.util.regex.Pattern;

public enum ActivityBeaconMarkerKind implements BeaconMarkerKind {
    QUEST("\uE007"),
    STORYLINE_QUEST("\uE009"),
    MINI_QUEST("\uE006"),
    WORLD_EVENT("\uE00A"),
    DISCOVERY("\uE002"),
    CAVE("\uE003"),
    DUNGEON("\uE004"),
    RAID("\uE008"),
    BOSS_ALTAR("\uE001"),
    LOOTRUN_CAMP("\uE005");

    private static final String MARKER_PREFIX = "\uDAFF\uDFF8[\uE010-\uE014]\uDAFF\uDFDE";
    private static final String MARKER_SUFFIX = "(\n\\d+m (\uE000|\uE001)?)?";

    private final Pattern iconPattern;

    ActivityBeaconMarkerKind(String iconCharacter) {
        this.iconPattern = Pattern.compile(MARKER_PREFIX + iconCharacter + MARKER_SUFFIX);
    }

    @Override
    public boolean matches(StyledText styledText) {
        return styledText.matches(iconPattern);
    }
}
