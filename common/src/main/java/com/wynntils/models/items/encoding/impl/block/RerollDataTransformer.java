/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.items.encoding.data.RerollData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;

public class RerollDataTransformer extends DataTransformer<RerollData> {
    @Override
    public ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, RerollData data) {
        return switch (version) {
            case VERSION_1, VERSION_2 ->
                ErrorOr.of(new UnsignedByte[] {
                    UnsignedByte.of((byte) data.rerolls()),
                });
        };
    }

    @Override
    protected boolean shouldEncodeData(ItemTransformingVersion version, RerollData data) {
        return data.rerolls() > 0;
    }

    @Override
    public ErrorOr<RerollData> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1, VERSION_2 ->
                ErrorOr.of(new RerollData(byteReader.read().value()));
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.REROLL_DATA_TRANSFORMER.getId();
    }
}
