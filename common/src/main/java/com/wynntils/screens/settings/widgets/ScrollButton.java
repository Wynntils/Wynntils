/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

public class ScrollButton extends WynntilsButton {
    private final Consumer<Integer> onScroll;
    private final int y2;
    private final int maxScroll;
    private final int perScrollIncrement;
    private final CustomColor scrollAreaColor;
    private final float requiredChangePerElement;
    private int currentScroll = 0;

    private double currentUnusedScroll = 0;

    private double currentUnusedDrag = 0;
    private boolean dragging = false;

    public ScrollButton(
            int x,
            int y,
            int y2,
            int width,
            int height,
            int maxScroll,
            int perScrollIncrement,
            Consumer<Integer> onScroll,
            CustomColor scrollAreaColor) {
        super(x, y, width, height, Component.literal("Scroll Button"));
        this.y2 = y2;
        this.maxScroll = maxScroll;
        this.perScrollIncrement = perScrollIncrement;
        this.onScroll = onScroll;
        this.scrollAreaColor = scrollAreaColor;
        this.requiredChangePerElement = (y2 - y) / 8f;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (maxScroll == 0) return;

        if (scrollAreaColor != CustomColor.NONE) {
            RenderUtils.drawRect(
                    poseStack,
                    scrollAreaColor,
                    this.getX(),
                    this.getY() + 2,
                    0,
                    this.width,
                    this.y2 - this.getY() - 4 + this.height);
        }

        float renderY = MathUtils.map(currentScroll, 0, maxScroll, getY(), y2);

        RenderUtils.drawHoverableTexturedRect(
                poseStack, Texture.SETTING_SCROLL_BUTTON, this.getX(), renderY, isHovered);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        float renderY = MathUtils.map(currentScroll, 0, maxScroll, getY(), y2);

        return mouseX >= this.getX()
                && mouseX <= this.getX() + this.width
                && mouseY >= renderY
                && mouseY <= renderY + this.height;
    }

    @Override
    public void onPress() {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        dragging = true;
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        currentUnusedDrag = 0;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragY == 0) return true;

        if (dragging) {
            currentUnusedDrag += dragY;

            while (currentUnusedDrag >= requiredChangePerElement) {
                scroll(1);
                currentUnusedDrag -= requiredChangePerElement;
            }

            while (currentUnusedDrag <= -requiredChangePerElement) {
                scroll(-1);
                currentUnusedDrag += requiredChangePerElement;
            }
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Usually, mouse scroll wheel delta is always (-)1
        if (Math.abs(delta) == 1) {
            scroll((int) -delta);
            return true;
        }

        // Now we handle touchpad scrolling

        // Delta is divided by 10 to make it more precise
        // We subtract so scrolling down actually scrolls down
        currentUnusedScroll -= delta / 10d;

        if (Math.abs(currentUnusedScroll) < 1) return true;

        int scroll = (int) (currentUnusedScroll);
        currentUnusedScroll = currentUnusedScroll % 1;

        scroll(scroll);

        return true;
    }

    private void scroll(int scroll) {
        onScroll.accept(scroll * perScrollIncrement);
        currentScroll = MathUtils.clamp(currentScroll + scroll * perScrollIncrement, 0, maxScroll);
    }
}
