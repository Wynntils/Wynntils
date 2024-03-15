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

    /*
    Here we go again...
    Usually this is called when dragY reaches *about* 0.50.
    It varies sometimes if the user is dragging the scroll bar quickly.
    We need the mouse's relative position to the scroll bar to remain the same, as if we "picked up" the scroll bar.
    Scrolling once with scrollY = -1 will move the scroll bar by 7.575-ish on the particular display I tested on.
    We can kind of reverse-engineer how the scroll bar moves to determine the scaling value.

    (from mouseScrolled in SkillPointLoadoutsScreen)
    scrollPercent = scrollPercent - scrollY / 50
    So the difference in scrollPercent = -scrollY / 50

    (from scroll bar rendering in SkillPointLoadoutsScreen)
    y = dividedHeight * 8 + dividedHeight * 48 * scrollPercent
    We don't care about the actual value of scrollPercent, just the difference, so:
    Difference in y = dividedHeight * 8 + dividedHeight * 48 * (-scrollY / 50)
    We know difference in y from the onDrag here (dragY)
    Rearrange and solve for scrollY
    scrollY = (-25 * ydiff) / (24 * dividedHeight) + 25/3
    That 25/3 at the end comes from the fact that the scroll bar is constrained by 8 and 48 dividedHeight vertically
    We can just ignore it since we want the bar to stay still when the user isn't dragging anyway
     */
    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        super.onDrag(mouseX, mouseY, dragX, dragY);
        parent.mouseScrolled(mouseX, mouseY, 0, (-25 * dragY) / (24 * dividedHeight));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
