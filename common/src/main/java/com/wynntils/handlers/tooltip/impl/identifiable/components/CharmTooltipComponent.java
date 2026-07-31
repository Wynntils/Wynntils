/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components;

import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.rewards.type.CharmInfo;
import com.wynntils.models.rewards.type.CharmInstance;
import com.wynntils.utils.mc.TooltipUtils;
import java.util.List;
import net.minecraft.network.chat.Component;

public class CharmTooltipComponent extends RewardTooltipComponent<CharmInfo, CharmInstance> {
    @Override
    protected GearType getGearType() {
        return GearType.CHARM;
    }

    @Override
    protected String getTypeName() {
        return "Charm";
    }

    @Override
    protected String getItemName(CharmInfo itemInfo) {
        return itemInfo.name();
    }

    @Override
    protected GearTier getTier(CharmInfo itemInfo) {
        return itemInfo.tier();
    }

    @Override
    protected GearRestrictions getRestrictions(CharmInfo itemInfo) {
        return itemInfo.metaInfo().restrictions();
    }

    @Override
    protected int getLevelRequirement(CharmInfo itemInfo) {
        return itemInfo.requirements().level();
    }

    @Override
    protected boolean isPerfect(CharmInstance itemInstance) {
        return itemInstance.isPerfect();
    }

    @Override
    protected boolean isDefective(CharmInstance itemInstance) {
        return itemInstance.isDefective();
    }

    @Override
    protected boolean hasOverallValue(CharmInstance itemInstance) {
        return itemInstance.hasOverallValue();
    }

    @Override
    protected float getOverallPercentage(CharmInstance itemInstance) {
        return itemInstance.getOverallPercentage();
    }

    @Override
    protected int getContentEnd(List<Component> tooltipLines) {
        int pageLineIndex = TooltipUtils.findFirstLineWithFont(tooltipLines, TOOLTIP_PAGE_FONT);
        return pageLineIndex >= 0 ? pageLineIndex : tooltipLines.size();
    }
}
