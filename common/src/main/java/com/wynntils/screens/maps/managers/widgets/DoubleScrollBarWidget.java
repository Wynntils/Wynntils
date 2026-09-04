/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.screens.maps.managers.CategoryManagementScreen;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public abstract class DoubleScrollBarWidget extends AbstractWidget {
    private static final float SCROLL_FACTOR = 10f;

    protected static final int SCROLL_BAR_HEIGHT_PADDING = 4;
    protected static final int SCROLL_BAR_WIDTH_PADDING = 4;

    // Thumb never shrinks below this, otherwise it'd become an unclickable sliver on huge canvases.
    private static final int MIN_SCROLL_BUTTON_LENGTH = 12;

    // Gap between arrow buttons and the thumb's travel area (used on both ends)
    private static final int SCROLL_BAR_GAP = 0;

    // How far the vertical bar sits from the widget's right edge.
    private static final int VERTICAL_SCROLL_BAR_EDGE_PADDING = 4;

    // How far the horizontal bar sits from the widget's bottom edge.
    private static final int HORIZONTAL_SCROLL_BAR_EDGE_PADDING = 4;

    // scroll button size
    private static final int SCROLL_ARROW_BUTTON_WIDTH_VERTICAL = 9;
    private static final int SCROLL_ARROW_BUTTON_HEIGHT_VERTICAL = 11;
    private static final int SCROLL_ARROW_BUTTON_WIDTH_HORIZONTAL = 11;
    private static final int SCROLL_ARROW_BUTTON_HEIGHT_HORIZONTAL = 9;

    // How far scrollOffset moves per arrow button click.
    private static final int SCROLL_ARROW_STEP = 10;

    private final int x;
    private final int y;
    private final CategoryManagementScreen parent;

    private int canvasWidth;
    private int canvasHeight;

    public int scrollOffsetX = 0;
    public int scrollOffsetY = 0;

    private double dragOffsetX;
    private double dragOffsetY;
    private boolean draggingScrollX = false;
    private boolean draggingScrollY = false;

    private float scrollBarX;
    private float scrollBarY;
    private float verticalButtonLength;
    private float horizontalButtonLength;

    public DoubleScrollBarWidget(int x, int y, int width, int height, CategoryManagementScreen parent) {
        super(x, y, width, height, Component.literal("Category Tree Widget"));
        this.x = x;
        this.y = y;
        this.parent = parent;

        this.canvasWidth = width;
        this.canvasHeight = height;
    }

    public void setCanvasSize(int canvasWidth, int canvasHeight) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;

        int oldScrollOffsetX = scrollOffsetX;
        int oldScrollOffsetY = scrollOffsetY;

        scrollOffsetX = Math.max(0, Math.min(scrollOffsetX, getMaxScrollOffsetX()));
        scrollOffsetY = Math.max(0, Math.min(scrollOffsetY, getMaxScrollOffsetY()));

        if (oldScrollOffsetX != scrollOffsetX || oldScrollOffsetY != scrollOffsetY) {
            onScrollOffsetChanged();
        }
    }

    private int getViewportWidth() {
        int viewport = this.width - SCROLL_BAR_WIDTH_PADDING * 2;
        if (isVerticalScrollNeeded()) {
            viewport -= Texture.MANAGER_SCROLL_BAR_VERTICAL.width() + VERTICAL_SCROLL_BAR_EDGE_PADDING;
        }
        return viewport;
    }

    private int getViewportHeight() {
        int viewport = this.height - SCROLL_BAR_HEIGHT_PADDING * 2;
        if (isHorizontalScrollNeeded()) {
            viewport -= Texture.MANAGER_SCROLL_BAR_HORIZONTAL.height() + HORIZONTAL_SCROLL_BAR_EDGE_PADDING;
        }
        return viewport;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.MANAGER_WIDGET_BACKGROUND, x, y, this.width, this.height);

        RenderUtils.enableScissor(
                guiGraphics,
                this.x + SCROLL_BAR_WIDTH_PADDING,
                this.y + SCROLL_BAR_HEIGHT_PADDING,
                getViewportWidth(),
                getViewportHeight());

        renderCategoryTree(guiGraphics, mouseX, mouseY, partialTick);

        RenderUtils.disableScissor(guiGraphics);

        renderVerticalScroll(guiGraphics);
        renderHorizontalScroll(guiGraphics);

        if (draggingScrollY) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
        } else if (draggingScrollX) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_EW);
        } else if (isOntopOfVerticalScrollDragButton(mouseX, mouseY)
                || isOntopOfHorizontalScrollDragButton(mouseX, mouseY)) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        } else if (isOntopOfVerticalUpButton(mouseX, mouseY)
                || isOntopOfVerticalDownButton(mouseX, mouseY)
                || isOntopOfHorizontalLeftButton(mouseX, mouseY)
                || isOntopOfHorizontalRightButton(mouseX, mouseY)) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    protected abstract void renderCategoryTree(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    private void renderVerticalScroll(GuiGraphics guiGraphics) {
        if (!isVerticalScrollNeeded()) {
            verticalButtonLength = 0;
            scrollBarY = this.y;
            return;
        }

        float barX = getVerticalScrollBarX();
        float barAreaTop = getVerticalScrollBarY();
        float barAreaHeight = getVerticalScrollBarYBottom() - barAreaTop;

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_SCROLL_BAR_VERTICAL,
                barX,
                barAreaTop,
                Texture.MANAGER_SCROLL_BAR_VERTICAL.width(),
                barAreaHeight);

        verticalButtonLength = getScaledButtonLength(getVerticalTrackHeight(), this.height, canvasHeight);

        int maxScrollOffset = getMaxScrollOffsetY();
        float thumbTop = getVerticalScrollTrackTop();
        float thumbBottom = getVerticalScrollTrackBottom();

        scrollBarY = maxScrollOffset <= 0
                ? thumbTop
                : MathUtils.map(scrollOffsetY, 0, maxScrollOffset, thumbTop, thumbBottom);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_SCROLL_BAR_BUTTON,
                barX,
                scrollBarY,
                Texture.MANAGER_SCROLL_BAR_VERTICAL.width(),
                verticalButtonLength);
    }

    private void renderHorizontalScroll(GuiGraphics guiGraphics) {
        if (!isHorizontalScrollNeeded()) {
            horizontalButtonLength = 0;
            scrollBarX = this.x;
            return;
        }

        float barY = getHorizontalScrollBarY();
        float barAreaLeft = getHorizontalScrollBarX();
        float barAreaWidth = getHorizontalScrollBarXRight() - barAreaLeft;

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_SCROLL_BAR_HORIZONTAL,
                barAreaLeft,
                barY,
                barAreaWidth,
                Texture.MANAGER_SCROLL_BAR_HORIZONTAL.height());

        horizontalButtonLength = getScaledButtonLength(getHorizontalScrollBarTrackWidth(), this.width, canvasWidth);

        int maxScrollOffset = getMaxScrollOffsetX();
        float thumbLeft = getHorizontalScrollTrackLeft();
        float thumbRight = getHorizontalScrollTrackRight();

        scrollBarX = maxScrollOffset <= 0
                ? thumbLeft
                : MathUtils.map(scrollOffsetX, 0, maxScrollOffset, thumbLeft, thumbRight);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_SCROLL_BAR_BUTTON,
                scrollBarX,
                barY,
                horizontalButtonLength,
                Texture.MANAGER_SCROLL_BAR_HORIZONTAL.height());
    }

    private float getScaledButtonLength(float trackLength, int viewportSize, int canvasSize) {
        if (canvasSize <= 0) return trackLength;

        float visibleRatio = Math.min(1f, viewportSize / (float) canvasSize);
        return Math.max(MIN_SCROLL_BUTTON_LENGTH, trackLength * visibleRatio);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingScrollY) {
            double newScrollY = event.y() - dragOffsetY;

            int newOffset = Math.round(MathUtils.map(
                    (float) newScrollY,
                    getVerticalScrollTrackTop(),
                    getVerticalScrollTrackBottom(),
                    0,
                    getMaxScrollOffsetY()));

            scrollOffsetY = Math.max(0, Math.min(newOffset, getMaxScrollOffsetY()));
            onScrollOffsetChanged();

            return super.mouseDragged(event, dragX, dragY);
        }

        if (draggingScrollX) {
            double newScrollX = event.x() - dragOffsetX;

            int newOffset = Math.round(MathUtils.map(
                    (float) newScrollX,
                    getHorizontalScrollTrackLeft(),
                    getHorizontalScrollTrackRight(),
                    0,
                    getMaxScrollOffsetX()));

            scrollOffsetX = Math.max(0, Math.min(newOffset, getMaxScrollOffsetX()));
            onScrollOffsetChanged();

            return super.mouseDragged(event, dragX, dragY);
        }

        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (isOntopOfVerticalUpButton(event.x(), event.y())) {
            scrollOffsetY = Math.max(0, scrollOffsetY - SCROLL_ARROW_STEP);
            onScrollOffsetChanged();
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        if (isOntopOfVerticalDownButton(event.x(), event.y())) {
            scrollOffsetY = Math.min(getMaxScrollOffsetY(), scrollOffsetY + SCROLL_ARROW_STEP);
            onScrollOffsetChanged();
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        if (isOntopOfHorizontalLeftButton(event.x(), event.y())) {
            scrollOffsetX = Math.max(0, scrollOffsetX - SCROLL_ARROW_STEP);
            onScrollOffsetChanged();
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        if (isOntopOfHorizontalRightButton(event.x(), event.y())) {
            scrollOffsetX = Math.min(getMaxScrollOffsetX(), scrollOffsetX + SCROLL_ARROW_STEP);
            onScrollOffsetChanged();
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        if (!draggingScrollY && getMaxScrollOffsetY() > 0 && isOntopOfVerticalScrollDragButton(event.x(), event.y())) {
            draggingScrollY = true;
            dragOffsetY = event.y() - scrollBarY;
            return true;
        }

        if (!draggingScrollX
                && getMaxScrollOffsetX() > 0
                && isOntopOfHorizontalScrollDragButton(event.x(), event.y())) {
            draggingScrollX = true;
            dragOffsetX = event.x() - scrollBarX;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingScrollX = false;
        draggingScrollY = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int scrollAmount = (int) (-scrollY * SCROLL_FACTOR);

        if (isOverHorizontalScrollBar(mouseX, mouseY)) {
            scrollOffsetX = Math.max(0, Math.min(scrollOffsetX + scrollAmount, getMaxScrollOffsetX()));
        } else {
            scrollOffsetY = Math.max(0, Math.min(scrollOffsetY + scrollAmount, getMaxScrollOffsetY()));
        }
        onScrollOffsetChanged();

        return true;
    }

    protected abstract void onScrollOffsetChanged();

    private boolean isOverHorizontalScrollBar(double mouseX, double mouseY) {
        if (!isHorizontalScrollNeeded()) return false;

        float top = Math.min(getHorizontalScrollBarY(), getHorizontalArrowButtonY());

        return MathUtils.isInside(
                (int) mouseX, (int) mouseY, this.x, this.x + this.width, (int) top, this.y + this.height);
    }

    private int getMaxScrollOffsetX() {
        return Math.max(0, canvasWidth - getViewportWidth());
    }

    private int getMaxScrollOffsetY() {
        return Math.max(0, canvasHeight - getViewportHeight());
    }

    private boolean isVerticalScrollNeeded() {
        return canvasHeight > this.height - SCROLL_BAR_HEIGHT_PADDING * 2;
    }

    private boolean isHorizontalScrollNeeded() {
        return canvasWidth > this.width - SCROLL_BAR_WIDTH_PADDING * 2;
    }

    private float getVerticalScrollBarX() {
        return this.x + this.width - Texture.MANAGER_SCROLL_BAR_VERTICAL.width() - VERTICAL_SCROLL_BAR_EDGE_PADDING;
    }

    private float getHorizontalScrollBarY() {
        return this.y
                + this.height
                - Texture.MANAGER_SCROLL_BAR_HORIZONTAL.height()
                - HORIZONTAL_SCROLL_BAR_EDGE_PADDING;
    }

    // --- Vertical bar ---
    private float getVerticalScrollBarY() {
        return this.y + SCROLL_BAR_HEIGHT_PADDING;
    }

    private float getVerticalScrollBarYBottom() {
        float bottom = this.y + this.height - SCROLL_BAR_HEIGHT_PADDING;
        if (isHorizontalScrollNeeded()) {
            bottom -= Texture.MANAGER_SCROLL_BAR_HORIZONTAL.height() + HORIZONTAL_SCROLL_BAR_EDGE_PADDING;
        }
        return bottom;
    }

    private float getVerticalScrollBarTrackTop() {
        return getVerticalScrollBarY() + SCROLL_ARROW_BUTTON_HEIGHT_VERTICAL + SCROLL_BAR_GAP;
    }

    private float getVerticalScrollBarTrackBottom() {
        return getVerticalScrollBarYBottom() - SCROLL_ARROW_BUTTON_HEIGHT_VERTICAL - SCROLL_BAR_GAP;
    }

    private float getVerticalTrackHeight() {
        return Math.max(0, getVerticalScrollBarTrackBottom() - getVerticalScrollBarTrackTop());
    }

    private float getVerticalArrowButtonX() {
        return getVerticalScrollBarX();
    }

    private float getVerticalUpButtonY() {
        return getVerticalScrollBarY();
    }

    private float getVerticalDownButtonY() {
        return getVerticalScrollBarYBottom() - SCROLL_ARROW_BUTTON_HEIGHT_VERTICAL;
    }

    // --- Horizontal bar ---
    private float getHorizontalScrollBarX() {
        return this.x + SCROLL_BAR_WIDTH_PADDING;
    }

    private float getHorizontalScrollBarXRight() {
        float right = this.x + this.width - SCROLL_BAR_WIDTH_PADDING;
        if (isVerticalScrollNeeded()) {
            right -= Texture.MANAGER_SCROLL_BAR_VERTICAL.width() + VERTICAL_SCROLL_BAR_EDGE_PADDING;
        }
        return right;
    }

    private float getHorizontalScrollBarTrackLeft() {
        return getHorizontalScrollBarX() + SCROLL_ARROW_BUTTON_WIDTH_HORIZONTAL + SCROLL_BAR_GAP;
    }

    private float getHorizontalScrollBarTrackRight() {
        return getHorizontalScrollBarXRight() - SCROLL_ARROW_BUTTON_WIDTH_HORIZONTAL - SCROLL_BAR_GAP;
    }

    private float getHorizontalScrollBarTrackWidth() {
        return Math.max(0, getHorizontalScrollBarTrackRight() - getHorizontalScrollBarTrackLeft());
    }

    private float getHorizontalArrowButtonY() {
        return getHorizontalScrollBarY();
    }

    private float getHorizontalLeftButtonX() {
        return getHorizontalScrollBarX();
    }

    private float getHorizontalRightButtonX() {
        return getHorizontalScrollBarXRight() - SCROLL_ARROW_BUTTON_WIDTH_HORIZONTAL;
    }

    private float getVerticalScrollTrackTop() {
        return getVerticalScrollBarTrackTop() + SCROLL_BAR_GAP;
    }

    private float getVerticalScrollTrackBottom() {
        return getVerticalScrollBarTrackBottom() - verticalButtonLength - SCROLL_BAR_GAP;
    }

    private float getHorizontalScrollTrackLeft() {
        return getHorizontalScrollBarTrackLeft() + SCROLL_BAR_GAP;
    }

    private float getHorizontalScrollTrackRight() {
        return getHorizontalScrollBarTrackRight() - horizontalButtonLength - SCROLL_BAR_GAP;
    }

    private boolean isOntopOfVerticalScrollDragButton(double mouseX, double mouseY) {
        float trackX = getVerticalScrollBarX();

        return MathUtils.isInside(
                (int) mouseX,
                (int) mouseY,
                (int) trackX,
                (int) (trackX + Texture.MANAGER_SCROLL_BAR_VERTICAL.width() - 1),
                (int) scrollBarY,
                (int) (scrollBarY + verticalButtonLength - 1));
    }

    private boolean isOntopOfHorizontalScrollDragButton(double mouseX, double mouseY) {
        float trackY = getHorizontalScrollBarY();

        return MathUtils.isInside(
                (int) mouseX,
                (int) mouseY,
                (int) scrollBarX,
                (int) (scrollBarX + horizontalButtonLength - 1),
                (int) trackY,
                (int) (trackY + Texture.MANAGER_SCROLL_BAR_HORIZONTAL.height() - 1));
    }

    private boolean isOntopOfVerticalUpButton(double mouseX, double mouseY) {
        if (!isVerticalScrollNeeded()) return false;
        return isOntopOfArrowButton(
                mouseX,
                mouseY,
                getVerticalArrowButtonX(),
                getVerticalUpButtonY(),
                SCROLL_ARROW_BUTTON_WIDTH_VERTICAL,
                SCROLL_ARROW_BUTTON_HEIGHT_VERTICAL);
    }

    private boolean isOntopOfVerticalDownButton(double mouseX, double mouseY) {
        if (!isVerticalScrollNeeded()) return false;
        return isOntopOfArrowButton(
                mouseX,
                mouseY,
                getVerticalArrowButtonX(),
                getVerticalDownButtonY(),
                SCROLL_ARROW_BUTTON_WIDTH_VERTICAL,
                SCROLL_ARROW_BUTTON_HEIGHT_VERTICAL);
    }

    private boolean isOntopOfHorizontalLeftButton(double mouseX, double mouseY) {
        if (!isHorizontalScrollNeeded()) return false;
        return isOntopOfArrowButton(
                mouseX,
                mouseY,
                getHorizontalLeftButtonX(),
                getHorizontalArrowButtonY(),
                SCROLL_ARROW_BUTTON_WIDTH_HORIZONTAL,
                SCROLL_ARROW_BUTTON_HEIGHT_HORIZONTAL);
    }

    private boolean isOntopOfHorizontalRightButton(double mouseX, double mouseY) {
        if (!isHorizontalScrollNeeded()) return false;
        return isOntopOfArrowButton(
                mouseX,
                mouseY,
                getHorizontalRightButtonX(),
                getHorizontalArrowButtonY(),
                SCROLL_ARROW_BUTTON_WIDTH_HORIZONTAL,
                SCROLL_ARROW_BUTTON_HEIGHT_HORIZONTAL);
    }

    private boolean isOntopOfArrowButton(
            double mouseX, double mouseY, float buttonX, float buttonY, int buttonWidth, int buttonHeight) {
        return MathUtils.isInside(
                (int) mouseX, (int) mouseY, (int) buttonX, (int) (buttonX + buttonWidth - 1), (int) buttonY, (int)
                        (buttonY + buttonHeight - 1));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
