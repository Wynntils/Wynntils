/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemsharing.widgets;

import com.google.common.collect.Lists;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SavedItemsButton extends WynntilsButton {
    private final Consumer<Integer> onClick;
    private final List<Component> tooltip;
    private final Texture buttonTexture;

    public SavedItemsButton(int x, int y, Consumer<Integer> onClick, List<Component> tooltip, Texture buttonTexture) {
        super(x, y, 9, 9, Component.literal("Saved Items Button"));
        this.onClick = onClick;
        this.tooltip = ComponentUtils.wrapTooltips(tooltip, 200);
        this.buttonTexture = buttonTexture;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawHoverableTexturedRect(
                guiGraphics.pose(), buttonTexture, this.getX(), this.getY(), this.isHovered);

        if (this.isHovered) {
            McUtils.mc().screen.setTooltipForNextRenderPass(Lists.transform(tooltip, Component::getVisualOrderText));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        if (onClick != null) {
            onClick.accept(button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    // Unused
    @Override
    public void onPress() {}
}
