/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.game;

import com.wynntils.model.item.properties.QualityTierItemProperty;
import com.wynntils.wynn.objects.profiles.material.MaterialProfile;

public class MaterialItem extends GameItem implements QualityTierItemProperty {
    private final MaterialProfile materialProfile;

    public MaterialItem(MaterialProfile ingredientProfile) {
        this.materialProfile = ingredientProfile;
    }

    public MaterialProfile getMaterialProfile() {
        return materialProfile;
    }

    public int getQualityTier() {
        return materialProfile.getTier();
    }
}
