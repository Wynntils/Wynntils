/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.label.infos;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.profession.type.MaterialProfile;
import com.wynntils.utils.mc.type.Location;

public class GatheringNodeLabelInfo extends ProfessionLabelInfo {
    private final MaterialProfile.SourceMaterial sourceMaterial;
    private final MaterialProfile.MaterialType materialType;

    public GatheringNodeLabelInfo(
            StyledText label,
            String name,
            Location location,
            MaterialProfile.SourceMaterial sourceMaterial,
            MaterialProfile.MaterialType materialType) {
        super(label, name, location, materialType.getProfessionType());
        this.sourceMaterial = sourceMaterial;
        this.materialType = materialType;
    }

    public MaterialProfile.SourceMaterial getSourceMaterial() {
        return sourceMaterial;
    }

    @Override
    public String toString() {
        return "GatheringNodeLabelInfo{" + "sourceMaterial="
                + sourceMaterial + ", materialType="
                + materialType + ", professionType="
                + professionType + ", label="
                + label + ", labelString='"
                + name + '\'' + ", location="
                + location + '}';
    }
}
