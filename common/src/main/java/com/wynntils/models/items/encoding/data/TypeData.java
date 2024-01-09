/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.encoding.type.ItemType;
import com.wynntils.utils.type.UnsignedByte;

public record TypeData(ItemType itemType) implements ItemData {
    public static TypeData fromByte(UnsignedByte versionByte) {
        return new TypeData(ItemType.fromEncodingId(versionByte.toByte()));
    }
}
