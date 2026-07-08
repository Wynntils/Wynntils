package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.WynntilsMod;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public abstract class ScrollListWidget extends AbstractWidget {
    private static final float SCROLL_FACTOR = 10f;
    private static final int MAX_WIDGETS_PER_PAGE = 7;
    private static final int SCROLL_BAR_HEIGHT_PADDING = 5;
    private static final int SCROLL_BAR_WIDTH_PADDING = 2;
    private int scrollOffset = 0;
    private final int x;
    private final int y;
    private final int widgetHeight;
    private final int widgetHeightPadding;
    private final int widgetHeightEdgePadding;
    private float scrollY;

    public ScrollListWidget(int x, int y, int width, int height, int widgetHeight, int widgetHeightPadding, int widgetHeightEdgePadding) {
        super(x, y, width, height, Component.literal("Scroll List Widget"));
        this.x = x;
        this.y = y;
        this.widgetHeight = widgetHeight;
        this.widgetHeightPadding = widgetHeightPadding;
        this.widgetHeightEdgePadding = widgetHeightEdgePadding;
    }

    protected abstract List<AbstractWidget> getWidgets();

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_LIGHT,
                x,
                y,
                this.width,
                this.height);

        RenderUtils.enableScissor(
                guiGraphics, this.x, this.y + widgetHeightEdgePadding, this.width, this.height - widgetHeightEdgePadding * 2);
        getWidgets().forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
        RenderUtils.disableScissor(guiGraphics);

        renderScroll(guiGraphics);
    }

    private void renderScroll(GuiGraphics guiGraphics) {
        scrollY = MathUtils.map(scrollOffset, 0, getMaxScrollOffset(), this.y + SCROLL_BAR_HEIGHT_PADDING, this.y + this.height - Texture.SCROLL_BUTTON.height() - SCROLL_BAR_HEIGHT_PADDING);

        RenderUtils.drawTexturedRect(guiGraphics, Texture.SCROLL_BUTTON, this.x + this.width - Texture.SCROLL_BUTTON.width() - SCROLL_BAR_WIDTH_PADDING, scrollY);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        WynntilsMod.info("dragged");

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
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
            widget.visible = (newY <= this.y + this.height + widgetHeight + widgetHeightPadding) && (newY >= this.y - widgetHeight - widgetHeightPadding);
            currentY += widgetHeight + widgetHeightPadding;
        }
    }

    private int getMaxScrollOffset() {
        return (getWidgets().size() - MAX_WIDGETS_PER_PAGE) * (widgetHeight + widgetHeightPadding);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
