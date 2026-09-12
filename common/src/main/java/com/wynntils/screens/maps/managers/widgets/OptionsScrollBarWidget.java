/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets;

import com.wynntils.screens.maps.managers.type.OptionCategory;
import com.wynntils.screens.maps.managers.widgets.options.AbstractOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.CategoryHeaderWidget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;

public class OptionsScrollBarWidget extends ScrollBarWidget {
    private static final int WIDGET_HEIGHT_PADDING = 4;

    private final List<AbstractOptionWidget<?>> registeredWidgets = new ArrayList<>();
    private final Map<OptionCategory, CategoryHeaderWidget> categoryHeaders = new EnumMap<>(OptionCategory.class);

    public OptionsScrollBarWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
        for (OptionCategory category : OptionCategory.values()) {
            categoryHeaders.put(category, new CategoryHeaderWidget(category));
        }
    }

    public void addWidget(AbstractOptionWidget<?> widget) {
        registeredWidgets.add(widget);
        updateWidgetPositions();
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
                    .filter(widget -> widget.display)
                    .filter(widget -> widget.getCategory() == category)
                    .toList();

            if (inCategory.isEmpty()) continue;

            layout.add(categoryHeaders.get(category));
            layout.addAll(inCategory);
        }

        return layout;
    }

    @Override
    protected void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        getWidgets().forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    public void updateWidgetPositions() {
        int viewportTop = getY() + SCROLL_BAR_HEIGHT_PADDING;
        int viewportBottom = viewportTop + getViewportHeight();
        int viewportLeft = getX() + SCROLL_BAR_WIDTH_PADDING;

        int currentY = getY() + SCROLL_BAR_HEIGHT_PADDING - scrollOffsetY;

        List<AbstractOptionWidget<?>> widgets = getWidgets();
        for (int i = 0; i < widgets.size(); i++) {
            AbstractOptionWidget<?> abstractOptionWidget = widgets.get(i);
            int widgetHeight = abstractOptionWidget.getHeight();
            if (currentY + widgetHeight >= viewportTop && currentY <= viewportBottom) {
                abstractOptionWidget.visible = true;
                abstractOptionWidget.setX(viewportLeft);
                abstractOptionWidget.setY(currentY);
                abstractOptionWidget.setWidth(getViewportWidth());
            } else {
                abstractOptionWidget.visible = false;
            }
            currentY += getHeightWithPadding(i, widgetHeight, widgets.size());
        }
    }

    @Override
    protected void onScrollOffsetChanged() {
        updateWidgetPositions();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (super.mouseClicked(event, isDoubleClick)) {
            return true;
        }

        for (AbstractOptionWidget<?> abstractOptionWidget : getWidgets()) {
            if (isInsideViewport(event.x(), event.y())
                    && abstractOptionWidget.isHovered()
                    && abstractOptionWidget.mouseClicked(event, isDoubleClick)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        for (AbstractOptionWidget<?> abstractOptionWidget : getWidgets()) {
            if (isInsideViewport(event.x(), event.y())
                    && abstractOptionWidget.isHovered()
                    && abstractOptionWidget.mouseReleased(event)) {
                return true;
            }
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (super.mouseDragged(event, dragX, dragY)) {
            return true;
        }

        for (AbstractOptionWidget<?> abstractOptionWidget : getWidgets()) {
            if (isInsideViewport(event.x(), event.y())
                    && abstractOptionWidget.isHovered()
                    && abstractOptionWidget.mouseDragged(event, dragX, dragY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (!isInsideViewport(mouseX, mouseY)) {
            return false;
        }

        for (AbstractOptionWidget<?> abstractOptionWidget : getWidgets()) {
            if (isInsideViewport(mouseX, mouseY)) {
                if (abstractOptionWidget.isHovered()
                        && abstractOptionWidget.mouseScrolled(mouseX, mouseY, deltaX, deltaY)) {
                    return true;
                }
            }
        }

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }
}
