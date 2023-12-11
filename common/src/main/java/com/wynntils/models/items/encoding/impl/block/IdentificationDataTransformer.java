/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.models.items.encoding.data.IdentificationData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.utils.UnsignedByteUtils;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IdentificationDataTransformer extends DataTransformer<IdentificationData> {
    private static final boolean ENCODE_EXTENDED_DATA = true;

    public static final byte ID = 3;

    @Override
    public ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, IdentificationData data) {
        return switch (version) {
            case VERSION_1 -> encodeIdentifications(data);
        };
    }

    @Override
    protected boolean shouldEncodeData(ItemTransformingVersion version, IdentificationData data) {
        return switch (version) {
            case VERSION_1 -> {
                if (ENCODE_EXTENDED_DATA) {
                    yield !data.identifications().isEmpty();
                } else {
                    yield data.identifications().stream().anyMatch(stat -> {
                        StatPossibleValues possibleValues =
                                data.possibleValues().get(stat.statType());
                        return possibleValues == null || !possibleValues.isPreIdentified();
                    });
                }
            }
        };
    }

    @Override
    protected byte getId() {
        return ID;
    }

    private ErrorOr<UnsignedByte[]> encodeIdentifications(IdentificationData data) {
        List<UnsignedByte> bytes = new ArrayList<>();

        if (data.identifications().size() > 255) {
            WynntilsMod.warn("Item has more than 255 identifications!");
            return ErrorOr.error("Cannot encode more than 255 identifications!");
        }

        // Only count non-pre-identified stats
        byte encodedSize = (byte) data.identifications().stream()
                .filter(stat -> {
                    StatPossibleValues possibleValues = data.possibleValues().get(stat.statType());
                    return possibleValues == null || !possibleValues.isPreIdentified();
                })
                .count();
        bytes.add(UnsignedByte.of(encodedSize));
        bytes.add(UnsignedByte.of((byte) (ENCODE_EXTENDED_DATA ? 1 : 0)));

        ErrorOr<List<UnsignedByte>> errorOrData;
        errorOrData = encodeIdentifications(data, bytes, ENCODE_EXTENDED_DATA);

        if (errorOrData.hasError()) {
            return ErrorOr.error(errorOrData.getError());
        }

        return ErrorOr.of(bytes.toArray(new UnsignedByte[0]));
    }

    private ErrorOr<List<UnsignedByte>> encodeIdentifications(
            IdentificationData data, List<UnsignedByte> bytes, boolean encodeExtendedData) {
        // Encoding simple data:
        // Encoding an identification:
        // Each identification takes 2 bytes to encode.
        // The first byte is the numerical key of the ID.
        // The second byte is the calculated internal roll of the item.
        // Pre-identified stats are not encoded.

        // Encoding extended data:
        // The first byte is the number of pre-identified stats.
        // Encoding an identification:
        // The first byte is the numerical key of the ID.
        // The second byte is the length of the integer that the base value fits in, in bytes.
        // The following bytes is are assembled into an integer representing the base value of the id, as of sharing.
        // Pre-identified stats do not have an internal roll.

        if (encodeExtendedData) {
            // The first byte is the number of pre-identified stats.
            List<StatActualValue> preIdentifiedStats = data.identifications().stream()
                    .filter(stat -> {
                        StatPossibleValues possibleValues =
                                data.possibleValues().get(stat.statType());
                        return possibleValues != null && possibleValues.isPreIdentified();
                    })
                    .toList();

            bytes.add(UnsignedByte.of((byte) preIdentifiedStats.size()));

            for (StatActualValue identification : preIdentifiedStats) {
                StatPossibleValues possibleValues = data.possibleValues().get(identification.statType());
                // We know that possibleValues is not null, as we filtered out all stats above

                Optional<Integer> idOpt = Models.Stat.getIdForStatType(identification.statType());
                if (idOpt.isEmpty()) {
                    WynntilsMod.warn("No ID found for stat type "
                            + identification.statType().getApiName());
                    return ErrorOr.error("Unable to encode stat type: "
                            + identification.statType().getDisplayName());
                }

                int id = idOpt.get();

                // The first byte is the numerical key of the ID.
                bytes.add(UnsignedByte.of((byte) id));

                // The second byte is the length of the integer that the base value fits in, in bytes.
                // The base value is the value of the stat as of sharing.
                int baseValue = possibleValues.baseValue();
                UnsignedByte[] baseValueBytes = UnsignedByteUtils.encodeVariableSizedInteger(baseValue);

                // The second byte is the length of the integer that the base value fits in, in bytes.
                bytes.add(UnsignedByte.of((byte) baseValueBytes.length));
                // The following bytes is are assembled into an integer,
                // representing the base value of the id, as of sharing.
                bytes.addAll(List.of(baseValueBytes));
            }
        }

        for (StatActualValue identification : data.identifications()) {
            StatPossibleValues possibleValues = data.possibleValues().get(identification.statType());
            if (possibleValues == null) {
                WynntilsMod.warn("No possible values found for stat type "
                        + identification.statType().getApiName());
                return ErrorOr.error("Unable to encode stat type, no possible values for id: "
                        + identification.statType().getDisplayName());
            }

            // Pre-identified stats are not encoded, when not encoding extended data.
            // When encoding extended data, pre-identified stats are encoded above.
            if (possibleValues.isPreIdentified()) {
                continue;
            }

            Optional<Integer> idOpt = Models.Stat.getIdForStatType(identification.statType());
            if (idOpt.isEmpty()) {
                WynntilsMod.warn(
                        "No ID found for stat type " + identification.statType().getApiName());
                return ErrorOr.error("Unable to encode stat type: "
                        + identification.statType().getDisplayName());
            }

            int id = idOpt.get();

            // The first byte is the numerical key of the ID.
            bytes.add(UnsignedByte.of((byte) id));

            if (encodeExtendedData) {
                // The second byte is the length of the integer that the base value fits in, in bytes.
                // The base value is the value of the stat as of sharing.
                int baseValue = possibleValues.baseValue();
                UnsignedByte[] baseValueBytes = UnsignedByteUtils.encodeVariableSizedInteger(baseValue);

                // The second byte is the length of the integer that the base value fits in, in bytes.
                bytes.add(UnsignedByte.of((byte) baseValueBytes.length));
                // The following bytes is are assembled into an integer,
                // representing the base value of the id, as of sharing.
                bytes.addAll(List.of(baseValueBytes));
            }

            int internalRoll = identification.internalRoll().low();
            UnsignedByte internalRollByte = UnsignedByte.of((byte) internalRoll);

            // Check if the internal roll fits a byte.
            if (internalRoll != internalRollByte.value()) {
                WynntilsMod.warn("Internal roll " + internalRoll + " does not fit a byte!");
                return ErrorOr.error("Unable to encode stat type, invalid internal roll: "
                        + identification.statType().getDisplayName());
            }

            // The last byte is the calculated internal roll of the item.
            bytes.add(internalRollByte);
        }

        return ErrorOr.of(bytes);
    }
}
