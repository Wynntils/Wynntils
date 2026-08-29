/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.models.mount.type.MountInfo;
import com.wynntils.models.mount.type.MountType;

public class MountItem extends GameItem implements GearTierItemProperty, NamedItemProperty {
    private final String name;
    private final MountType mountType;
    private final MountInfo mountInfo;
    private final boolean isSummonItem;

    public MountItem(String name, MountType mountType, MountInfo mountInfo, boolean isSummonItem) {
        this.name = name;
        this.mountType = mountType;
        this.mountInfo = mountInfo;
        this.isSummonItem = isSummonItem;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public GearTier getGearTier() {
        return GearTier.NORMAL;
    }

    public MountType getMountType() {
        return mountType;
    }

    public MountInfo getMountInfo() {
        return mountInfo;
    }

    public boolean isSummonItem() {
        return isSummonItem;
    }

    @Override
    public String toString() {
        return "MountItem{" + "name='"
                + name + '\'' + ", mountType="
                + mountType + ", mountInfo="
                + mountInfo + ", isSummonItem="
                + isSummonItem + '}';
    }
}
