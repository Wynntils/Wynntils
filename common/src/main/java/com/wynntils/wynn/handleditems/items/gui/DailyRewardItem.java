/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.gui;

import com.wynntils.wynn.handleditems.properties.CountedItemProperty;

public class DailyRewardItem extends GuiItem implements CountedItemProperty {
    private final int count;

    public DailyRewardItem(int count) {
        this.count = count;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "DailyRewardItem{" + "count=" + count + '}';
    }
}
