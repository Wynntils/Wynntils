/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.item;

import com.wynntils.core.components.Models;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.items.encoding.data.IdentificationData;
import com.wynntils.models.items.encoding.data.NameData;
import com.wynntils.models.items.encoding.data.PowderData;
import com.wynntils.models.items.encoding.data.RerollData;
import com.wynntils.models.items.encoding.data.ShinyData;
import com.wynntils.models.items.encoding.type.EncodingSettings;
import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.encoding.type.ItemDataMap;
import com.wynntils.models.items.encoding.type.ItemTransformer;
import com.wynntils.models.items.encoding.type.ItemType;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.ErrorOr;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GearItemTransformer extends ItemTransformer<GearItem> {
    @Override
    public ErrorOr<GearItem> decodeItem(ItemDataMap itemDataMap) {
        NameData nameData = itemDataMap.get(NameData.class);
        if (nameData == null) {
            return ErrorOr.error("Gear item does not have name data!");
        }

        GearInfo gearInfo =
                Models.Gear.getGearInfoFromDisplayName(nameData.name().orElse(""));
        if (gearInfo == null) {
            return ErrorOr.error("Unknown gear item: " + nameData.name());
        }

        IdentificationData identificationData = itemDataMap.get(IdentificationData.class);
        ErrorOr<Map<StatType, StatActualValue>> errorOrIdentifications =
                processIdentifications(identificationData, gearInfo.getPossibleValueList());
        if (errorOrIdentifications.hasError()) {
            return ErrorOr.error(errorOrIdentifications.getError());
        }
        Map<StatType, StatActualValue> identifications = errorOrIdentifications.getValue();

        PowderData powderData = itemDataMap.get(PowderData.class);

        int rerolls = 0;
        RerollData rerollData = itemDataMap.get(RerollData.class);
        if (rerollData != null) {
            rerolls = rerollData.rerolls();
        }

        Optional<ShinyStat> shinyStat = Optional.empty();
        ShinyData shinyData = itemDataMap.get(ShinyData.class);
        if (shinyData != null) {
            shinyStat = Optional.of(shinyData.shinyStat());
        }

        List<StatActualValue> idList = identifications.values().stream().toList();

        GearInstance instance = GearInstance.create(gearInfo, idList, rerolls);
        GearItem item = new GearItem(gearInfo, instance, shinyStat, 0);
        if (powderData != null) {
            item.setPowders(powderData.powders());
            item.setPowderSpecial(powderData.powderSpecial());
        }
        return ErrorOr.of(item);
    }

    @Override
    public List<ItemData> encodeItem(GearItem item, EncodingSettings encodingSettings) {
        List<ItemData> dataList = new ArrayList<>();

        dataList.add(NameData.fromSafeName(item));
        dataList.add(IdentificationData.from(item, encodingSettings.extendedIdentificationEncoding()));
        dataList.add(PowderData.from(item));
        dataList.add(RerollData.from(item));
        dataList.add(ShinyData.from(item));

        return dataList;
    }

    @Override
    public ItemType getType() {
        return ItemType.GEAR;
    }
}
