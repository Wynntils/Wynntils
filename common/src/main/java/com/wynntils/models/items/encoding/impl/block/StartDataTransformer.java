/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.items.encoding.data.StartData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;

public class StartDataTransformer extends DataTransformer<StartData> {
    public static final byte ID = 0;

    @Override
    public ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, StartData data) {
        return switch (version) {
            case VERSION_1 -> ErrorOr.of(new UnsignedByte[] {
                UnsignedByte.of(data.version().getId()),
            });
        };
    }

    @Override
    protected byte getId() {
        return ID;
    }
}
