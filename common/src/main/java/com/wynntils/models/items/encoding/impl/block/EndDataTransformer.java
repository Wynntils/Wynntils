/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.items.encoding.data.EndData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;

public class EndDataTransformer extends DataTransformer<EndData> {
    public static final byte ID = (byte) 255;

    @Override
    public ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, EndData data) {
        // End data is always empty
        return ErrorOr.of(new UnsignedByte[0]);
    }

    @Override
    protected byte getId() {
        return ID;
    }
}
