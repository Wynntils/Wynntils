/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.items.encoding.data.TypeData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;

public class TypeDataTransformer extends DataTransformer<TypeData> {
    @Override
    public ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, TypeData data) {
        return switch (version) {
            case VERSION_1, VERSION_2 ->
                ErrorOr.of(new UnsignedByte[] {UnsignedByte.of(data.itemType().getEncodingId())});
        };
    }

    @Override
    public ErrorOr<TypeData> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1, VERSION_2 -> decodeType(byteReader);
        };
    }

    private static ErrorOr<TypeData> decodeType(ArrayReader<UnsignedByte> byteReader) {
        TypeData typeData = TypeData.fromByte(byteReader.read());
        if (typeData.itemType() == null) {
            return ErrorOr.error("Unknown item type.");
        }

        return ErrorOr.of(typeData);
    }

    @Override
    public byte getId() {
        return DataTransformerType.TYPE_DATA_TRANSFORMER.getId();
    }
}
