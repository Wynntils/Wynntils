/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.items.encoding.data.PowderData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.UnsignedByteUtils;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.UnsignedByte;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PowderDataTransformer extends DataTransformer<PowderData> {
    @Override
    public ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, PowderData data) {
        return switch (version) {
            case VERSION_1 -> encodePowderData(data);
        };
    }

    @Override
    protected boolean shouldEncodeData(ItemTransformingVersion version, PowderData data) {
        return !data.powders().isEmpty() || data.powderSlots() > 0;
    }

    @Override
    public ErrorOr<PowderData> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1 -> decodePowderData(byteReader);
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.POWDER_DATA_TRANSFORMER.getId();
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

        // Pad the data to the nearest byte with '0' bits
        // (the array is initialized with '0' bits, but we do this for clarity)
        for (int i = bitsNeeded; i < totalBits; i++) {
            powderData[i] = false;
        }

        UnsignedByte[] dataBytes = UnsignedByteUtils.fromBitArray(powderData);
        if (data.powders().size() > 255) {
            return ErrorOr.error("Too many powders on item.");
        }
        if (data.powderSlots() > 255) {
            return ErrorOr.error("Too many powder slots on item.");
        }

        // The first byte is the powder slots on the item
        // The second byte is the number of powders
        return ErrorOr.of(Stream.concat(
                        Stream.of(UnsignedByte.of((byte) data.powderSlots()), UnsignedByte.of((byte)
                                data.powders().size())),
                        Stream.of(dataBytes))
                .toArray(UnsignedByte[]::new));
    }

    private ErrorOr<PowderData> decodePowderData(ArrayReader<UnsignedByte> byteReader) {
        // The first byte is the powder slots on the item
        int powderSlots = byteReader.read().value();

        // The second byte is the number of powders
        int powderCount = byteReader.read().value();

        // The powder data is encoded as bits, a powder needs 5 bits to encode
        // That means the total size is 5 * powderCount,
        // which is padded to the nearest byte
        int bitsNeeded = powderCount * 5;

        // Pad to nearest byte
        int totalBits = (bitsNeeded + 7) / 8 * 8;

        // The remaining bytes are the powder data
        UnsignedByte[] powderData = byteReader.read(totalBits / 8);

        // Convert the powder data to a bit array
        boolean[] powderBits = UnsignedByteUtils.toBitArray(powderData);

        // Remove the padding
        boolean[] powderDataBits = new boolean[bitsNeeded];
        System.arraycopy(powderBits, 0, powderDataBits, 0, bitsNeeded);

        // Decode the powder data
        List<Pair<Powder, Integer>> data = new ArrayList<>();
        for (int i = 0; i < powderCount; i++) {
            int powderDataIndex = i * 5;

            // Read the 5 bits for the powder
            int powderValue = 0;
            for (int j = 0; j < 5; j++) {
                // The index is reversed because the bits are stored in reverse order
                int index = powderDataIndex + (4 - j);
                boolean value = powderDataBits[index];
                if (value) {
                    powderValue |= 1 << j;
                }
            }

            // Decode the powder value
            int element = powderValue / 6;
            int tier = powderValue % 6;

            // Add the powder to the data
            data.add(new Pair<>(Powder.values()[element - 1], tier));
        }

        return ErrorOr.of(new PowderData(powderSlots, data));
    }
}
