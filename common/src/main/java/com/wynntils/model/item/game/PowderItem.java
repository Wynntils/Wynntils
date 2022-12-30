/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.game;

import com.wynntils.model.item.properties.NumberedTierItemProperty;
import com.wynntils.wynn.objects.profiles.PowderProfile;

public class PowderItem extends GameItem implements NumberedTierItemProperty {
    private final PowderProfile powderProfile;

    public PowderItem(PowderProfile powderProfile) {
        this.powderProfile = powderProfile;
    }

    public PowderProfile getPowderProfile() {
        return powderProfile;
    }

    @Override
    public int getTier() {
        return powderProfile.tier();
    }

    @Override
    public String toString() {
        return "PowderItem{" + "powderProfile=" + powderProfile + '}';
    }
}
