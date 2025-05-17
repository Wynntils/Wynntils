/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.activities.widgets;

import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.utils.mc.LoreUtils;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class ContentBookActionWidget extends AbstractWidget implements TooltipProvider {
    private final Consumer<Integer> onClick;
    private final ItemStack itemStack;

    public ContentBookActionWidget(int x, int y, ItemStack itemStack, Consumer<Integer> onClick) {
        super(x, y, 16, 16, Component.literal("Content Book Action Button"));

        this.itemStack = itemStack;
        this.onClick = onClick;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.renderItem(itemStack, getX(), getY());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) return false;

        onClick.accept(button);
        return true;
    }

    @Override
    public List<Component> getTooltipLines() {
        return LoreUtils.getTooltipLines(itemStack);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
