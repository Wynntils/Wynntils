/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

public enum ItemWeightSource {
    NONE,
    WYNNPOOL,
    NORI,
    ALL;

    public boolean isSingleSource() {
        return this == WYNNPOOL || this == NORI;
    }
}
