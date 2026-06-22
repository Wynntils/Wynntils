/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

public record ItemInformation(int itemId, float modelId) {
    public float getLockedDamage() {
        return modelId;
    }

    public float getUnlockableDamage() {
        return modelId + 1;
    }

    public float getBlockedDamage() {
        return modelId + 2;
    }

    public float getUnlockedDamage() {
        return modelId + 3;
    }
}
