/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.mount.type.MountStat;
import java.util.Map;

public record MountStatsMaxData(boolean estimatedMaxStats, Map<MountStat, Integer> maxStats) implements ItemData {
    public static MountStatsMaxData from(MountItem mountItem) {
        return new MountStatsMaxData(
                mountItem.getMountInfo().estimatedMaxStats(),
                mountItem.getMountInfo().maxStats());
    }
}
