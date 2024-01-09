/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.items.encoding.data.EndData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;

public class EndDataTransformer extends DataTransformer<EndData> {
    @Override
    public ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, EndData data) {
        // End data is always empty
        return ErrorOr.of(new UnsignedByte[0]);
    }

    @Override
    public ErrorOr<EndData> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        // End data is always empty
        return ErrorOr.of(new EndData());
    }

    @Override
    public byte getId() {
        return DataTransformerType.END_DATA_TRANSFORMER.getId();
    }
}
