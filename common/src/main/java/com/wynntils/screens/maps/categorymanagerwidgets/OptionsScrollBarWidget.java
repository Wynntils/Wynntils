package com.wynntils.screens.maps.categorymanagerwidgets;

import com.wynntils.mc.event.MouseScrollEvent;
import com.wynntils.screens.maps.CategoryManagementScreen;
import com.wynntils.screens.maps.type.OptionCategory;
import com.wynntils.screens.maps.type.ScrollableWidget;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;

public class OptionsScrollBarWidget extends ScrollBarWidget {
    private final List<ScrollableWidget<?>> registeredWidgets = new ArrayList<>();

    public OptionsScrollBarWidget(int x, int y, int width, int height, CategoryManagementScreen parent) {
        super(x, y, width, height, parent);
    }

    public void addWidget(ScrollableWidget<?> widget) {
        registeredWidgets.add(widget);
    }

    public void clearWidgets() {
        registeredWidgets.clear();
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

        for (ScrollableWidget<?> widget : getWidgets()) {
            AbstractWidget aw = (AbstractWidget) widget;
            int widgetHeight = aw.getHeight();
            if (currentY + widgetHeight >= viewportTop && currentY <= viewportBottom) {
                aw.setX(viewportLeft);
                aw.setY(currentY);
                aw.setWidth(contentWidth);
                aw.render(guiGraphics, mouseX, mouseY, partialTick);
            }
            currentY += widgetHeight;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (super.mouseClicked(event, isDoubleClick)) {
            return true;
        }

        int viewportTop = getY() + SCROLL_BAR_HEIGHT_PADDING;
        int viewportBottom = viewportTop + getViewportHeight();
        int contentWidth = getViewportWidth();
        int viewportLeft = getX() + SCROLL_BAR_WIDTH_PADDING;
        int viewportRight = viewportLeft + contentWidth;

        // Ignore clicks outside the viewport area
        if (!(event.x() >= viewportLeft && event.x() < viewportRight &&
                event.y() >= viewportTop && event.y() < viewportBottom)) {
            return false;
        }

        int currentY = viewportTop - scrollOffsetY;
        for (ScrollableWidget<?> widget : getWidgets()) {
            AbstractWidget aw = (AbstractWidget) widget;
            int widgetHeight = aw.getHeight();
            if (currentY + widgetHeight >= viewportTop && currentY <= viewportBottom) {
                aw.setX(viewportLeft);
                aw.setY(currentY);
                aw.setWidth(contentWidth);
                if (aw.mouseClicked(event, isDoubleClick)) {
                    return true;
                }
            }
            currentY += widgetHeight;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        // Let the scrollbar handle its own dragging first
        if (super.mouseDragged(event, dragX, dragY)) {
            return true;
        }

        int viewportTop = getY() + SCROLL_BAR_HEIGHT_PADDING;
        int viewportBottom = viewportTop + getViewportHeight();
        int contentWidth = getViewportWidth();
        int viewportLeft = getX() + SCROLL_BAR_WIDTH_PADDING;
        int viewportRight = viewportLeft + contentWidth;

        // Ignore drags that started or happen outside the viewport
        if (!(event.x() >= viewportLeft && event.x() < viewportRight &&
                event.y() >= viewportTop && event.y() < viewportBottom)) {
            return false;
        }

        int currentY = viewportTop - scrollOffsetY;
        for (ScrollableWidget<?> widget : getWidgets()) {
            AbstractWidget aw = (AbstractWidget) widget;
            int widgetHeight = aw.getHeight();
            if (currentY + widgetHeight >= viewportTop && currentY <= viewportBottom) {
                aw.setX(viewportLeft);
                aw.setY(currentY);
                aw.setWidth(contentWidth);
                if (aw.mouseDragged(event, dragX, dragY)) {
                    return true;
                }
            }
            currentY += widgetHeight;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        // Let the scrollbar handle release first
        if (super.mouseReleased(event)) {
            return true;
        }

        int viewportTop = getY() + SCROLL_BAR_HEIGHT_PADDING;
        int viewportBottom = viewportTop + getViewportHeight();
        int contentWidth = getViewportWidth();
        int viewportLeft = getX() + SCROLL_BAR_WIDTH_PADDING;
        int viewportRight = viewportLeft + contentWidth;

        // Only forward if the release is inside the viewport
        if (!(event.x() >= viewportLeft && event.x() < viewportRight &&
                event.y() >= viewportTop && event.y() < viewportBottom)) {
            return false;
        }

        int currentY = viewportTop - scrollOffsetY;
        for (ScrollableWidget<?> widget : getWidgets()) {
            AbstractWidget aw = (AbstractWidget) widget;
            int widgetHeight = aw.getHeight();
            if (currentY + widgetHeight >= viewportTop && currentY <= viewportBottom) {
                aw.setX(viewportLeft);
                aw.setY(currentY);
                aw.setWidth(contentWidth);
                if (aw.mouseReleased(event)) {
                    return true;
                }
            }
            currentY += widgetHeight;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        // Let the scrollbar handle scrolling first (e.g., move the thumb)
        if (super.mouseScrolled(mouseX, mouseY, deltaX, deltaY)) {
            return true;
        }

        int viewportTop = getY() + SCROLL_BAR_HEIGHT_PADDING;
        int viewportBottom = viewportTop + getViewportHeight();
        int contentWidth = getViewportWidth();
        int viewportLeft = getX() + SCROLL_BAR_WIDTH_PADDING;
        int viewportRight = viewportLeft + contentWidth;

        // Only forward if the scroll occurs inside the viewport
        if (!(mouseX >= viewportLeft && mouseX < viewportRight &&
                mouseY >= viewportTop && mouseY < viewportBottom)) {
            return false;
        }

        int currentY = viewportTop - scrollOffsetY;
        for (ScrollableWidget<?> widget : getWidgets()) {
            AbstractWidget aw = (AbstractWidget) widget;
            int widgetHeight = aw.getHeight();
            if (currentY + widgetHeight >= viewportTop && currentY <= viewportBottom) {
                aw.setX(viewportLeft);
                aw.setY(currentY);
                aw.setWidth(contentWidth);
                if (aw.mouseScrolled(mouseX, mouseY, deltaX, deltaY)) {
                    return true;
                }
            }
            currentY += widgetHeight;
        }
        return false;
    }
}