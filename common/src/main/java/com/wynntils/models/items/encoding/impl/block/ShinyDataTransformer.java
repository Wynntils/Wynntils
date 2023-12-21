/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.core.components.Models;
import com.wynntils.models.items.encoding.data.ShinyData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.utils.UnsignedByteUtils;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;

public class ShinyDataTransformer extends DataTransformer<ShinyData> {
    public static final byte ID = 6;

    @Override
    public ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, ShinyData data) {
        return switch (version) {
            case VERSION_1 -> ErrorOr.of(encodeShinyData(data));
        };
    }

    @Override
    protected boolean shouldEncodeData(ItemTransformingVersion version, ShinyData data) {
        return data.shinyStat() != null && data.shinyStat().statType() != null;
    }

    @Override
    public ErrorOr<ShinyData> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1 -> decodeShinyData(byteReader);
        };
    }

    @Override
    protected byte getId() {
        return ID;
    }

    private static UnsignedByte[] encodeShinyData(ShinyData data) {
        UnsignedByte[] shinyStatValueBytes =
                UnsignedByteUtils.encodeVariableSizedInteger(data.shinyStat().value());

        UnsignedByte[] bytes = new UnsignedByte[shinyStatValueBytes.length + 2];
        bytes[0] = UnsignedByte.of((byte) data.shinyStat().statType().id());
        bytes[1] = UnsignedByte.of((byte) shinyStatValueBytes.length);
        System.arraycopy(shinyStatValueBytes, 0, bytes, 2, shinyStatValueBytes.length);

        return bytes;
    }

    private ErrorOr<ShinyData> decodeShinyData(ArrayReader<UnsignedByte> byteReader) {
        UnsignedByte statTypeId = byteReader.read();
        UnsignedByte statValueLength = byteReader.read();

        if (statValueLength == null) {
            return ErrorOr.error("Failed to read shiny stat value length");
        }

        long statValue = UnsignedByteUtils.decodeVariableSizedInteger(byteReader.read(statValueLength.value()));

        return ErrorOr.of(new ShinyData(new ShinyStat(Models.Shiny.getShinyStatType(statTypeId.value()), statValue)));
    }
}
