/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BackButton extends AbstractButton {
    private final Screen backTo;

    public BackButton(int x, int y, int width, int height, Screen backTo) {
        super(x, y, width, height, Component.literal("Back Button"));
        this.backTo = backTo;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Texture backArrow = Texture.BACK_ARROW;
        if (this.isHovered) {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    backArrow.resource(),
                    this.x,
                    this.y,
                    0,
                    this.width,
                    this.height,
                    backArrow.width() / 2,
                    0,
                    backArrow.width() / 2,
                    backArrow.height(),
                    backArrow.width(),
                    backArrow.height());
        } else {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    backArrow.resource(),
                    this.x,
                    this.y,
                    0,
                    this.width,
                    this.height,
                    0,
                    0,
                    backArrow.width() / 2,
                    backArrow.height(),
                    backArrow.width(),
                    backArrow.height());
        }
    }

    @Override
    public void onPress() {
        McUtils.mc().setScreen(backTo);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
