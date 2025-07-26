/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.changelog.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ExitFlagButton extends WynntilsButton {
    private final Runnable onClickRunnable;

    public ExitFlagButton(int x, int y, Runnable onClickRunnable) {
        super(x, y, Texture.EXIT_FLAG.width(), Texture.EXIT_FLAG.height() / 2, Component.literal("Exit Button"));
        this.onClickRunnable = onClickRunnable;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        Texture texture = Texture.EXIT_FLAG;
        RenderUtils.drawTexturedRect(
                poseStack,
                texture.resource(),
                this.getX(),
                this.getY(),
                0,
                this.width,
                this.height,
                0,
                isHovered ? texture.height() / 2 : 0,
                texture.width(),
                texture.height() / 2,
                texture.width(),
                texture.height());
    }

    @Override
    public void onPress() {
        onClickRunnable.run();
    }
}
