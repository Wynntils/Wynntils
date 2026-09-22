/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.items.encoding.data.MountPotentialData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.UnsignedByteUtils;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;

public class MountPotentialDataTransformer extends DataTransformer<MountPotentialData> {
    @Override
    protected ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, MountPotentialData data) {
        return switch (version) {
            case VERSION_1, VERSION_2, VERSION_3 -> encodeMountPotentialData(data);
        };
    }

    private ErrorOr<UnsignedByte[]> encodeMountPotentialData(MountPotentialData data) {
        if (data.potential() < 0) {
            return ErrorOr.error("Mount potential cannot be negative: " + data.potential());
        }

        return ErrorOr.of(UnsignedByteUtils.encodeVariableSizedInteger(data.potential()));
    }

    @Override
    public ErrorOr<MountPotentialData> decodeData(
            ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1, VERSION_2, VERSION_3 ->
                ErrorOr.of(new MountPotentialData((int) UnsignedByteUtils.decodeVariableSizedInteger(byteReader)));
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.MOUNT_POTENTIAL_DATA_TRANSFORMER.getId();
    }
}
