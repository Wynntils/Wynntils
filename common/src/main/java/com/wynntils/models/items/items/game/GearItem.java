/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.concepts.Powder;
import com.wynntils.models.gearinfo.type.GearInfo;
import com.wynntils.models.gearinfo.type.GearInstance;
import com.wynntils.models.gearinfo.type.GearTier;
import com.wynntils.models.items.properties.GearTierItemProperty;
import java.util.List;

public class GearItem extends GameItem implements GearTierItemProperty {
    private final GearInfo gearInfo;
    private final GearInstance gearInstance;

    public GearItem(GearInfo gearInfo, GearInstance gearInstance) {
        this.gearInfo = gearInfo;
        this.gearInstance = gearInstance;
    }

    public GearInfo getGearInfo() {
        return gearInfo;
    }

    public GearInstance getGearInstance() {
        return gearInstance;
    }

    public boolean isUnidentified() {
        return gearInstance == null;
    }

    public List<Powder> getPowders() {
        if (gearInstance == null) return List.of();

        return gearInstance.powders();
    }

    public int getRerolls() {
        if (gearInstance == null) return 0;

        return gearInstance.rerolls();
    }

    @Override
    public GearTier getGearTier() {
        return gearInfo.tier();
    }

    @Override
    public String toString() {
        return "GearItem{" + "gearInfo=" + gearInfo + ", gearInstance=" + gearInstance + '}';
    }

    public boolean hasVariableIds() {
        if (gearInstance == null) return false;

        return gearInstance.hasVariableIds();
    }

    public float getOverallPercentage() {
        if (gearInstance == null) return 0;

        return gearInstance.getOverallPercentage();
    }

    public boolean isPerfect() {
        if (gearInstance == null) return false;

        return gearInstance.isPerfect();
    }

    public boolean isDefective() {
        if (gearInstance == null) return false;

        return gearInstance.isDefective();
    }
}
