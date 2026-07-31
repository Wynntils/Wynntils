/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.type;

import java.util.Locale;

public enum ResourceType {
    INGOT(MaterialType.ORE),
    GEM(MaterialType.ORE),
    PLANK(MaterialType.LOG),
    PAPER(MaterialType.LOG),
    STRING(MaterialType.CROP),
    GRAINS(MaterialType.CROP),
    OIL(MaterialType.FISH),
    MEAT(MaterialType.FISH);

    private final MaterialType materialType;

    ResourceType(MaterialType materialType) {
        this.materialType = materialType;
    }

    public static ResourceType fromString(String str) {
        try {
            return ResourceType.valueOf(str.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public MaterialType getMaterialType() {
        return materialType;
    }
}
