/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.item;

import com.wynntils.models.items.encoding.data.IdentificationData;
import com.wynntils.models.items.encoding.data.NameData;
import com.wynntils.models.items.encoding.data.PowderData;
import com.wynntils.models.items.encoding.data.RerollData;
import com.wynntils.models.items.encoding.data.ShinyData;
import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.encoding.type.ItemTransformer;
import com.wynntils.models.items.encoding.type.ItemType;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.utils.type.ErrorOr;
import java.util.ArrayList;
import java.util.List;

public class GearItemTransformer extends ItemTransformer<GearItem> {
    @Override
    public ErrorOr<GearItem> decodeItem(List<ItemData> itemData) {
        return null;
    }

    @Override
    public List<ItemData> encodeItem(GearItem item) {
        List<ItemData> dataList = new ArrayList<>();

        dataList.add(NameData.from(item));
        dataList.add(IdentificationData.from(item));
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
