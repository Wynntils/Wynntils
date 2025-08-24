/*
 * Copyright © Wynntils 2023-2025.
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
            case VERSION_1, VERSION_2 -> {
                if (data.gearType().getEncodingId() == -1) {
                    yield ErrorOr.error("Gear type cannot be encoded.");
                }

                yield ErrorOr.of(new UnsignedByte[] {
                    new UnsignedByte((byte) data.gearType().getEncodingId())
                });
            }
        };
    }

    @Override
    public ErrorOr<CustomGearTypeData> decodeData(
            ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1, VERSION_2 -> {
                GearType gearType = GearType.fromEncodingId(byteReader.read().value());

                if (gearType == null) {
                    yield ErrorOr.error("Gear type cannot be decoded.");
                }

                yield ErrorOr.of(new CustomGearTypeData(gearType));
            }
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.CUSTOM_GEAR_TYPE_TRANSFORMER.getId();
    }
}
