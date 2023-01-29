/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear;

import com.wynntils.models.gear.profile.GearProfile;
import com.wynntils.models.ingredients.profile.IdentificationProfile;
import com.wynntils.models.ingredients.type.IdentificationModifier;

public record GearIdentificationContainer(
        String inGameIdName,
        GearProfile gearProfile,
        IdentificationProfile idProfile,
        IdentificationModifier modifier,
        String shortIdName,
        int value,
        int stars,
        float percent) {}
