/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.mount.type.MountColorInfo;

public record MountColorData(MountColorInfo primaryColorInfo, MountColorInfo secondaryColorInfo) implements ItemData {
    public static MountColorData from(MountItem mountItem) {
        return new MountColorData(
                mountItem.getMountInfo().primaryColorInfo(),
                mountItem.getMountInfo().secondaryColorInfo());
    }
}
