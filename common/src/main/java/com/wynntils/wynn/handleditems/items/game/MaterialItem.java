/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.game;

import com.wynntils.wynn.handleditems.properties.QualityTierItemProperty;
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

    @Override
    public String toString() {
        return "MaterialItem{" + "materialProfile=" + materialProfile + '}';
    }
}
