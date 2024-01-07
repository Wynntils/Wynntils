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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SavedItemsHelpButton extends WynntilsButton {
    private final List<Component> tooltip;

    public SavedItemsHelpButton(int x, int y, List<Component> tooltip) {
        super(x, y, 11, 11, Component.literal("Saved Items Help Button"));
        this.tooltip = ComponentUtils.wrapTooltips(tooltip, 200);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawHoverableTexturedRect(
                guiGraphics.pose(), Texture.VAULT_HELP, this.getX(), this.getY(), this.isHovered);

        if (this.isHovered) {
            McUtils.mc().screen.setTooltipForNextRenderPass(Lists.transform(tooltip, Component::getVisualOrderText));
        }
    }

    // Unused
    @Override
    public void onPress() {}
}
