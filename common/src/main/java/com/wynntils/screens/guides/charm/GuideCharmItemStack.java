/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.charm;

import com.wynntils.core.components.Handlers;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.models.items.items.game.CharmItem;
import com.wynntils.models.rewards.type.CharmInfo;
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

public class GuideCharmItemStack extends GuideItemStack {
    private final CharmItem charmItem;
    private final CharmInfo charmInfo;

    private List<Component> generatedTooltip;
    private int currentPage = 0;

    public GuideCharmItemStack(CharmInfo charmInfo) {
        super(charmInfo.metaInfo().material().itemStack(), new CharmItem(charmInfo, null), charmInfo.name());

        this.charmItem = new CharmItem(charmInfo, null);
        this.charmInfo = charmInfo;
        this.set(DataComponents.TOOLTIP_STYLE, charmInfo.tier().getTooltipStyle(false));
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag isAdvanced) {
        if (generatedTooltip == null) {
            List<Component> tooltip = new ArrayList<>(buildTooltip());

            generatedTooltip = tooltip;
        }

        return generatedTooltip;
    }

    public CharmInfo getCharmInfo() {
        return charmInfo;
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
            charmItem.getData().getOrCalculate(WynnItemData.TOOLTIP_KEY, () -> Handlers.Tooltip.buildNew(charmItem));
            return TooltipUtils.getWynnItemTooltip(this, charmItem);
        } else {
            List<Component> tooltipLines =
                    buildObtainInfoPage(charmItem.getItemInfo().metaInfo().obtainInfo());
            int widestLine = tooltipLines.stream()
                    .mapToInt(McUtils.mc().font::width)
                    .max()
                    .orElse(0);
            tooltipLines.addAll(buildPaginationLines(currentPage, 3, widestLine));
            return tooltipLines;
        }
    }
}
