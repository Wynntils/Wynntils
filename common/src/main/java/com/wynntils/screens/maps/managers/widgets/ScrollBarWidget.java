package com.wynntils.screens.maps.managers.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.screens.maps.managers.CategoryManagementScreen;
import com.wynntils.screens.maps.managers.widgets.options.AbstractOptionWidget;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public abstract class ScrollBarWidget extends AbstractWidget {
    private static final float SCROLL_FACTOR = 10f;

    protected static final int SCROLL_BAR_HEIGHT_PADDING = 4;
    protected static final int SCROLL_BAR_WIDTH_PADDING = 4;

    // Thumb never shrinks below this, otherwise it'd become an unclickable sliver on huge canvases.
    private static final int MIN_SCROLL_BUTTON_LENGTH = 12;

    // Gap between arrow buttons and the thumb's travel area (used on both ends)
    private static final int SCROLL_BAR_GAP = 0;

    // How far the vertical bar sits from the widget's right edge.
    private static final int VERTICAL_SCROLL_BAR_EDGE_PADDING = 4;

    // scroll button size
    private static final int SCROLL_ARROW_BUTTON_WIDTH_VERTICAL = 9;
    private static final int SCROLL_ARROW_BUTTON_HEIGHT_VERTICAL = 11;

    // How far scrollOffset moves per arrow button click.
    private static final int SCROLL_ARROW_STEP = 10;

    private final int x;
    private final int y;
    private final CategoryManagementScreen parent;

    public int scrollOffsetY = 0;

    private double dragOffsetY;
    private boolean draggingScrollY = false;

    private float scrollBarY;
    private float verticalButtonLength;

    public ScrollBarWidget(int x, int y, int width, int height, CategoryManagementScreen parent) {
        super(x, y, width, height, Component.literal("Category Tree Widget"));
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    protected int getWidgetHeightPadding() {
        return 0;
    }

    /**
     * The widgets whose combined height determines the scrollable canvas height.
     */
    protected abstract List<? extends AbstractOptionWidget<?>> getWidgets();

    private int getCanvasHeight() {
        List<? extends AbstractOptionWidget<?>> widgets = getWidgets();
        int total = 0;

        for (int i = 0; i < widgets.size(); i++) {
            total += widgets.get(i).getHeight();
            if (i < widgets.size() - 1) {
                total += getWidgetHeightPadding();
            }
        }
        return total;
    }

    protected int getViewportWidth() {
        int viewport = this.width - SCROLL_BAR_WIDTH_PADDING * 2;
        if (isVerticalScrollNeeded()) {
            viewport -= Texture.MANAGER_SCROLL_BAR_VERTICAL.width() + VERTICAL_SCROLL_BAR_EDGE_PADDING;
        }
        return viewport;
    }

    protected int getViewportHeight() {
        return this.height - SCROLL_BAR_HEIGHT_PADDING * 2;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Canvas height can change every frame (widgets added/removed), so keep the offset in bounds.
        scrollOffsetY = Math.max(0, Math.min(scrollOffsetY, getMaxScrollOffsetY()));

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.MANAGER_WIDGET_BACKGROUND, x, y, this.width, this.height);

        RenderUtils.enableScissor(
                guiGraphics,
                this.x + SCROLL_BAR_WIDTH_PADDING,
                this.y + SCROLL_BAR_HEIGHT_PADDING,
                getViewportWidth(),
                getViewportHeight());

        renderWidgets(guiGraphics, mouseX, mouseY, partialTick);

        RenderUtils.disableScissor(guiGraphics);

        renderVerticalScroll(guiGraphics);

        if (draggingScrollY) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
        } else if (isOntopOfVerticalScrollDragButton(mouseX, mouseY)) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        } else if (isOntopOfVerticalUpButton(mouseX, mouseY) || isOntopOfVerticalDownButton(mouseX, mouseY)) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    protected abstract void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

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

        verticalButtonLength = getScaledButtonLength(getVerticalTrackHeight(), this.height, getCanvasHeight());

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

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (isOntopOfVerticalUpButton(event.x(), event.y())) {
            scrollOffsetY = Math.max(0, scrollOffsetY - SCROLL_ARROW_STEP);
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        if (isOntopOfVerticalDownButton(event.x(), event.y())) {
            scrollOffsetY = Math.min(getMaxScrollOffsetY(), scrollOffsetY + SCROLL_ARROW_STEP);
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        if (!draggingScrollY && getMaxScrollOffsetY() > 0 && isOntopOfVerticalScrollDragButton(event.x(), event.y())) {
            draggingScrollY = true;
            dragOffsetY = event.y() - scrollBarY;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingScrollY = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        int scrollAmount = (int) (-deltaY * SCROLL_FACTOR);

        scrollOffsetY = Math.max(0, Math.min(scrollOffsetY + scrollAmount, getMaxScrollOffsetY()));

        return true;
    }

    private int getMaxScrollOffsetY() {
        return Math.max(0, getCanvasHeight() - getViewportHeight());
    }

    private boolean isVerticalScrollNeeded() {
        return getCanvasHeight() > this.height - SCROLL_BAR_HEIGHT_PADDING * 2;
    }

    private float getVerticalScrollBarX() {
        return this.x + this.width - Texture.MANAGER_SCROLL_BAR_VERTICAL.width() - VERTICAL_SCROLL_BAR_EDGE_PADDING;
    }

    // --- Vertical bar geometry ---
    // The bar texture (arrows baked into its top/bottom caps) is drawn across the full area
    // between getVerticalScrollBarY() and getVerticalScrollBarYBottom() - see renderVerticalScroll().
    // The methods below instead carve out the sub-region reserved for the thumb, which must stay
    // clear of those baked-in arrow caps.

    private float getVerticalScrollBarY() {
        return this.y + SCROLL_BAR_HEIGHT_PADDING;
    }

    private float getVerticalScrollBarYBottom() {
        return this.y + this.height - SCROLL_BAR_HEIGHT_PADDING;
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

    private float getVerticalScrollTrackTop() {
        return getVerticalScrollBarTrackTop() + SCROLL_BAR_GAP;
    }

    private float getVerticalScrollTrackBottom() {
        return getVerticalScrollBarTrackBottom() - verticalButtonLength - SCROLL_BAR_GAP;
    }

    private boolean isOntopOfVerticalScrollDragButton(double mouseX, double mouseY) {
        float trackX = getVerticalScrollBarX();

        return MathUtils.isInside(
                (int) mouseX, (int) mouseY,
                (int) trackX,
                (int) (trackX + Texture.MANAGER_SCROLL_BAR_VERTICAL.width() - 1),
                (int) scrollBarY,
                (int) (scrollBarY + verticalButtonLength - 1)
        );
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

    private boolean isOntopOfArrowButton(
            double mouseX, double mouseY, float buttonX, float buttonY, int buttonWidth, int buttonHeight) {
        return MathUtils.isInside(
                (int) mouseX, (int) mouseY,
                (int) buttonX,
                (int) (buttonX + buttonWidth - 1),
                (int) buttonY,
                (int) (buttonY + buttonHeight - 1)
        );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}