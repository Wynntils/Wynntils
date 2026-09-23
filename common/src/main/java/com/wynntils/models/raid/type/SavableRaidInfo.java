/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.type;

import java.util.Collections;
import java.util.Map;

public record SavableRaidInfo(String raidName, long raidStartTime, Map<Integer, SavableRaidRoomInfo> challenges) {
    public static final SavableRaidInfo EMPTY = new SavableRaidInfo("", -1L, Collections.emptyMap());
}
