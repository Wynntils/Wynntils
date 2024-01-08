/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.encoding.data.CustomGearTypeData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;

public class CustomGearTypeTransformer extends DataTransformer<CustomGearTypeData> {
    @Override
    protected ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, CustomGearTypeData data) {
        return switch (version) {
            case VERSION_1 -> {
                if (data.gearType().getId() == -1) {
                    yield ErrorOr.error("Gear type cannot be encoded.");
                }

                yield ErrorOr.of(new UnsignedByte[] {
                    new UnsignedByte((byte) data.gearType().getId())
                });
            }
        };
    }

    @Override
    public ErrorOr<CustomGearTypeData> decodeData(
            ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1 -> ErrorOr.of(
                    new CustomGearTypeData(GearType.fromId(byteReader.read().value())));
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.CUSTOM_GEAR_TYPE_TRANSFORMER.getId();
    }
}
