/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear;

import com.wynntils.models.gear.profile.GearProfile;
import com.wynntils.models.gear.profile.IdentificationProfile;
import com.wynntils.models.gear.type.IdentificationModifier;
import net.minecraft.network.chat.Component;

public record GearIdentificationContainer(
        String inGameIdName,
        GearProfile gearProfile,
        IdentificationProfile idProfile,
        IdentificationModifier modifier,
        String shortIdName,
        int value,
        int stars,
        float percent,
        Component rawLoreLine,
        Component percentLoreLine,
        Component rangeLoreLine,
        Component rerollLoreLine) {

    public boolean isNew() {
        return (idProfile == null || idProfile.isInvalidValue(value));
    }

    public boolean isFixed() {
        return !isNew() && idProfile.hasConstantValue();
    }
}
