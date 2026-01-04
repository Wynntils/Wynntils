/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.RenderDirection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class BackButton extends WynntilsButton {
    private final Screen backTo;

    public BackButton(int x, int y, int width, int height, Screen backTo) {
        super(x, y, width, height, Component.literal("Back Button"));
        this.backTo = backTo;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawHoverableTexturedRect(
                guiGraphics, Texture.BACK_ARROW_OFFSET, getX(), getY(), isHovered, RenderDirection.HORIZONTAL);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        McUtils.setScreen(backTo);
    }
}
