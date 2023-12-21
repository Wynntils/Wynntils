/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.items.encoding.data.StartData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;

public class StartDataTransformer extends DataTransformer<StartData> {
    public static final byte ID = 0;

    /**
     * Decodes the start data block. This is specially handled by the {@link ItemTransformerRegistry}.
     * @param byteReader The byte reader to read the data from.
     * @return The decoded start data.
     */
    public static ErrorOr<StartData> decodeData(ArrayReader<UnsignedByte> byteReader) {
        UnsignedByte idByte = byteReader.read();
        if (idByte.value() != ID) {
            return ErrorOr.error("Encoded data does not start with a start data block.");
        }

        UnsignedByte versionByte = byteReader.read();

        StartData startData = StartData.fromByte(versionByte);
        if (startData.version() == null) {
            return ErrorOr.error("Unknown version: " + versionByte);
        }

        return ErrorOr.of(startData);
    }

    @Override
    public ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, StartData data) {
        return switch (version) {
            case VERSION_1 -> ErrorOr.of(new UnsignedByte[] {
                UnsignedByte.of(data.version().getId()),
            });
        };
    }

    @Override
    public ErrorOr<StartData> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        // NOOP, should never be called
        throw new IllegalStateException("StartDataTransformer should never be called to decode data");
    }

    @Override
    protected byte getId() {
        return ID;
    }
}
