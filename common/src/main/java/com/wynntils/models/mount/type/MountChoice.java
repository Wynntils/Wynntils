/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.type;

public enum MountChoice {
    FIRST(null),
    HORSE(MountType.HORSE),
    WYVERN(MountType.WYVERN),
    ADASAUR(MountType.ADASAUR);

    private final MountType mountType;

    MountChoice(MountType mountType) {
        this.mountType = mountType;
    }

    public MountType getMountType() {
        return mountType;
    }
}
