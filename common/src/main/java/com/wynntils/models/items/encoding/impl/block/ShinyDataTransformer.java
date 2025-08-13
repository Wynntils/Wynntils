/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.core.components.Models;
import com.wynntils.models.items.encoding.data.ShinyData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.utils.UnsignedByteUtils;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;

public class ShinyDataTransformer extends DataTransformer<ShinyData> {
    @Override
    public ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, ShinyData data) {
        return switch (version) {
            case VERSION_1 -> ErrorOr.of(encodeShinyData(data));
            case VERSION_2 -> ErrorOr.of(encodeShinyDataV2(data));
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
            case VERSION_2 -> decodeShinyDataV2(byteReader);
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.SHINY_DATA_TRANSFORMER.getId();
    }

    private static UnsignedByte[] encodeShinyDataV2(ShinyData data) {
        UnsignedByte[] shinyStatValueBytes =
                UnsignedByteUtils.encodeVariableSizedInteger(data.shinyStat().value());

        UnsignedByte[] bytes = new UnsignedByte[shinyStatValueBytes.length + 2];

        // The first byte is the id of the shiny stat.
        bytes[0] = UnsignedByte.of((byte) data.shinyStat().statType().id());

        // The second byte is the shiny reroll count.
        bytes[1] = UnsignedByte.of((byte) data.shinyStat().shinyRerolls());

        // The following bytes is are assembled into an integer representing the shiny value.
        System.arraycopy(shinyStatValueBytes, 0, bytes, 2, shinyStatValueBytes.length);

        return bytes;
    }

    private static ErrorOr<ShinyData> decodeShinyDataV2(ArrayReader<UnsignedByte> byteReader) {
        // The first byte is the id of the shiny stat.
        UnsignedByte statTypeId = byteReader.read();

        // The second byte is the shiny reroll count.
        UnsignedByte shinyRerolls = byteReader.read();

        // The following bytes is are assembled into an integer representing the shiny value.
        long statValue = UnsignedByteUtils.decodeVariableSizedInteger(byteReader);

        // Note: V1 encoding does not support shiny rerolls, so we default to 0.
        return ErrorOr.of(new ShinyData(
                new ShinyStat(Models.Shiny.getShinyStatType(statTypeId.value()), statValue, shinyRerolls.value())));
    }

    private static UnsignedByte[] encodeShinyData(ShinyData data) {
        UnsignedByte[] shinyStatValueBytes =
                UnsignedByteUtils.encodeVariableSizedInteger(data.shinyStat().value());

        UnsignedByte[] bytes = new UnsignedByte[shinyStatValueBytes.length + 1];

        // The first byte is the id of the shiny stat.
        bytes[0] = UnsignedByte.of((byte) data.shinyStat().statType().id());

        // The following bytes is are assembled into an integer representing the shiny value.
        System.arraycopy(shinyStatValueBytes, 0, bytes, 1, shinyStatValueBytes.length);

        return bytes;
    }

    private ErrorOr<ShinyData> decodeShinyData(ArrayReader<UnsignedByte> byteReader) {
        // The first byte is the id of the shiny stat.
        UnsignedByte statTypeId = byteReader.read();

        // The following bytes is are assembled into an integer representing the shiny value.
        long statValue = UnsignedByteUtils.decodeVariableSizedInteger(byteReader);

        // Note: V1 encoding does not support shiny rerolls, so we default to 0.
        return ErrorOr.of(
                new ShinyData(new ShinyStat(Models.Shiny.getShinyStatType(statTypeId.value()), statValue, 0)));
    }
}
