/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.item;

import com.wynntils.core.components.Models;
import com.wynntils.models.items.encoding.data.IdentificationData;
import com.wynntils.models.items.encoding.data.NameData;
import com.wynntils.models.items.encoding.data.RerollData;
import com.wynntils.models.items.encoding.type.EncodingSettings;
import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.encoding.type.ItemDataMap;
import com.wynntils.models.items.encoding.type.ItemTransformer;
import com.wynntils.models.items.encoding.type.ItemType;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.models.rewards.type.TomeInstance;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.ErrorOr;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TomeItemTransformer extends ItemTransformer<TomeItem> {
    @Override
    public ErrorOr<TomeItem> decodeItem(ItemDataMap itemDataMap) {
        NameData nameData = itemDataMap.get(NameData.class);
        if (nameData == null) {
            return ErrorOr.error("Tome item does not have name data!");
        }

        TomeInfo tomeInfo =
                Models.Rewards.getTomeInfoFromDisplayName(nameData.name().orElse(""));
        if (tomeInfo == null) {
            return ErrorOr.error("Unknown tome item: " + nameData.name());
        }

        IdentificationData identificationData = itemDataMap.get(IdentificationData.class);
        ErrorOr<Map<StatType, StatActualValue>> errorOrIdentifications =
                processIdentifications(identificationData, tomeInfo.getPossibleValueList());
        if (errorOrIdentifications.hasError()) {
            return ErrorOr.error(errorOrIdentifications.getError());
        }
        Map<StatType, StatActualValue> identifications = errorOrIdentifications.getValue();

        int rerolls = 0;
        RerollData rerollData = itemDataMap.get(RerollData.class);
        if (rerollData != null) {
            rerolls = rerollData.rerolls();
        }

        List<StatActualValue> idList = identifications.values().stream().toList();
        return ErrorOr.of(new TomeItem(tomeInfo, TomeInstance.create(rerolls, tomeInfo, idList)));
    }

    @Override
    protected List<ItemData> encodeItem(TomeItem item, EncodingSettings encodingSettings) {
        List<ItemData> dataList = new ArrayList<>();

        dataList.add(NameData.fromSafeName(item));
        dataList.add(IdentificationData.from(item, encodingSettings.extendedIdentificationEncoding()));
        dataList.add(RerollData.from(item));

        return dataList;
    }

    @Override
    public ItemType getType() {
        return ItemType.TOME;
    }
}
