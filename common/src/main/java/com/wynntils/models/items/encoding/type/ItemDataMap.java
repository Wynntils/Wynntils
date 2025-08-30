/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.type;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ItemDataMap {
    private final Map<Class<? extends ItemData>, ItemData> dataMap;

    public ItemDataMap(List<ItemData> data) {
        this.dataMap = data.stream().collect(Collectors.toMap(ItemData::getClass, Function.identity()));
    }

    public <T extends ItemData> T get(Class<T> clazz) {
        return (T) dataMap.get(clazz);
    }
}
