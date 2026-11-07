/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.tome;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.builders.IdentifiableTooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.screens.guides.GuideItemStack;
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
            generatedTooltip = IdentifiableTooltipBuilder.buildNewItem(tomeItem, ItemSourceType.GUIDE, "", currentPage)
                    .getTooltipLines(Models.Character.getClassType());
        }

        return generatedTooltip;
    }

    public TomeInfo getTomeInfo() {
        return tomeInfo;
    }

    public void changePage() {
        // Page 2 is the lore page in the builder which is what we want here
        currentPage = currentPage == 0 ? 2 : 0;

        generatedTooltip = null;
    }
}
