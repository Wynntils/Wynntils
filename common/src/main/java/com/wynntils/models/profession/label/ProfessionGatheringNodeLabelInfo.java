/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.profession.type.MaterialProfile;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

public class ProfessionGatheringNodeLabelInfo extends ProfessionLabelInfo {
    private final MaterialProfile.SourceMaterial sourceMaterial;
    private final MaterialProfile.MaterialType materialType;

    public ProfessionGatheringNodeLabelInfo(
            StyledText label,
            String name,
            Location location,
            Entity entity,
            MaterialProfile.SourceMaterial sourceMaterial,
            MaterialProfile.MaterialType materialType) {
        super(label, name, location, entity, materialType.getProfessionType());
        this.sourceMaterial = sourceMaterial;
        this.materialType = materialType;
    }

    public MaterialProfile.SourceMaterial getSourceMaterial() {
        return sourceMaterial;
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
