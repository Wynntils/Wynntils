/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.type;

public record HarvestMaterial(ResourceType resourceType, SourceMaterial sourceMaterial, int tier) {
    public HarvestMaterial(ResourceType resourceType, SourceMaterial sourceMaterial) {
        this(resourceType, sourceMaterial, -1);
    }

    public HarvestMaterial withTier(int tier) {
        return new HarvestMaterial(this.resourceType(), this.sourceMaterial(), tier);
    }
}
