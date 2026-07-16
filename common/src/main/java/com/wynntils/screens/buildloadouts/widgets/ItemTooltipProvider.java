package com.wynntils.screens.buildloadouts.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface ItemTooltipProvider {
    void renderHoveredItemTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY);
}
