/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public abstract class ScrollListWidget extends AbstractWidget {
    private static final float SCROLL_FACTOR = 10f;
    private static final int SCROLL_BAR_BUTTON_HEIGHT_PADDING = 11;
    private static final int SCROLL_BAR_BUTTON_WIDTH_PADDING = 3;
    private static final int SCROLL_BAR_HEIGHT_PADDING = 4;
    private static final int SCROLL_BAR_WIDTH_PADDING = 4;
    public int scrollOffset = 0;
    private boolean draggingScroll = false;
    private final int x;
    private final int y;
    private final int widgetHeight;
    private final int widgetHeightPadding;
    private final int widgetHeightEdgePadding;
    private final int maxWidgetsPerPage;
    private float scrollY;

    public ScrollListWidget(
            int x,
            int y,
            int width,
            int height,
            int widgetHeight,
            int widgetHeightPadding,
            int widgetHeightEdgePadding,
            int maxWidgetsPerPage) {
        super(x, y, width, height, Component.literal("Scroll List Widget"));
        this.x = x;
        this.y = y;
        this.widgetHeight = widgetHeight;
        this.widgetHeightPadding = widgetHeightPadding;
        this.widgetHeightEdgePadding = widgetHeightEdgePadding;
        this.maxWidgetsPerPage = maxWidgetsPerPage;
    }

    protected abstract List<AbstractWidget> getWidgets();

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_LIGHT, x, y, this.width, this.height);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_SCROLL_SIDE_BAR,
                this.x + this.width - Texture.BUILD_LOADOUTS_SCROLL_SIDE_BAR.width() - SCROLL_BAR_WIDTH_PADDING / 2f,
                y + SCROLL_BAR_HEIGHT_PADDING,
                Texture.BUILD_LOADOUTS_SCROLL_SIDE_BAR.width(),
                this.height - SCROLL_BAR_HEIGHT_PADDING * 2);

        RenderUtils.enableScissor(
                guiGraphics,
                this.x,
                this.y + widgetHeightEdgePadding,
                this.width,
                this.height - widgetHeightEdgePadding * 2);
        getWidgets().forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
        RenderUtils.disableScissor(guiGraphics);

        renderScroll(guiGraphics);

        if (draggingScroll) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
        } else if (isOntopOfScrollDragButton(mouseX, mouseY)) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    private void renderScroll(GuiGraphics guiGraphics) {
        int maxScrollOffset = getMaxScrollOffset();
        scrollY = maxScrollOffset <= 0
                ? this.y + SCROLL_BAR_BUTTON_HEIGHT_PADDING
                : MathUtils.map(
                        scrollOffset,
                        0,
                        maxScrollOffset,
                        this.y + SCROLL_BAR_BUTTON_HEIGHT_PADDING,
                        this.y
                                + this.height
                                - Texture.BUILD_LOADOUTS_SCOLL_BAR_BUTTON.height()
                                - SCROLL_BAR_BUTTON_HEIGHT_PADDING);

        RenderUtils.drawTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_SCOLL_BAR_BUTTON,
                this.x + this.width - Texture.BUILD_LOADOUTS_SCOLL_BAR_BUTTON.width() - SCROLL_BAR_BUTTON_WIDTH_PADDING,
                scrollY);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!draggingScroll) return false;

        int scrollAreaStartY = this.y + SCROLL_BAR_BUTTON_HEIGHT_PADDING + 5;
        int scrollAreaHeight =
                this.height - Texture.BUILD_LOADOUTS_SCOLL_BAR_BUTTON.height() - SCROLL_BAR_BUTTON_HEIGHT_PADDING;

        int newOffset = Math.round(MathUtils.map(
                (float) event.y(), scrollAreaStartY, scrollAreaStartY + scrollAreaHeight, 0, getMaxScrollOffset()));

        newOffset = Math.max(0, Math.min(newOffset, getMaxScrollOffset()));

        scroll(newOffset);

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!draggingScroll) {
            if (getMaxScrollOffset() > 0 && isOntopOfScrollDragButton(event.x(), event.y())) {
                draggingScroll = true;

                return true;
            }
        }

        for (AbstractWidget widget : getWidgets()) {
            if (widget.isMouseOver(event.x(), event.y() + scrollOffset)) {
                return widget.mouseClicked(event, isDoubleClick);
            }
        }

        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingScroll = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        int scrollAmount = (int) (-deltaY * SCROLL_FACTOR);
        int newOffset = Math.max(0, Math.min(scrollOffset + scrollAmount, getMaxScrollOffset()));
        scroll(newOffset);

        return true;
    }

    private void scroll(int newOffset) {
        scrollOffset = newOffset;
        int currentY = this.y + widgetHeightEdgePadding;

        for (AbstractWidget widget : getWidgets()) {
            int newY = currentY - scrollOffset;
            widget.setY(newY);
            widget.visible = (newY <= this.y + this.height + widgetHeight + widgetHeightPadding)
                    && (newY >= this.y - widgetHeight - widgetHeightPadding);
            currentY += widgetHeight + widgetHeightPadding;
        }
    }

    private int getMaxScrollOffset() {
        return (getWidgets().size() - maxWidgetsPerPage) * (widgetHeight + widgetHeightPadding);
    }

    private boolean isOntopOfScrollDragButton(double mouseX, double mouseY) {
        return MathUtils.isInside(
                (int) mouseX,
                (int) mouseY,
                this.x + this.width - Texture.BUILD_LOADOUTS_SCOLL_BAR_BUTTON.width() - SCROLL_BAR_BUTTON_WIDTH_PADDING,
                this.x + this.width - SCROLL_BAR_BUTTON_WIDTH_PADDING,
                (int) scrollY,
                (int) (scrollY + Texture.SCROLL_BUTTON.height()));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
