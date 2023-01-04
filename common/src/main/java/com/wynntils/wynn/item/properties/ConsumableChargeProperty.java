/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.gui.render.FontRenderer;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.utils.WynnItemMatchers;
import java.util.regex.Matcher;

public class ConsumableChargeProperty extends CustomStackCountProperty {
    public ConsumableChargeProperty(WynnItemStack item) {
        super(item);

        // parse charge
        String charges = "";
        Matcher consumableMatcher = WynnItemMatchers.consumableNameMatcher(item.getHoverName());
        if (consumableMatcher.matches()) {
            charges = consumableMatcher.group(2);
        }

        this.setCustomStackCount(charges, CommonColors.WHITE, FontRenderer.TextShadow.NORMAL);
    }

    @Override
    public boolean isTextOverlayEnabled() {
        return false;
    }
}
