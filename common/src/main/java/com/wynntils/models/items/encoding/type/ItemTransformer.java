/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.type;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.encoding.data.TypeData;
import com.wynntils.utils.type.ErrorOr;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface for transforming items into an item data list.
 * @param <T> The type of item to transform.
 */
public abstract class ItemTransformer<T extends WynnItem> {
    public final List<ItemData> encode(T item) {
        List<ItemData> dataList = new ArrayList<>();
        dataList.add(new TypeData(getType()));
        dataList.addAll(encodeItem(item));
        return List.copyOf(dataList);
    }

    public abstract ErrorOr<T> decodeItem(ItemDataMap itemDataMap);

    protected abstract List<ItemData> encodeItem(T item);

    public abstract ItemType getType();
}
