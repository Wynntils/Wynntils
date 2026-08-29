/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.mount.type.MountType;

public record MountTypeData(MountType mountType) implements ItemData {
    public static MountTypeData from(MountItem mountItem) {
        return new MountTypeData(mountItem.getMountType());
    }
}
