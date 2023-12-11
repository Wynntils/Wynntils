/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.items.encoding.data.PowderData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.UnsignedByteUtils;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.UnsignedByte;
import java.util.stream.Stream;

public class PowderDataTransformer extends DataTransformer<PowderData> {
    public static final byte ID = 4;

    @Override
    public ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, PowderData data) {
        return switch (version) {
            case VERSION_1 -> encodePowderData(data);
        };
    }

    @Override
    protected boolean shouldEncodeData(ItemTransformingVersion version, PowderData data) {
        return !data.powders().isEmpty();
    }

    @Override
    protected byte getId() {
        return ID;
    }

    private ErrorOr<UnsignedByte[]> encodePowderData(PowderData data) {
        // Powders are encoded as bits, a powder needs 5 bits to encode
        // That means the total size is 5 * powderCount,
        // which is padded to the nearest byte
        int bitsNeeded = data.powders().size() * 5;
        // Pad to nearest byte
        int totalBits = (bitsNeeded + 7) / 8 * 8;

        boolean[] powderData = new boolean[totalBits];

        // A powder is encoded in 5 bits, with the following math: `element * 6 + tier`.
        // The elements follow an `ETWFA` order.
        // 5 `0` bits are used to represent that no powder is present at the slot.
        for (int i = 0; i < data.powders().size(); i++) {
            Pair<Powder, Integer> powder = data.powders().get(i);
            int element = powder.key().ordinal();
            int tier = powder.value();
            int powderDataIndex = i * 5;

            // Set the 5 bits for the powder
            for (int j = 0; j < 5; j++) {
                // The index is reversed because the bits are stored in reverse order
                int index = powderDataIndex + (4 - j);
                boolean value = ((element * 6 + tier) >> j) % 2 != 0;
                powderData[index] = value;
            }
        }

        // Pad the data to the nearest byte with '1' bits
        for (int i = bitsNeeded; i < totalBits; i++) {
            powderData[i] = true;
        }

        UnsignedByte[] dataBytes = UnsignedByteUtils.fromBitArray(powderData);
        if (data.powders().size() > 255) {
            return ErrorOr.error("Too many powders on item.");
        }

        return ErrorOr.of(
                Stream.concat(Stream.of(UnsignedByte.of((byte) data.powders().size())), Stream.of(dataBytes))
                        .toArray(UnsignedByte[]::new));
    }
}
