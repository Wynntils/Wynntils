/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.type;

import java.util.Map;

public record HistoricRaidInfo(
        String name, String abbreviation, Map<Integer, RaidRoomInfo> challenges, long endedTimestamp) {}
