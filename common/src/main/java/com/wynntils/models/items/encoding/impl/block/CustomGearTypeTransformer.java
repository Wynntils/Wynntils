/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.encoding.data.CustomGearTypeData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.UnsignedByte;
import java.util.List;

public class CustomGearTypeTransformer extends DataTransformer<CustomGearTypeData> {
    private static final List<Pair<GearType, Integer>> GEAR_TYPE_IDS = List.of(
            new Pair<>(GearType.SPEAR, 0),
            new Pair<>(GearType.WAND, 1),
            new Pair<>(GearType.DAGGER, 2),
            new Pair<>(GearType.BOW, 3),
            new Pair<>(GearType.RELIK, 4),
            new Pair<>(GearType.RING, 5),
            new Pair<>(GearType.BRACELET, 6),
            new Pair<>(GearType.NECKLACE, 7),
            new Pair<>(GearType.HELMET, 8),
            new Pair<>(GearType.CHESTPLATE, 9),
            new Pair<>(GearType.LEGGINGS, 10),
            new Pair<>(GearType.BOOTS, 11));

    @Override
    protected ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, CustomGearTypeData data) {
        return switch (version) {
            case VERSION_1 -> {
                for (Pair<GearType, Integer> gearType : GEAR_TYPE_IDS) {
                    if (gearType.a() == data.gearType()) {
                        int gearTypeId = gearType.value();
                        yield ErrorOr.of(new UnsignedByte[] {UnsignedByte.of((byte) gearTypeId)});
                    }
                }
                yield ErrorOr.error("Cannot map gear type to ID: " + data.gearType());
            }
        };
    }

    @Override
    public ErrorOr<CustomGearTypeData> decodeData(
            ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1 -> {
                int gearTypeId = byteReader.read().value();
                for (Pair<GearType, Integer> gearType : GEAR_TYPE_IDS) {
                    if (gearType.value() == gearTypeId) {
                        yield ErrorOr.of(new CustomGearTypeData(gearType.a()));
                    }
                }
                yield ErrorOr.error("Cannot map gear type ID to gear type: " + gearTypeId);
            }
        };
    }

    @Override
    public byte getId() {
        return 7;
    }
}
