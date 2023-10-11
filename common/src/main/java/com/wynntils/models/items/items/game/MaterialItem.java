/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.items.properties.ProfessionItemProperty;
import com.wynntils.models.items.properties.QualityTierItemProperty;
import com.wynntils.models.profession.type.MaterialProfile;
import com.wynntils.models.profession.type.ProfessionType;
import java.util.List;

public class MaterialItem extends GameItem
        implements QualityTierItemProperty, LeveledItemProperty, ProfessionItemProperty {
    private final MaterialProfile materialProfile;

    public MaterialItem(int emeraldPrice, MaterialProfile ingredientProfile) {
        super(emeraldPrice);
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
    public List<ProfessionType> getProfessionTypes() {
        return List.of(materialProfile.getResourceType().getMaterialType().getProfessionType());
    }

    @Override
    public String toString() {
        return "MaterialItem{" + "materialProfile=" + materialProfile + ", emeraldPrice=" + emeraldPrice + '}';
    }
}
