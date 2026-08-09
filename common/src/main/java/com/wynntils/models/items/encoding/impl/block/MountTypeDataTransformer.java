/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.items.encoding.data.MountTypeData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.models.mount.type.MountType;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;

public class MountTypeDataTransformer extends DataTransformer<MountTypeData> {
    @Override
    protected ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, MountTypeData data) {
        return switch (version) {
            case VERSION_1, VERSION_2, VERSION_3 -> {
                if (data.mountType().getEncodingId() == -1) {
                    yield ErrorOr.error("Mount type cannot be encoded.");
                }

                yield ErrorOr.of(new UnsignedByte[] {
                    new UnsignedByte((byte) data.mountType().getEncodingId())
                });
            }
        };
    }

    @Override
    public ErrorOr<MountTypeData> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1, VERSION_2, VERSION_3 -> {
                MountType mountType = MountType.fromEncodingId(byteReader.read().value());

                if (mountType == null) {
                    yield ErrorOr.error("Mount type cannot be decoded.");
                }

                yield ErrorOr.of(new MountTypeData(mountType));
            }
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.MOUNT_TYPE_DATA_TRANSFORMER.getId();
    }
}
