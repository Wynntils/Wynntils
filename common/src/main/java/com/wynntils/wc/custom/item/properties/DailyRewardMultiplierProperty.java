/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item.properties;

import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.utils.objects.CommonColors;
import com.wynntils.wc.custom.item.WynnItemStack;

public class DailyRewardMultiplierProperty extends CustomStackCountProperty {
    public DailyRewardMultiplierProperty(WynnItemStack item) {
        super(item);
        // Multiplier line is always on index 3
        String value = String.valueOf(ItemUtils.getLore(item).get(3).charAt(25));

        this.setCustomStackCount(value, CommonColors.WHITE, FontRenderer.TextShadow.NORMAL);
    }
}
