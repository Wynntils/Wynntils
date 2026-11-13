/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.type;

import com.wynntils.utils.StringUtils;

/**
 * One kind of gathering node, as shown in the gathering node filter. The category id is the map data
 * category all nodes of this kind belong to, and is what node visibility is keyed on.
 */
public record GatheringNodeType(String categoryId, MaterialType materialType, SourceMaterial sourceMaterial)
        implements Comparable<GatheringNodeType> {
    public String getDisplayName() {
        return sourceMaterial.name() + " " + StringUtils.capitalized(materialType.name());
    }

    @Override
    public int compareTo(GatheringNodeType other) {
        int materialTypeCompare = Integer.compare(materialType.ordinal(), other.materialType.ordinal());
        if (materialTypeCompare != 0) return materialTypeCompare;

        int levelCompare = Integer.compare(sourceMaterial.level(), other.sourceMaterial.level());
        if (levelCompare != 0) return levelCompare;

        return sourceMaterial.name().compareToIgnoreCase(other.sourceMaterial.name());
    }
}
