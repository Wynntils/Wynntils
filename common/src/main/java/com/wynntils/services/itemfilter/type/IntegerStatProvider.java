/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

import com.wynntils.models.items.WynnItem;
import java.util.List;

public abstract class IntegerStatProvider extends ItemStatProvider<Integer> {
    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

    @Override
    public int compare(WynnItem wynnItem1, WynnItem wynnItem2) {
        List<Integer> itemValues1 = this.getValue(wynnItem1);
        List<Integer> itemValues2 = this.getValue(wynnItem2);

        if (itemValues1.isEmpty() && !itemValues2.isEmpty()) return 1;
        if (!itemValues1.isEmpty() && itemValues2.isEmpty()) return -1;
        if (itemValues1.isEmpty() && itemValues2.isEmpty()) return 0;

        return itemValues1.get(0).compareTo(itemValues2.get(0));
    }
}
