/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.gear;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.TooltipUtils;
import java.util.ArrayList;
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
            List<Component> tooltip = new ArrayList<>(buildTooltip());

            generatedTooltip = tooltip;
        }

        return generatedTooltip;
    }

    public GearInfo getGearInfo() {
        return gearInfo;
    }

    public void changePage() {
        if (currentPage == 0) {
            currentPage = 2;
        } else if (currentPage == 2) {
            currentPage = 0;
        }

        generatedTooltip = null;
    }

    public List<Component> buildTooltip() {
        if (currentPage == 0) {
            gearItem.getData().getOrCalculate(WynnItemData.TOOLTIP_KEY, () -> Handlers.Tooltip.buildNew(gearItem));
            return TooltipUtils.getWynnItemTooltip(this, gearItem);
        } else {
            List<Component> tooltipLines = buildObtainInfoPage(Models.Gear.getObtainInfo(gearInfo));
            int widestLine = tooltipLines.stream()
                    .mapToInt(McUtils.mc().font::width)
                    .max()
                    .orElse(0);
            tooltipLines.addAll(buildPaginationLines(currentPage, 3, widestLine));
            return tooltipLines;
        }
    }
}
