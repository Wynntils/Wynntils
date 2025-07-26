/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.beacons;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.activities.type.ActivityType;
import com.wynntils.models.beacons.type.BeaconMarkerKind;
import java.util.regex.Pattern;

public enum ActivityBeaconMarkerKind implements BeaconMarkerKind {
    QUEST("\uE007", ActivityType.QUEST),
    STORYLINE_QUEST("\uE009", ActivityType.STORYLINE_QUEST),
    MINI_QUEST("\uE006", ActivityType.MINI_QUEST),
    WORLD_EVENT("\uE00A", ActivityType.WORLD_EVENT),
    DISCOVERY("\uE002", ActivityType.WORLD_DISCOVERY),
    CAVE("\uE003", ActivityType.CAVE),
    DUNGEON("\uE004", ActivityType.DUNGEON),
    RAID("\uE008", ActivityType.RAID),
    BOSS_ALTAR("\uE001", ActivityType.BOSS_ALTAR),
    LOOTRUN_CAMP("\uE005", ActivityType.LOOTRUN_CAMP);

    private static final String MARKER_PREFIX = "\uDAFF\uDFF8[\uE010-\uE014]\uDAFF\uDFDE";
    private static final String MARKER_SUFFIX = "\n(\\d+m (§[a-z0-9])?(\uE000|\uE001)?)?";

    private final ActivityType activityType;
    private final Pattern iconPattern;

    ActivityBeaconMarkerKind(String iconCharacter, ActivityType activityType) {
        this.iconPattern = Pattern.compile(MARKER_PREFIX + iconCharacter + MARKER_SUFFIX);
        this.activityType = activityType;
    }

    @Override
    public boolean matches(StyledText styledText) {
        return styledText.matches(iconPattern);
    }

    public ActivityType getActivityType() {
        return activityType;
    }
}
