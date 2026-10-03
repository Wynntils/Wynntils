/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.RenderDirection;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class BasicHoverableButton extends BasicTexturedButton {
    public BasicHoverableButton(
            int x, int y, int width, int height, Texture texture, Consumer<Integer> onClick, List<Component> tooltip) {
        super(x, y, width, height, texture, onClick, tooltip, true);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawScalingHoverableTexturedRect(
                guiGraphics, texture, getX(), getY(), getWidth(), getHeight(), isHovered, RenderDirection.VERTICAL);
    }
}
