/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.core.components.Models;
import com.wynntils.models.items.encoding.data.MountColorData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;

public class MountColorDataTransformer extends DataTransformer<MountColorData> {
    @Override
    public ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, MountColorData data) {
        return switch (version) {
            case VERSION_1, VERSION_2, VERSION_3 -> encodeColorData(data);
        };
    }

    @Override
    public ErrorOr<MountColorData> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1, VERSION_2, VERSION_3 -> decodeColorData(byteReader);
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.MOUNT_COLOR_DATA_TRANSFORMER.getId();
    }

    private static ErrorOr<UnsignedByte[]> encodeColorData(MountColorData data) {
        UnsignedByte[] bytes = new UnsignedByte[2];

        // The first byte is the id of the primary mount color.
        bytes[0] = UnsignedByte.of((byte) data.primaryColorInfo().id());

        // The second byte is the id of the secondary mount color.
        bytes[1] = UnsignedByte.of((byte) data.secondaryColorInfo().id());

        return ErrorOr.of(bytes);
    }

    private static ErrorOr<MountColorData> decodeColorData(ArrayReader<UnsignedByte> byteReader) {
        // The first byte is the id of the primary mount color.
        UnsignedByte primaryColorId = byteReader.read();

        // The second byte is the id of the secondary mount color.
        UnsignedByte secondaryColorId = byteReader.read();

        return ErrorOr.of(new MountColorData(
                Models.Mount.getMountColor(primaryColorId.value()),
                Models.Mount.getMountColor(secondaryColorId.value())));
    }
}
