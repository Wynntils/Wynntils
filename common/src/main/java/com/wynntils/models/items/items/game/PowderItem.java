/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.elements.type.PowderTierInfo;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.models.items.properties.NumberedTierItemProperty;
import com.wynntils.utils.MathUtils;

public class PowderItem extends GameItem implements NamedItemProperty, NumberedTierItemProperty {
    private final PowderTierInfo powderTierInfo;

    public PowderItem(PowderTierInfo powderTierInfo) {
        this.powderTierInfo = powderTierInfo;
    }

    public PowderTierInfo getPowderProfile() {
        return powderTierInfo;
    }

    @Override
    public int getTier() {
        return powderTierInfo.tier();
    }

    @Override
    public String getName() {
        return powderTierInfo.element().getSymbol() + " "
                + powderTierInfo.element().getName() + " Powder " + MathUtils.toRoman(powderTierInfo.tier());
    }

    @Override
    public String toString() {
        return "PowderItem{" + "powderTierInfo=" + powderTierInfo + '}';
    }
}
