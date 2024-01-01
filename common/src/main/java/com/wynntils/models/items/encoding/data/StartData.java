/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.type.UnsignedByte;

public record StartData(ItemTransformingVersion version) implements ItemData {
    public static StartData fromByte(UnsignedByte versionByte) {
        return new StartData(ItemTransformingVersion.fromId(versionByte.toByte()));
    }
}
