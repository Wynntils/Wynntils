/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.models.items.encoding.data.CustomIdentificationsData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.models.stats.type.StatActualValue;
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
    private static final int PERFECT_INTERNAL_ROLL_FLAG = 1;
    private static final int ICON_PREFIX_FLAG = 1 << 1;
    private static final int VANILLA_METER_FLAG = 1 << 2;
    private static final char EMPTY_VANILLA_METER = '\uE000';
    private static final char FULL_VANILLA_METER = '\uE023';

    @Override
    protected ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, CustomIdentificationsData data) {
        return switch (version) {
            case VERSION_1, VERSION_2 -> encodeCustomIdentificationData(data);
            case VERSION_3 -> encodeCustomIdentificationDataV3(data);
        };
    }

    @Override
    public ErrorOr<CustomIdentificationsData> decodeData(
            ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1, VERSION_2 -> decodeCustomIdentificationData(byteReader);
            case VERSION_3 -> decodeCustomIdentificationDataV3(byteReader);
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

            // For positive stats on crafted items, the max values can be used to calculate the minimum values (10%
            // of the maximum, rounded). Negative stats do not decay so are always the maximum.
            RangedValue range = maxValue <= 0
                    ? RangedValue.of(maxValue, maxValue)
                    : RangedValue.of(Math.round(maxValue * 0.1f), maxValue);
            possibleValues.add(new StatPossibleValues(statType, range, maxValue, false));
        }

        return ErrorOr.of(new CustomIdentificationsData(possibleValues));
    }

    private ErrorOr<UnsignedByte[]> encodeCustomIdentificationDataV3(CustomIdentificationsData data) {
        if (data.identifications().size() > 255) {
            return ErrorOr.error("Cannot encode more than 255 crafted identifications!");
        }

        List<UnsignedByte> bytes = new ArrayList<>();
        bytes.add(UnsignedByte.of((byte) data.identifications().size()));

        for (StatActualValue identification : data.identifications()) {
            Optional<Integer> idOpt = Models.Stat.getIdForStatType(identification.statType());
            if (idOpt.isEmpty()) {
                WynntilsMod.warn(
                        "No ID found for stat type " + identification.statType().getApiName());
                return ErrorOr.error("Unable to encode stat type: "
                        + identification.statType().getDisplayName());
            }

            bytes.add(UnsignedByte.of((byte) idOpt.get().intValue()));
            bytes.addAll(List.of(UnsignedByteUtils.encodeVariableSizedInteger(identification.value())));

            int flags = 0;
            if (identification.perfectInternalRoll()) flags |= PERFECT_INTERNAL_ROLL_FLAG;
            if (identification.hasIconPrefix()) flags |= ICON_PREFIX_FLAG;
            if (identification.vanillaMeter().isPresent()) flags |= VANILLA_METER_FLAG;
            bytes.add(UnsignedByte.of((byte) flags));

            if (identification.vanillaMeter().isPresent()) {
                char vanillaMeter = identification.vanillaMeter().get();
                if (vanillaMeter < EMPTY_VANILLA_METER || vanillaMeter > FULL_VANILLA_METER) {
                    return ErrorOr.error("Invalid vanilla identification meter: " + vanillaMeter);
                }
                bytes.add(UnsignedByte.of((byte) (vanillaMeter - EMPTY_VANILLA_METER)));
            }
        }

        return ErrorOr.of(bytes.toArray(new UnsignedByte[0]));
    }

    private ErrorOr<CustomIdentificationsData> decodeCustomIdentificationDataV3(ArrayReader<UnsignedByte> byteReader) {
        int identificationCount = byteReader.read().value();
        List<StatActualValue> identifications = new ArrayList<>();

        for (int i = 0; i < identificationCount; i++) {
            int id = byteReader.read().value();
            Optional<StatType> statTypeOpt = Models.Stat.getStatTypeForId(id);
            if (statTypeOpt.isEmpty()) {
                WynntilsMod.warn("No stat found for id " + id);
                return ErrorOr.error("Unable to decode stat with id " + id);
            }

            int value = (int) UnsignedByteUtils.decodeVariableSizedInteger(byteReader);
            int flags = byteReader.read().value();
            Optional<Character> vanillaMeter = Optional.empty();
            if ((flags & VANILLA_METER_FLAG) != 0) {
                int meterOffset = byteReader.read().value();
                if (meterOffset > FULL_VANILLA_METER - EMPTY_VANILLA_METER) {
                    return ErrorOr.error("Invalid vanilla identification meter offset: " + meterOffset);
                }
                vanillaMeter = Optional.of((char) (EMPTY_VANILLA_METER + meterOffset));
            }

            identifications.add(new StatActualValue(
                    statTypeOpt.get(),
                    value,
                    (flags & PERFECT_INTERNAL_ROLL_FLAG) != 0,
                    RangedValue.NONE,
                    (flags & ICON_PREFIX_FLAG) != 0,
                    vanillaMeter));
        }

        return ErrorOr.of(new CustomIdentificationsData(List.of(), identifications));
    }
}
