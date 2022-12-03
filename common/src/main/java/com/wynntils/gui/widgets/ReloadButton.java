/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ReloadButton extends AbstractButton {

    private final Runnable onClickRunnable;

    public ReloadButton(int x, int y, int width, int height, Runnable onClickRunnable) {
        super(x, y, width, height, Component.literal("Reload Button"));
        this.onClickRunnable = onClickRunnable;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Texture reloadButton = Texture.RELOAD_BUTTON;
        if (this.isHovered) {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    reloadButton.resource(),
                    this.x,
                    this.y,
                    0,
                    this.width,
                    this.height,
                    reloadButton.width() / 2,
                    0,
                    reloadButton.width() / 2,
                    reloadButton.height(),
                    reloadButton.width(),
                    reloadButton.height());
        } else {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    reloadButton.resource(),
                    this.x,
                    this.y,
                    0,
                    this.width,
                    this.height,
                    0,
                    0,
                    reloadButton.width() / 2,
                    reloadButton.height(),
                    reloadButton.width(),
                    reloadButton.height());
        }
    }

    @Override
    public void onPress() {
        onClickRunnable.run();
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
