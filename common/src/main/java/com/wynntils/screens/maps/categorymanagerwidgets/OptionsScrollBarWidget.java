package com.wynntils.screens.maps.categorymanagerwidgets;

import com.wynntils.screens.maps.CategoryManagementScreen;
import com.wynntils.screens.maps.type.OptionCategory;
import com.wynntils.screens.maps.type.ScrollableWidget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wynntils.utils.MathUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;

public class OptionsScrollBarWidget extends ScrollBarWidget {
    private static final int WIDGET_PADDING = 3;

    private final List<ScrollableWidget<?>> registeredWidgets = new ArrayList<>();

    public OptionsScrollBarWidget(int x, int y, int width, int height, CategoryManagementScreen parent) {
        super(x, y, width, height, parent);
    }

    public void addWidget(ScrollableWidget<?> widget) {
        registeredWidgets.add(widget);
    }

    public List<ScrollableWidget<?>> getRegisteredWidgets() {
        return Collections.unmodifiableList(registeredWidgets);
    }

    private int getHeightWithPadding(int index, int widgetHeight, int totalWidgets) {
        return widgetHeight + (index < totalWidgets - 1 ? WIDGET_PADDING : 0);
    }

    @Override
    protected List<ScrollableWidget<?>> getWidgets() {
        List<ScrollableWidget<?>> layout = new ArrayList<>();

        for (OptionCategory category : OptionCategory.values()) {
            List<ScrollableWidget<?>> inCategory = registeredWidgets.stream()
                    .filter(ScrollableWidget::isVisible)
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

        List<ScrollableWidget<?>> widgets = getWidgets();
        for (int i = 0; i < widgets.size(); i++) {
            AbstractWidget abstractWidget = (AbstractWidget) widgets.get(i);
            int widgetHeight = abstractWidget.getHeight();
            if (currentY + widgetHeight >= viewportTop && currentY <= viewportBottom) {
                abstractWidget.setX(viewportLeft);
                abstractWidget.setY(currentY);
                abstractWidget.setWidth(contentWidth);
                abstractWidget.render(guiGraphics, mouseX, mouseY, partialTick);
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

        List<ScrollableWidget<?>> widgets = getWidgets();
        for (int i = 0; i < widgets.size(); i++) {
            AbstractWidget abstractWidget = (AbstractWidget) widgets.get(i);
            int widgetHeight = abstractWidget.getHeight();
            if (isInsideViewport(event.x(), event.y())) {
                abstractWidget.setX(getX() + SCROLL_BAR_WIDTH_PADDING);
                abstractWidget.setY(currentY);
                abstractWidget.setWidth(getViewportWidth());
                if (abstractWidget.isHovered() && abstractWidget.mouseClicked(event, isDoubleClick)) {
                    return true;
                }
            }
            currentY += getHeightWithPadding(i, widgetHeight, widgets.size());
        }

        return false;
    }

    //TODO: make this better so that when i drag the button outside of the widget y it still works.
    // fix ishovered check basically
    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        // Let the scrollbar handle its own dragging first
        if (super.mouseDragged(event, dragX, dragY)) {
            return true;
        }

        int currentY = getY() + SCROLL_BAR_HEIGHT_PADDING - scrollOffsetY;
        List<ScrollableWidget<?>> widgets = getWidgets();
        for (int i = 0; i < widgets.size(); i++) {
            AbstractWidget abstractWidget = (AbstractWidget) widgets.get(i);
            int widgetHeight = abstractWidget.getHeight();
            if (isInsideViewport(event.x(), event.y())) {
                abstractWidget.setX(getX() + SCROLL_BAR_WIDTH_PADDING);
                abstractWidget.setY(currentY);
                abstractWidget.setWidth(getViewportWidth());
                if (abstractWidget.isHovered() && abstractWidget.mouseDragged(event, dragX, dragY)) {
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

        List<ScrollableWidget<?>> widgets = getWidgets();
        for (int i = 0; i < widgets.size(); i++) {
            AbstractWidget abstractWidget = (AbstractWidget) widgets.get(i);
            int widgetHeight = abstractWidget.getHeight();
            if (isInsideViewport(event.x(), event.y())) {
                abstractWidget.setX(getX() + SCROLL_BAR_WIDTH_PADDING);
                abstractWidget.setY(currentY);
                abstractWidget.setWidth(getViewportWidth());
                if (abstractWidget.isHovered() && abstractWidget.mouseReleased(event)) {
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

        List<ScrollableWidget<?>> widgets = getWidgets();
        for (int i = 0; i < widgets.size(); i++) {
            AbstractWidget abstractWidget = (AbstractWidget) widgets.get(i);
            int widgetHeight = abstractWidget.getHeight();
            if (isInsideViewport(mouseX, mouseY)) {
                abstractWidget.setX(getX() + SCROLL_BAR_WIDTH_PADDING);
                abstractWidget.setY(currentY);
                abstractWidget.setWidth(getViewportWidth());
                if (abstractWidget.isHovered() && abstractWidget.mouseScrolled(mouseX, mouseY, deltaX, deltaY)) {
                    return true;
                }
            }
            currentY += getHeightWithPadding(i, widgetHeight, widgets.size());
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    private boolean isInsideViewport(double x, double y) {
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