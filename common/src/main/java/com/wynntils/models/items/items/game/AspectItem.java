/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.aspects.type.AspectInfo;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.ClassableItemProperty;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.models.items.properties.NumberedTierItemProperty;

public class AspectItem extends GameItem
        implements NamedItemProperty, GearTierItemProperty, ClassableItemProperty, NumberedTierItemProperty {
    private final AspectInfo aspectInfo;
    private final int aspectTier;

    public AspectItem(AspectInfo aspectInfo, int aspectTier) {
        this.aspectInfo = aspectInfo;
        this.aspectTier = aspectTier;
    }

    public AspectInfo getAspectInfo() {
        return aspectInfo;
    }

    @Override
    public String getName() {
        return aspectInfo.name();
    }

    @Override
    public ClassType getRequiredClass() {
        return aspectInfo.classType();
    }

    @Override
    public GearTier getGearTier() {
        return aspectInfo.gearTier();
    }

    @Override
    public int getTier() {
        return aspectTier;
    }

    @Override
    public String toString() {
        return "AspectItem{" + "aspectInfo=" + aspectInfo + ", aspectTier=" + aspectTier + '}';
    }
}
