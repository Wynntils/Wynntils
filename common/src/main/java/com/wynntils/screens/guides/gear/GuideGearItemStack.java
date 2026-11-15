/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.gear;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.builders.IdentifiableTooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.screens.guides.GuideItemStack;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public final class GuideGearItemStack extends GuideItemStack {
    private final GearItem gearItem;
    private final GearInfo gearInfo;

    private List<Component> generatedTooltip;
    private int currentPage = 0;

    public GuideGearItemStack(GearInfo gearInfo) {
        super(gearInfo.metaInfo().material().itemStack(), new GearItem(gearInfo, null), gearInfo.name());

        this.gearItem = new GearItem(gearInfo, null);
        this.gearInfo = gearInfo;
        this.set(DataComponents.TOOLTIP_STYLE, gearInfo.tier().getTooltipStyle(false));
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag isAdvanced) {
        if (generatedTooltip == null) {
            generatedTooltip = IdentifiableTooltipBuilder.buildNewItem(gearItem, ItemSourceType.GUIDE, "", currentPage)
                    .getTooltipLines(Models.Character.getClassType());
        }

        return generatedTooltip;
    }

    public GearInfo getGearInfo() {
        return gearInfo;
    }

    public void changePage() {
        currentPage = (currentPage + 1) % 3;

        generatedTooltip = null;
    }
}
