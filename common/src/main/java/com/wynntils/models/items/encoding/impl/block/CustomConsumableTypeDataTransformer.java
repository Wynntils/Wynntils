/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.gear.type.ConsumableType;
import com.wynntils.models.items.encoding.data.CustomConsumableTypeData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;

public class CustomConsumableTypeDataTransformer extends DataTransformer<CustomConsumableTypeData> {
    @Override
    protected ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, CustomConsumableTypeData data) {
        return switch (version) {
            case VERSION_1, VERSION_2 -> encodeCustomConsumableTypeData(data);
        };
    }

    @Override
    public ErrorOr<CustomConsumableTypeData> decodeData(
            ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1, VERSION_2 -> decodeCustomConsumableTypeData(byteReader);
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.CUSTOM_CONSUMABLE_TYPE_DATA_TRANSFORMER.getId();
    }

    private ErrorOr<UnsignedByte[]> encodeCustomConsumableTypeData(CustomConsumableTypeData data) {
        // The data is a single byte, containing the id of the type of the item.
        return ErrorOr.of(
                new UnsignedByte[] {UnsignedByte.of((byte) data.consumableType().getEncodingId())});
    }

    private ErrorOr<CustomConsumableTypeData> decodeCustomConsumableTypeData(ArrayReader<UnsignedByte> byteReader) {
        // The data is a single byte, containing the id of the type of the item.
        int typeId = byteReader.read().value();
        ConsumableType type = ConsumableType.fromEncodingId(typeId);
        if (type == null) {
            return ErrorOr.error("Unknown consumable type id: " + typeId);
        }

        return ErrorOr.of(new CustomConsumableTypeData(type));
    }
}
