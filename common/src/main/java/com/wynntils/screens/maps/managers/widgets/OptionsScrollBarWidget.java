package com.wynntils.screens.maps.managers.widgets;

import com.wynntils.screens.maps.managers.CategoryManagementScreen;
import com.wynntils.screens.maps.managers.widgets.options.CategoryHeaderWidget;
import com.wynntils.screens.maps.managers.type.OptionCategory;
import com.wynntils.screens.maps.managers.widgets.options.AbstractOptionWidget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wynntils.utils.MathUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;

public class OptionsScrollBarWidget extends ScrollBarWidget {
    private static final int WIDGET_HEIGHT_PADDING = 4;

    private final List<AbstractOptionWidget<?>> registeredWidgets = new ArrayList<>();

    public OptionsScrollBarWidget(int x, int y, int width, int height, CategoryManagementScreen parent) {
        super(x, y, width, height, parent);
    }

    public void addWidget(AbstractOptionWidget<?> widget) {
        registeredWidgets.add(widget);
    }

    public List<AbstractOptionWidget<?>> getRegisteredWidgets() {
        return Collections.unmodifiableList(registeredWidgets);
    }

    @Override
    protected int getWidgetHeightPadding() {
        return WIDGET_HEIGHT_PADDING;
    }

    private int getHeightWithPadding(int index, int widgetHeight, int totalWidgets) {
        return widgetHeight + (index < totalWidgets - 1 ? getWidgetHeightPadding() : 0);
    }

    @Override
    public List<AbstractOptionWidget<?>> getWidgets() {
        List<AbstractOptionWidget<?>> layout = new ArrayList<>();

        for (OptionCategory category : OptionCategory.values()) {
            List<AbstractOptionWidget<?>> inCategory = registeredWidgets.stream()
                    .filter(widget -> widget.visible)
                    .filter(widget -> widget.getCategory() == category)
                    .toList();

            if (inCategory.isEmpty()) continue;

            layout.add(new CategoryHeaderWidget(category));
            layout.addAll(inCategory);
        }

        return layout;
    }

    @Override
    protected void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int viewportTop = getY() + SCROLL_BAR_HEIGHT_PADDING;
        int viewportBottom = viewportTop + getViewportHeight();
        int contentWidth = getViewportWidth();
        int viewportLeft = getX() + SCROLL_BAR_WIDTH_PADDING;
        int currentY = viewportTop - scrollOffsetY;

        List<AbstractOptionWidget<?>> widgets = getWidgets();
        for (int i = 0; i < widgets.size(); i++) {
            AbstractOptionWidget<?> abstractOptionWidget = widgets.get(i);
            int widgetHeight = abstractOptionWidget.getHeight();
            if (currentY + widgetHeight >= viewportTop && currentY <= viewportBottom) {
                abstractOptionWidget.setX(viewportLeft);
                abstractOptionWidget.setY(currentY);
                abstractOptionWidget.setWidth(contentWidth);
                abstractOptionWidget.render(guiGraphics, mouseX, mouseY, partialTick);
            }
            currentY += getHeightWithPadding(i, widgetHeight, widgets.size());
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (super.mouseClicked(event, isDoubleClick)) {
            return true;
        }

        int currentY = getY() + SCROLL_BAR_HEIGHT_PADDING - scrollOffsetY;

        List<AbstractOptionWidget<?>> widgets = getWidgets();
        for (int i = 0; i < widgets.size(); i++) {
            AbstractOptionWidget<?> abstractOptionWidget = widgets.get(i);
            int widgetHeight = abstractOptionWidget.getHeight();
            if (isInsideViewport(event.x(), event.y())) {
                abstractOptionWidget.setX(getX() + SCROLL_BAR_WIDTH_PADDING);
                abstractOptionWidget.setY(currentY);
                abstractOptionWidget.setWidth(getViewportWidth());
                if (abstractOptionWidget.isHovered() && abstractOptionWidget.mouseClicked(event, isDoubleClick)) {
                    return true;
                }
            }
            currentY += getHeightWithPadding(i, widgetHeight, widgets.size());
        }

        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        // Let the scrollbar handle its own dragging first
        if (super.mouseDragged(event, dragX, dragY)) {
            return true;
        }

        int currentY = getY() + SCROLL_BAR_HEIGHT_PADDING - scrollOffsetY;
        List<AbstractOptionWidget<?>> widgets = getWidgets();
        for (int i = 0; i < widgets.size(); i++) {
            AbstractOptionWidget<?> abstractOptionWidget = widgets.get(i);
            int widgetHeight = abstractOptionWidget.getHeight();
            if (isInsideViewport(event.x(), event.y())) {
                abstractOptionWidget.setX(getX() + SCROLL_BAR_WIDTH_PADDING);
                abstractOptionWidget.setY(currentY);
                abstractOptionWidget.setWidth(getViewportWidth());
                if (abstractOptionWidget.isHovered() && abstractOptionWidget.mouseDragged(event, dragX, dragY)) {
                    return true;
                }
            }
            currentY += getHeightWithPadding(i, widgetHeight, widgets.size());
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        int currentY = getY() + SCROLL_BAR_HEIGHT_PADDING - scrollOffsetY;

        List<AbstractOptionWidget<?>> widgets = getWidgets();
        for (int i = 0; i < widgets.size(); i++) {
            AbstractOptionWidget<?> abstractOptionWidget = widgets.get(i);
            int widgetHeight = abstractOptionWidget.getHeight();
            if (isInsideViewport(event.x(), event.y())) {
                abstractOptionWidget.setX(getX() + SCROLL_BAR_WIDTH_PADDING);
                abstractOptionWidget.setY(currentY);
                abstractOptionWidget.setWidth(getViewportWidth());
                if (abstractOptionWidget.isHovered() && abstractOptionWidget.mouseReleased(event)) {
                    return true;
                }
            }
            currentY += getHeightWithPadding(i, widgetHeight, widgets.size());
        }
        return super.mouseReleased(event);
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (!isInsideViewport(mouseX, mouseY)) {
            return false;
        }

        int currentY = getY() + SCROLL_BAR_HEIGHT_PADDING - scrollOffsetY;

        List<AbstractOptionWidget<?>> widgets = getWidgets();
        for (int i = 0; i < widgets.size(); i++) {
            AbstractOptionWidget<?> abstractOptionWidget = widgets.get(i);
            int widgetHeight = abstractOptionWidget.getHeight();
            if (isInsideViewport(mouseX, mouseY)) {
                abstractOptionWidget.setX(getX() + SCROLL_BAR_WIDTH_PADDING);
                abstractOptionWidget.setY(currentY);
                abstractOptionWidget.setWidth(getViewportWidth());
                if (abstractOptionWidget.isHovered() && abstractOptionWidget.mouseScrolled(mouseX, mouseY, deltaX, deltaY)) {
                    return true;
                }
            }
            currentY += getHeightWithPadding(i, widgetHeight, widgets.size());
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    public boolean isInsideViewport(double x, double y) {
        int viewportTop = getY() + SCROLL_BAR_HEIGHT_PADDING;
        int viewportBottom = viewportTop + getViewportHeight();
        int viewportLeft = getX() + SCROLL_BAR_WIDTH_PADDING;
        int viewportRight = viewportLeft + getViewportWidth();

        return MathUtils.isInside(
                (int) x,
                (int) y,
                viewportLeft,
                viewportRight,
                viewportTop,
                viewportBottom);
    }
}