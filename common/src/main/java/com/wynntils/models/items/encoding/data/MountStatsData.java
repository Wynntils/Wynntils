/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.mount.type.MountStat;
import com.wynntils.utils.type.CappedValue;
import java.util.Map;

public record MountStatsData(Map<MountStat, CappedValue> stats) implements ItemData {
    public static MountStatsData from(MountItem mountItem) {
        return new MountStatsData(mountItem.getMountInfo().stats());
    }
}
