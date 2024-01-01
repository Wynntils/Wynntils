/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.type;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.encoding.data.IdentificationData;
import com.wynntils.models.items.encoding.data.TypeData;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Interface for transforming items into an item data list.
 * @param <T> The type of item to transform.
 */
public abstract class ItemTransformer<T extends WynnItem> {
    public final List<ItemData> encode(T item, EncodingSettings encodingSettings) {
        List<ItemData> dataList = new ArrayList<>();
        dataList.add(new TypeData(getType()));
        dataList.addAll(encodeItem(item, encodingSettings));
        return List.copyOf(dataList);
    }

    public abstract ErrorOr<T> decodeItem(ItemDataMap itemDataMap);

    protected abstract List<ItemData> encodeItem(T item, EncodingSettings encodingSettings);

    public abstract ItemType getType();

    protected ErrorOr<Map<StatType, StatActualValue>> processIdentifications(
            IdentificationData identificationData, List<StatPossibleValues> itemInfoPossibleValues) {
        Map<StatType, StatActualValue> identifications;
        Map<StatType, StatPossibleValues> statPossibleValues;
        if (identificationData != null) {
            statPossibleValues = identificationData.possibleValues();

            // If there are no encoded possible values, use the item info's possible values
            if (statPossibleValues.isEmpty()) {
                statPossibleValues = itemInfoPossibleValues.stream()
                        .collect(Collectors.toMap(StatPossibleValues::statType, Function.identity()));
            }

            // Process the pending calculations, as we know the base values now
            ErrorOr<Void> processResult = identificationData.processPendingCalculations(statPossibleValues);
            if (processResult.hasError()) {
                return ErrorOr.error(processResult.getError());
            }

            identifications = identificationData.identifications().stream()
                    .collect(Collectors.toMap(StatActualValue::statType, Function.identity()));
        } else {
            // If there are no encoded possible values, use the item info's possible values
            statPossibleValues = itemInfoPossibleValues.stream()
                    .collect(Collectors.toMap(StatPossibleValues::statType, Function.identity()));
            identifications = new HashMap<>();
        }

        // Add back all pre-identified values that were not encoded
        // (this may happen if there was no identification data block, if an item is fully pre-identified)
        for (StatPossibleValues possibleValues : statPossibleValues.values()) {
            if (possibleValues.isPreIdentified() && !identifications.containsKey(possibleValues.statType())) {
                identifications.put(
                        possibleValues.statType(),
                        new StatActualValue(
                                possibleValues.statType(), possibleValues.baseValue(), 0, RangedValue.NONE));
            }
        }

        return ErrorOr.of(identifications);
    }
}
