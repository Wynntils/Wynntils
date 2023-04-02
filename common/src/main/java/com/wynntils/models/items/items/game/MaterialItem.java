/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gathering.MaterialProfile;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.items.properties.QualityTierItemProperty;

public class MaterialItem extends GameItem implements QualityTierItemProperty, LeveledItemProperty {
    private final MaterialProfile materialProfile;

    public MaterialItem(MaterialProfile ingredientProfile) {
        this.materialProfile = ingredientProfile;
    }

    public MaterialProfile getMaterialProfile() {
        return materialProfile;
    }

    @Override
    public int getQualityTier() {
        return materialProfile.getTier();
    }

    @Override
    public int getLevel() {
        return materialProfile.getSourceMaterial().level();
    }

    @Override
    public String toString() {
        return "MaterialItem{" + "materialProfile=" + materialProfile + '}';
    }
}
