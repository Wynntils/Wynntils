/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.items.encoding.data.MountEnergyData;
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

public class MountEnergyDataTransformer extends DataTransformer<MountEnergyData> {
    @Override
    protected ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, MountEnergyData data) {
        return switch (version) {
            case VERSION_1, VERSION_2, VERSION_3 -> encodeEnergyData(data);
        };
    }

    @Override
    public ErrorOr<MountEnergyData> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1, VERSION_2, VERSION_3 -> decodeEnergyData(byteReader);
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.MOUNT_ENERGY_DATA_TRANSFORMER.getId();
    }

    private ErrorOr<UnsignedByte[]> encodeEnergyData(MountEnergyData data) {
        List<UnsignedByte> bytes = new ArrayList<>();

        // The first bytes are the current energy bytes, which are assembled into an integer.
        int current = data.energy().current();
        if (current < 0) {
            return ErrorOr.error("Mount current energy cannot be negative: " + current);
        }
        bytes.addAll(List.of(UnsignedByteUtils.encodeVariableSizedInteger(current)));

        // The next bytes are the maximum energy bytes, which are assembled into an integer.
        int max = data.energy().max();
        if (max < 0) {
            return ErrorOr.error("Mount maximum energy cannot be negative: " + max);
        }
        bytes.addAll(List.of(UnsignedByteUtils.encodeVariableSizedInteger(max)));

        return ErrorOr.of(bytes.toArray(new UnsignedByte[0]));
    }

    private ErrorOr<MountEnergyData> decodeEnergyData(ArrayReader<UnsignedByte> byteReader) {
        // The first bytes are the current energy bytes, which are assembled into an integer.
        int currentEnergy = (int) UnsignedByteUtils.decodeVariableSizedInteger(byteReader);

        // The next bytes are the maximum energy bytes, which are assembled into an integer.
        int maxEnergy = (int) UnsignedByteUtils.decodeVariableSizedInteger(byteReader);

        return ErrorOr.of(new MountEnergyData(new CappedValue(currentEnergy, maxEnergy)));
    }
}
