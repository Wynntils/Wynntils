/*
 * Copyright © Wynntils 2023-2024.
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
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.UnsignedByte;
import java.util.List;

public class CustomConsumableTypeDataTransformer extends DataTransformer<CustomConsumableTypeData> {
    private static final List<Pair<ConsumableType, Integer>> CONSUMABLE_TYPE_IDS = List.of(
            new Pair<>(ConsumableType.POTION, 0),
            new Pair<>(ConsumableType.FOOD, 1),
            new Pair<>(ConsumableType.SCROLL, 2));

    @Override
    protected ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, CustomConsumableTypeData data) {
        return switch (version) {
            case VERSION_1 -> encodeCustomConsumableTypeData(data);
        };
    }

    @Override
    public ErrorOr<CustomConsumableTypeData> decodeData(
            ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1 -> decodeCustomConsumableTypeData(byteReader);
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.CUSTOM_CONSUMABLE_TYPE_DATA_TRANSFORMER.getId();
    }

    private ErrorOr<UnsignedByte[]> encodeCustomConsumableTypeData(CustomConsumableTypeData data) {
        // The data is a single byte, containing the id of the type of the item.
        for (Pair<ConsumableType, Integer> consumableTypeId : CONSUMABLE_TYPE_IDS) {
            if (consumableTypeId.key() == data.consumableType()) {
                int id = consumableTypeId.value();
                return ErrorOr.of(new UnsignedByte[] {new UnsignedByte((byte) id)});
            }
        }

        return ErrorOr.error("Cannot encode consumable type: " + data.consumableType());
    }

    private ErrorOr<CustomConsumableTypeData> decodeCustomConsumableTypeData(ArrayReader<UnsignedByte> byteReader) {
        // The data is a single byte, containing the id of the type of the item.
        int typeId = byteReader.read().value();
        for (Pair<ConsumableType, Integer> consumableTypeId : CONSUMABLE_TYPE_IDS) {
            if (consumableTypeId.value() == typeId) {
                return ErrorOr.of(new CustomConsumableTypeData(consumableTypeId.key()));
            }
        }

        return ErrorOr.error("Cannot decode consumable type: " + typeId);
    }
}
