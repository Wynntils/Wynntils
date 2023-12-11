/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.items.encoding.data.NameData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.UnsignedByteUtils;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;

public class NameDataTransformer extends DataTransformer<NameData> {
    public static final byte ID = 2;

    @Override
    public ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, NameData data) {
        return switch (version) {
            case VERSION_1 -> ErrorOr.of(encodeName(data.name()));
        };
    }

    private UnsignedByte[] encodeName(String name) {
        return UnsignedByteUtils.encodeString(name);
    }

    @Override
    protected byte getId() {
        return ID;
    }
}
