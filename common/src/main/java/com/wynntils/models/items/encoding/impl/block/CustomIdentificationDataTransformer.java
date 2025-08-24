/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.models.items.encoding.data.CustomIdentificationsData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.UnsignedByteUtils;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.RangedValue;
import com.wynntils.utils.type.UnsignedByte;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomIdentificationDataTransformer extends DataTransformer<CustomIdentificationsData> {
    @Override
    protected ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, CustomIdentificationsData data) {
        return switch (version) {
            case VERSION_1, VERSION_2 -> encodeCustomIdentificationData(data);
        };
    }

    @Override
    public ErrorOr<CustomIdentificationsData> decodeData(
            ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1, VERSION_2 -> decodeCustomIdentificationData(byteReader);
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.CUSTOM_IDENTIFICATION_DATA_TRANSFORMER.getId();
    }

    private ErrorOr<UnsignedByte[]> encodeCustomIdentificationData(CustomIdentificationsData data) {
        List<UnsignedByte> bytes = new ArrayList<>();

        // The first byte is the number of identifications.
        bytes.add(UnsignedByte.of((byte) data.possibleValues().size()));

        // The identifications are encoded the following way:
        for (StatPossibleValues statPossibleValues : data.possibleValues()) {
            // The first byte is the id of the identification.
            Optional<Integer> idOpt = Models.Stat.getIdForStatType(statPossibleValues.statType());
            if (idOpt.isEmpty()) {
                WynntilsMod.warn("No ID found for stat type "
                        + statPossibleValues.statType().getApiName());
                return ErrorOr.error("Unable to encode stat type: "
                        + statPossibleValues.statType().getDisplayName());
            }
            int id = idOpt.get();
            bytes.add(UnsignedByte.of((byte) id));

            // The next bytes are the identification's max value bytes, which are assembled into an integer.
            UnsignedByte[] unsignedBytes = UnsignedByteUtils.encodeVariableSizedInteger(
                    statPossibleValues.range().high());
            bytes.addAll(List.of(unsignedBytes));
        }

        return ErrorOr.of(bytes.toArray(new UnsignedByte[0]));
    }

    private ErrorOr<CustomIdentificationsData> decodeCustomIdentificationData(ArrayReader<UnsignedByte> byteReader) {
        // The first byte is the number of identifications.
        int numIdentifications = byteReader.read().value();
        List<StatPossibleValues> possibleValues = new ArrayList<>();

        for (int i = 0; i < numIdentifications; i++) {
            // The first byte is the id of the identification.
            int id = byteReader.read().value();
            Optional<StatType> statTypeOpt = Models.Stat.getStatTypeForId(id);
            if (statTypeOpt.isEmpty()) {
                WynntilsMod.warn("No stat found for id " + id);
                return ErrorOr.error("Unable to decode stat with id " + id);
            }
            StatType statType = statTypeOpt.get();

            // The next bytes are the identification's max value bytes, which are assembled into an integer.
            int maxValue = (int) UnsignedByteUtils.decodeVariableSizedInteger(byteReader);

            //  For crafted items, the max values can be used to calculate the minimum values (10% of the maximum,
            // rounded).
            possibleValues.add(new StatPossibleValues(
                    statType, RangedValue.of(Math.round(maxValue * 0.1f), maxValue), maxValue, false));
        }

        return ErrorOr.of(new CustomIdentificationsData(possibleValues));
    }
}
