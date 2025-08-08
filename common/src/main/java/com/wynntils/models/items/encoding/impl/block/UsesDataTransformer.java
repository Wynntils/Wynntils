/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.items.encoding.data.UsesData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;

public class UsesDataTransformer extends DataTransformer<UsesData> {
    @Override
    protected ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, UsesData data) {
        return switch (version) {
            case VERSION_1, VERSION_2 -> encodeUsesData(data);
        };
    }

    @Override
    public ErrorOr<UsesData> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1, VERSION_2 -> decodeUsesData(byteReader);
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.USES_DATA_TRANSFORMER.getId();
    }

    private ErrorOr<UnsignedByte[]> encodeUsesData(UsesData data) {
        if (data.uses().current() < 0
                || data.uses().max() < 0
                || data.uses().current() > 255
                || data.uses().max() > 255) {
            return ErrorOr.error("Uses data does not fit a byte: " + data.uses());
        }

        UnsignedByte[] bytes = new UnsignedByte[2];
        // The first byte is the remaining uses for the item.
        bytes[0] = new UnsignedByte((byte) data.uses().current());

        // The second byte is the maximum uses for the item.
        bytes[1] = new UnsignedByte((byte) data.uses().max());

        return ErrorOr.of(bytes);
    }

    private ErrorOr<UsesData> decodeUsesData(ArrayReader<UnsignedByte> byteReader) {
        // The first byte is the remaining uses for the item.
        int currentUses = byteReader.read().value();

        // The second byte is the maximum uses for the item.
        int maxUses = byteReader.read().value();

        return ErrorOr.of(new UsesData(new CappedValue(currentUses, maxUses)));
    }
}
