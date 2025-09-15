/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.elements.type.Element;
import com.wynntils.models.items.encoding.data.DefenseData;
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

public class DefenseDataTransformer extends DataTransformer<DefenseData> {
    @Override
    protected ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, DefenseData data) {
        return switch (version) {
            case VERSION_1, VERSION_2 -> encodeDefenseData(data);
        };
    }

    @Override
    public ErrorOr<DefenseData> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1, VERSION_2 -> decodeDefenseData(byteReader);
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.DEFENSE_DATA_TRANSFORMER.getId();
    }

    @Override
    protected boolean shouldEncodeData(ItemTransformingVersion version, DefenseData data) {
        return data.health() != 0 || !data.defences().isEmpty();
    }

    private ErrorOr<UnsignedByte[]> encodeDefenseData(DefenseData data) {
        // The first bytes are the health bytes, which are assembled into an integer.
        UnsignedByte[] unsignedBytes = UnsignedByteUtils.encodeVariableSizedInteger(data.health());
        List<UnsignedByte> bytes = new ArrayList<>(List.of(unsignedBytes));

        // The next byte is the number of defense stats present on the item.
        bytes.add(UnsignedByte.of((byte) data.defences().size()));

        // A defense stat is encoded the following way:
        for (Pair<Element, Integer> defence : data.defences()) {
            // The first byte is the id of the skill (`ETWFA`).
            bytes.add(UnsignedByte.of((byte) defence.a().getEncodingId()));

            // The next bytes are the defense bytes, which are assembled into an integer.
            unsignedBytes = UnsignedByteUtils.encodeVariableSizedInteger(defence.b());
            bytes.addAll(List.of(unsignedBytes));
        }

        return ErrorOr.of(bytes.toArray(new UnsignedByte[0]));
    }

    private ErrorOr<DefenseData> decodeDefenseData(ArrayReader<UnsignedByte> byteReader) {
        // The first bytes are the health bytes, which are assembled into an integer.
        int health = (int) UnsignedByteUtils.decodeVariableSizedInteger(byteReader);

        // The next byte is the number of defense stats present on the item.
        int defencesCount = byteReader.read().value();
        List<Pair<Element, Integer>> defences = new ArrayList<>();

        for (int i = 0; i < defencesCount; i++) {
            // A defense stat is encoded the following way:
            // The first byte is the id of the skill (`ETWFA`).
            int elementTypeId = byteReader.read().value();
            Element element = Element.fromEncodingId(elementTypeId);

            if (element == null) { // Sometimes null when users mess with custom encoding
                return ErrorOr.error("Invalid element encoding: " + elementTypeId);
            }

            // The next bytes are the defense bytes, which are assembled into an integer.
            int defence = (int) UnsignedByteUtils.decodeVariableSizedInteger(byteReader);

            defences.add(Pair.of(element, defence));
        }

        return ErrorOr.of(new DefenseData(health, defences));
    }
}
