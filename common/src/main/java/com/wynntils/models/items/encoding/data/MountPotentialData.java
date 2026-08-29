/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.items.game.MountItem;

public record MountPotentialData(Integer potential) implements ItemData {
    public static MountPotentialData from(MountItem mountItem) {
        return new MountPotentialData(mountItem.getMountInfo().potential());
    }
}
