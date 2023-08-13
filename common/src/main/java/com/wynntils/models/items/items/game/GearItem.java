/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.models.items.properties.LeveledItemProperty;
import java.util.Optional;

public class GearItem extends GameItem implements GearTierItemProperty, GearTypeItemProperty, LeveledItemProperty {
    private final GearInfo gearInfo;
    private final GearInstance gearInstance;

    public GearItem(GearInfo gearInfo, GearInstance gearInstance) {
        this.gearInfo = gearInfo;
        this.gearInstance = gearInstance;
    }

    public GearInfo getGearInfo() {
        return gearInfo;
    }

    public Optional<GearInstance> getGearInstance() {
        return Optional.ofNullable(gearInstance);
    }

    public boolean isUnidentified() {
        return gearInstance == null;
    }

    @Override
    public GearTier getGearTier() {
        return gearInfo.tier();
    }

    @Override
    public GearType getGearType() {
        return gearInfo.type();
    }

    @Override
    public int getLevel() {
        return gearInfo.requirements().level();
    }

    @Override
    public String toString() {
        return "GearItem{" + "gearInfo=" + gearInfo + ", gearInstance=" + gearInstance + '}';
    }
}
