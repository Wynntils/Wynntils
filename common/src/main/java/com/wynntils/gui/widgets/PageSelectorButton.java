/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.screens.WynntilsMenuPagedScreenBase;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class PageSelectorButton extends AbstractButton {
    private final boolean forward;
    private final WynntilsMenuPagedScreenBase screen;

    public PageSelectorButton(
            int x, int y, int width, int height, boolean forward, WynntilsMenuPagedScreenBase screen) {
        super(x, y, width, height, Component.literal("Page Selector Button"));
        this.forward = forward;
        this.screen = screen;
    }

    @Override
    public void onPress() {
        if (forward) {
            screen.setCurrentPage(screen.getCurrentPage() + 1);
        } else {
            screen.setCurrentPage(screen.getCurrentPage() - 1);
        }
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Texture backArrow = this.forward ? Texture.FORWARD_ARROW : Texture.BACKWARD_ARROW;

        if ((forward && screen.getCurrentPage() != screen.getMaxPage()) || (!forward && screen.getCurrentPage() != 0)) {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    backArrow.resource(),
                    this.getX(),
                    this.getY(),
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
                    this.getX(),
                    this.getY(),
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
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
