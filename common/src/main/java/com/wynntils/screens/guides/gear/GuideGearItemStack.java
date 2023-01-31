/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.gear;

import com.wynntils.core.components.Models;
import com.wynntils.models.gearinfo.tooltip.GearTooltipBuilder;
import com.wynntils.models.gearinfo.type.GearInfo;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.screens.guides.GuideItemStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;

public final class GuideGearItemStack extends GuideItemStack {
    private final GearInfo gearInfo;
    private final MutableComponent name;
    private final List<Component> generatedTooltip;

    public GuideGearItemStack(GearInfo gearInfo) {
        super(gearInfo.metaInfo().material().getItemStack(), new GearItem(gearInfo, null));

        this.gearInfo = gearInfo;
        this.name = Component.literal(gearInfo.name()).withStyle(gearInfo.tier().getChatFormatting());
        GearTooltipBuilder gearTooltipBuilder = Models.GearTooltip.buildNew(gearInfo, null, true);
        this.generatedTooltip = gearTooltipBuilder.getTooltipLines();
    }

    @Override
    public Component getHoverName() {
        return name;
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag flag) {
        ArrayList<Component> tooltipLines = new ArrayList<>(generatedTooltip);

        tooltipLines.add(Component.empty());
        if (Models.Favorites.isFavorite(this)) {
            tooltipLines.add(Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.unfavorite")
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltipLines.add(Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.favorite")
                    .withStyle(ChatFormatting.GREEN));
        }
        tooltipLines.add(Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.open")
                .withStyle(ChatFormatting.RED));

        return tooltipLines;
    }

    public GearInfo getGearInfo() {
        return gearInfo;
    }
}
