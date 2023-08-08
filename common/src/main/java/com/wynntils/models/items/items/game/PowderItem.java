/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.elements.type.PowderTierInfo;
import com.wynntils.models.items.properties.NumberedTierItemProperty;

public class PowderItem extends GameItem implements NumberedTierItemProperty {
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
    public String toString() {
        return "PowderItem{" + "powderTierInfo=" + powderTierInfo + '}';
    }
}
