/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.beacons;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.beacons.type.BeaconMarkerKind;
import com.wynntils.models.lootrun.type.LootrunTaskType;
import java.util.regex.Pattern;

public enum LootrunBeaconMarkerKind implements BeaconMarkerKind {
    SLAY("\uE020", LootrunTaskType.SLAY),
    TARGET("\uE021", LootrunTaskType.TARGET),
    DEFEND("\uE022", LootrunTaskType.DEFEND),
    SPELUNK("\uE023", LootrunTaskType.LOOT),
    DESTROY("\uE024", LootrunTaskType.DESTROY);

    private static final String MARKER_PREFIX = "\uDAFF\uDFF8[\uE010-\uE014]\uDAFF\uDFDE";
    private static final String MARKER_SUFFIX = "\n(\\d+m (§[a-z0-9])?(\uE000|\uE001)?)?";

    private final Pattern iconPattern;
    private final LootrunTaskType taskType;

    LootrunBeaconMarkerKind(String iconCharacter, LootrunTaskType taskType) {
        this.iconPattern = Pattern.compile(
                MARKER_PREFIX + "§(?:#)?(?:[a-z0-9]{1,8})\uE000(§r)?\uDAFF\uDFE6" + iconCharacter + MARKER_SUFFIX);
        this.taskType = taskType;
    }

    public LootrunTaskType getTaskType() {
        return taskType;
    }

    @Override
    public boolean matches(StyledText styledText) {
        return styledText.matches(iconPattern);
    }
}
