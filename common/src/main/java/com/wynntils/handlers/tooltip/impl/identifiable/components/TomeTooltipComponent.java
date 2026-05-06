/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components;

import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.models.rewards.type.TomeInstance;

public class TomeTooltipComponent extends RewardTooltipComponent<TomeInfo, TomeInstance> {
    @Override
    protected GearType getGearType() {
        return GearType.MASTERY_TOME;
    }

    @Override
    protected String getTypeName() {
        return "Tome";
    }

    @Override
    protected String getItemName(TomeInfo itemInfo) {
        return itemInfo.name();
    }

    @Override
    protected GearTier getTier(TomeInfo itemInfo) {
        return itemInfo.tier();
    }

    @Override
    protected GearRestrictions getRestrictions(TomeInfo itemInfo) {
        return itemInfo.metaInfo().restrictions();
    }

    @Override
    protected int getLevelRequirement(TomeInfo itemInfo) {
        return itemInfo.requirements().level();
    }

    @Override
    protected boolean isPerfect(TomeInstance itemInstance) {
        return itemInstance.isPerfect();
    }

    @Override
    protected boolean isDefective(TomeInstance itemInstance) {
        return itemInstance.isDefective();
    }

    @Override
    protected boolean hasOverallValue(TomeInstance itemInstance) {
        return itemInstance.hasOverallValue();
    }

    @Override
    protected float getOverallPercentage(TomeInstance itemInstance) {
        return itemInstance.getOverallPercentage();
    }
}
