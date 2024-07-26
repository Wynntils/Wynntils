/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.type;

import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;

/**
 * Interface for transforming data into bytes.
 * @param <T> The type of data to transform.
 */
public abstract class DataTransformer<T extends ItemData> {
    public final ErrorOr<UnsignedByte[]> encode(ItemTransformingVersion version, T data) {
        if (!shouldEncodeData(version, data)) return ErrorOr.of(new UnsignedByte[0]);

        ErrorOr<UnsignedByte[]> errorOrData = encodeData(version, data);
        if (errorOrData.hasError()) {
            return errorOrData;
        }

        UnsignedByte[] dataBytes = errorOrData.getValue();

        UnsignedByte[] bytes = new UnsignedByte[dataBytes.length + 1];
        bytes[0] = UnsignedByte.of(getId());
        System.arraycopy(dataBytes, 0, bytes, 1, dataBytes.length);

        return ErrorOr.of(bytes);
    }

    protected abstract ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, T data);

    protected boolean shouldEncodeData(ItemTransformingVersion version, T data) {
        return true;
    }

    public abstract ErrorOr<T> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader);

    public abstract byte getId();
}
