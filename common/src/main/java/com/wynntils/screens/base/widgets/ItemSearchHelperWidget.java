/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.wynntils.core.components.Services;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.utils.render.Texture;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ItemSearchHelperWidget extends BasicTexturedButton implements TooltipProvider {
    public ItemSearchHelperWidget(
            int x,
            int y,
            int width,
            int height,
            Texture texture,
            Consumer<Integer> onClick,
            boolean renderTooltipAboveMouse,
            boolean scaleTexture) {
        super(x, y, width, height, texture, onClick, List.of(), renderTooltipAboveMouse, scaleTexture);
    }

    @Override
    public List<Component> getTooltipLines() {
        List<Component> helpTooltip =
                new ArrayList<>(List.of(Component.translatable("screens.wynntils.itemSearchHelperWidget.name")));

        Services.ItemFilter.getItemStatProviders().forEach(itemStatProvider -> {
            helpTooltip.add(Component.empty());
            helpTooltip.add(Component.literal(itemStatProvider.getName() + ": ").withStyle(ChatFormatting.YELLOW));
            helpTooltip.add(
                    Component.translatable(itemStatProvider.getDescription()).withStyle(ChatFormatting.GRAY));
        });

        return helpTooltip;
    }
}
