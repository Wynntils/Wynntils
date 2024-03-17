/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.skillpointloadouts.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.skillpointloadouts.SkillPointLoadoutsScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ScrollBar extends AbstractWidget {
    // slightly darker than light gray but lighter than normal gray
    private static final CustomColor HOVERED = CustomColor.fromInt(0x8d8d8d).withAlpha(255);

    private final SkillPointLoadoutsScreen parent;
    private final float dividedHeight;

    public ScrollBar(
            float x, float y, float width, float height, SkillPointLoadoutsScreen parent, float dividedHeight) {
        super((int) x, (int) y, (int) width, (int) height, Component.empty());
        this.parent = parent;
        this.dividedHeight = dividedHeight;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawRect(
                poseStack,
                this.isMouseOver(mouseX, mouseY) ? HOVERED : CommonColors.LIGHT_GRAY,
                getX(),
                getY(),
                0,
                getWidth(),
                getHeight());
    }

    // dragY = dividedHeight * 8 + dividedHeight * 48 * (-scrollY / 50)
    // Discard constant after solving, it comes from the height of the scrollable area
    // Full explanation in #artemis-dev
    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        parent.doScroll((-25 * dragY) / (24 * dividedHeight));
        super.onDrag(mouseX, mouseY, dragX, dragY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
