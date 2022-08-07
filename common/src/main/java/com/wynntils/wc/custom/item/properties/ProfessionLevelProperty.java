/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item.properties;

import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.utils.objects.CommonColors;
import com.wynntils.wc.custom.item.WynnItemStack;

public class ProfessionLevelProperty extends CustomStackCountProperty {
    public ProfessionLevelProperty(WynnItemStack item) {
        super(item);

        try {
            // Level is always at line index 2.
            String levelLine = ItemUtils.getLore(item).get(2);
            String value = levelLine.substring(levelLine.indexOf('f') + 1);

            setCustomStackCount(value, CommonColors.WHITE, FontRenderer.TextShadow.NORMAL);
        } catch (IndexOutOfBoundsException ignored) {
        }
    }
}
