/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.gui;

import com.wynntils.model.item.properties.CountedItemProperty;

public class DailyRewardItem extends GuiItem implements CountedItemProperty {
    private final int count;

    public DailyRewardItem(int count) {
        this.count = count;
    }

    @Override
    public int getCount() {
        return count;
    }
}
