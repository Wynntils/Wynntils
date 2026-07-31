/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.profession.type.MaterialType;
import com.wynntils.models.profession.type.SourceMaterial;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

public class ProfessionGatheringNodeLabelInfo extends ProfessionLabelInfo {
    private final SourceMaterial sourceMaterial;
    private final MaterialType materialType;

    public ProfessionGatheringNodeLabelInfo(
            StyledText label,
            String name,
            Location location,
            Entity entity,
            SourceMaterial sourceMaterial,
            MaterialType materialType) {
        super(label, name, location, entity, materialType.getProfessionType());
        this.sourceMaterial = sourceMaterial;
        this.materialType = materialType;
    }

    public SourceMaterial getSourceMaterial() {
        return sourceMaterial;
    }

    public MaterialType getMaterialType() {
        return materialType;
    }

    @Override
    public String toString() {
        return "ProfessionGatheringNodeLabelInfo{" + "sourceMaterial="
                + sourceMaterial + ", materialType="
                + materialType + ", professionType="
                + professionType + ", label="
                + label + ", name='"
                + name + '\'' + ", location="
                + location + ", entity="
                + entity + '}';
    }
}
