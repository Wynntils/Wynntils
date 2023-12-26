/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.items.encoding.data.NameData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.UnsignedByteUtils;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;
import java.util.ArrayList;
import java.util.List;

public class NameDataTransformer extends DataTransformer<NameData> {
    @Override
    public ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, NameData data) {
        return switch (version) {
            case VERSION_1 -> ErrorOr.of(encodeName(data.name()));
        };
    }

    @Override
    public ErrorOr<NameData> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1 -> decodeName(byteReader);
        };
    }

    private UnsignedByte[] encodeName(String name) {
        return UnsignedByteUtils.encodeString(name);
    }

    private ErrorOr<NameData> decodeName(ArrayReader<UnsignedByte> byteReader) {
        List<UnsignedByte> bytes = new ArrayList<>();

        // Read until we find a null byte
        do {
            bytes.add(byteReader.read());
        } while (byteReader.hasRemaining() && byteReader.peek().value() != 0);

        // If this is the case, the byte reader ended, and we didn't find a null byte
        UnsignedByte nullByte = byteReader.read();
        if (nullByte.value() != 0) {
            return ErrorOr.error("Name data is not null terminated");
        }

        return ErrorOr.of(new NameData(UnsignedByteUtils.decodeString(bytes)));
    }

    @Override
    public byte getId() {
        return 2;
    }
}
