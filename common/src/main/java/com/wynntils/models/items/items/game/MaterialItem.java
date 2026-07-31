/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.items.properties.ProfessionItemProperty;
import com.wynntils.models.items.properties.QualityTierItemProperty;
import com.wynntils.models.profession.type.MaterialInfo;
import com.wynntils.models.profession.type.ProfessionType;
import java.util.List;

public class MaterialItem extends GameItem
        implements QualityTierItemProperty, LeveledItemProperty, ProfessionItemProperty {
    private final MaterialInfo materialInfo;
    private final int tier;

    public MaterialItem(MaterialInfo materialInfo, int tier) {
        this.materialInfo = materialInfo;
        this.tier = tier;
    }

    public MaterialInfo getMaterialInfo() {
        return materialInfo;
    }

    @Override
    public int getQualityTier() {
        return tier;
    }

    @Override
    public int getLevel() {
        return materialInfo.level();
    }

    @Override
    public String toString() {
        return "MaterialItem{" + "materialInfo=" + materialInfo + '}';
    }

    @Override
    public List<ProfessionType> getProfessionTypes() {
        return List.of(materialInfo.professionType());
    }
}
