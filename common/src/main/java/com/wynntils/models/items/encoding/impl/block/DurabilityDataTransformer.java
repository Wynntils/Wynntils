/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.items.encoding.data.DurabilityData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.UnsignedByteUtils;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;
import java.util.ArrayList;
import java.util.List;

public class DurabilityDataTransformer extends DataTransformer<DurabilityData> {
    @Override
    protected ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, DurabilityData data) {
        return switch (version) {
            case VERSION_1, VERSION_2 -> encodeDurabilityData(data);
        };
    }

    @Override
    public ErrorOr<DurabilityData> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1, VERSION_2 -> decodeDurabilityData(byteReader);
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.DURABILITY_DATA_TRANSFORMER.getId();
    }

    private ErrorOr<UnsignedByte[]> encodeDurabilityData(DurabilityData data) {
        List<UnsignedByte> bytes = new ArrayList<>();

        // The first byte is the overall effectiveness of the identifications (the percentage next to the name for
        // crafted items).
        if (data.effectStrength() > 100 || data.effectStrength() < 0) {
            return ErrorOr.error("Effect strength was not a percentage.");
        }
        bytes.add(UnsignedByte.of((byte) data.effectStrength()));

        // The next bytes are the maximum durability bytes, which are assembled into an integer.
        int max = data.durability().max();
        UnsignedByte[] unsignedBytes = UnsignedByteUtils.encodeVariableSizedInteger(max);
        bytes.addAll(List.of(unsignedBytes));

        // The next bytes are the current durability bytes, which are assembled into an integer.
        int current = data.durability().current();
        unsignedBytes = UnsignedByteUtils.encodeVariableSizedInteger(current);
        bytes.addAll(List.of(unsignedBytes));

        return ErrorOr.of(bytes.toArray(new UnsignedByte[0]));
    }

    private ErrorOr<DurabilityData> decodeDurabilityData(ArrayReader<UnsignedByte> byteReader) {
        // The first byte is the overall effectiveness of the identifications (the percentage next to the name for
        // crafted items).
        int effectStrength = byteReader.read().value();

        // The next bytes are the maximum durability bytes, which are assembled into an integer.
        int max = (int) UnsignedByteUtils.decodeVariableSizedInteger(byteReader);

        // The next bytes are the current durability bytes, which are assembled into an integer.
        int current = (int) UnsignedByteUtils.decodeVariableSizedInteger(byteReader);

        return ErrorOr.of(new DurabilityData(effectStrength, new CappedValue(current, max)));
    }
}
