/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.SideListWidget;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public abstract class AbstractSideListScreen extends WynntilsGridLayoutScreen {
    private static final int ITEMS_PER_PAGE = 16;
    private static final int SCROLLBAR_HEIGHT = 40;
    private static final float SCROLL_FACTOR = 10f;

    protected List<SideListWidget> sideListWidgets = new ArrayList<>();

    private boolean draggingScroll;
    private float scrollRenderY;
    protected int scrollOffset = 0;

    protected AbstractSideListScreen(Component title) {
        super(title);
    }

    @Override
    protected void doInit() {
        super.doInit();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        for (SideListWidget sideListWidget : sideListWidgets) {
            sideListWidget.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (sideListWidgets.size() > ITEMS_PER_PAGE) {
            renderScrollBar(poseStack);
        } else if (sideListWidgets.isEmpty()) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            getEmptyListText(),
                            dividedWidth * 2,
                            dividedHeight * 32,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL,
                            2);
        }
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        for (SideListWidget widget : sideListWidgets) {
            if (widget.isMouseOver(mouseX, mouseY)) {
                return widget.mouseClicked(mouseX, mouseY, button);
            }
        }

        if (!draggingScroll
                && (sideListWidgets.size() > ITEMS_PER_PAGE)
                && MathUtils.isInside(
                        (int) mouseX,
                        (int) mouseY,
                        (int) (dividedWidth * 32),
                        (int) (dividedWidth * 32) + (int) (dividedWidth / 2),
                        (int) scrollRenderY,
                        (int) (scrollRenderY + SCROLLBAR_HEIGHT))) {
            draggingScroll = true;
            return true;
        }

        return super.doMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScroll) {
            int newOffset = Math.round(
                    MathUtils.map((float) mouseY, 20, 20 + this.height - SCROLLBAR_HEIGHT, 0, getMaxScrollOffset()));

            newOffset = Math.max(0, Math.min(newOffset, getMaxScrollOffset()));

            scroll(newOffset);

            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScroll = false;

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        int scrollAmount = (int) (-deltaY * SCROLL_FACTOR);

        if (sideListWidgets.size() > ITEMS_PER_PAGE) {
            int newOffset = Math.max(0, Math.min(scrollOffset + scrollAmount, getMaxScrollOffset()));
            scroll(newOffset);
        }

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    protected void scroll(int newOffset) {
        scrollOffset = newOffset;

        for (SideListWidget sideListWidget : sideListWidgets) {
            int newY = (sideListWidgets.indexOf(sideListWidget) * (int) (dividedHeight * 4)) - scrollOffset;

            sideListWidget.setY(newY);
        }
    }

    private int getMaxScrollOffset() {
        return (sideListWidgets.size() - ITEMS_PER_PAGE) * (int) (dividedHeight * 4);
    }

    private void renderScrollBar(PoseStack poseStack) {
        RenderUtils.drawRect(
                poseStack, CommonColors.LIGHT_GRAY, (dividedWidth * 32), 0, 0, (dividedWidth / 2), this.height);

        scrollRenderY = (int) (MathUtils.map(scrollOffset, 0, getMaxScrollOffset(), 0, this.height - SCROLLBAR_HEIGHT));

        RenderUtils.drawRect(
                poseStack,
                draggingScroll ? CommonColors.BLACK : CommonColors.GRAY,
                (dividedWidth * 32),
                scrollRenderY,
                0,
                (dividedWidth / 2),
                SCROLLBAR_HEIGHT);
    }

    protected abstract StyledText getEmptyListText();
}
