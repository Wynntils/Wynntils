/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.tome;

import com.wynntils.core.components.Handlers;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.rewards.type.TomeInfo;
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

public class GuideTomeItemStack extends GuideItemStack {
    private final TomeItem tomeItem;
    private final TomeInfo tomeInfo;

    private List<Component> generatedTooltip;
    private int currentPage = 0;

    public GuideTomeItemStack(TomeInfo tomeInfo) {
        super(tomeInfo.metaInfo().material().itemStack(), new TomeItem(tomeInfo, null), tomeInfo.name());

        this.tomeItem = new TomeItem(tomeInfo, null);
        this.tomeInfo = tomeInfo;
        this.set(DataComponents.TOOLTIP_STYLE, tomeInfo.tier().getTooltipStyle(false));
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag isAdvanced) {
        if (generatedTooltip == null) {
            List<Component> tooltip = new ArrayList<>(buildTooltip());

            generatedTooltip = tooltip;
        }

        return generatedTooltip;
    }

    public TomeInfo getTomeInfo() {
        return tomeInfo;
    }

    public void changePage() {
        if (currentPage == 0) {
            currentPage = 1;
        } else {
            currentPage = 0;
        }

        generatedTooltip = null;
    }

    public List<Component> buildTooltip() {
        if (currentPage == 0) {
            tomeItem.getData().getOrCalculate(WynnItemData.TOOLTIP_KEY, () -> Handlers.Tooltip.buildNew(tomeItem));
            return TooltipUtils.getWynnItemTooltip(this, tomeItem);
        } else {
            List<Component> tooltipLines =
                    buildObtainInfoPage(tomeItem.getItemInfo().metaInfo().obtainInfo());
            int widestLine = tooltipLines.stream()
                    .mapToInt(McUtils.mc().font::width)
                    .max()
                    .orElse(0);
            tooltipLines.addAll(buildPaginationLines(currentPage, 3, widestLine));
            return tooltipLines;
        }
    }
}
