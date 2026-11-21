/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.wynntils.core.components.Services;
import com.wynntils.screens.maps.managers.ProviderManagementScreen;
import com.wynntils.screens.maps.managers.type.ProviderType;
import com.wynntils.screens.maps.managers.widgets.ProviderHeaderWidget;
import com.wynntils.screens.maps.managers.widgets.ScrollBarWidget;
import com.wynntils.services.mapdata.providers.MapDataProviderInfo;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.utils.StringUtils;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ProviderScrollBarWidget extends ScrollBarWidget {
    private static final int WIDGET_HEIGHT_PADDING = 4;

    private final ProviderManagementScreen parent;

    private List<ProviderManagerEntryWidget> registeredWidgets = new ArrayList<>();
    private final Map<ProviderType, ProviderHeaderWidget> providerHeaders = new EnumMap<>(ProviderType.class);

    public ProviderScrollBarWidget(int x, int y, int width, int height, ProviderManagementScreen parent) {
        super(x, y, width, height);
        this.parent = parent;

        for (ProviderType providerType : ProviderType.values()) {
            providerHeaders.put(
                    providerType, new ProviderHeaderWidget(Component.literal(providerType.getDisplayName())));
        }

        populateWidgets();
    }

    public void populateWidgets() {
        registeredWidgets = new ArrayList<>();

        for (Map.Entry<String, MapDataProviderInfo> entry :
                Services.MapData.getProviderInfos().entrySet()) {
            if (entry.getValue().provider() instanceof BuiltInProvider builtInProvider) {
                registeredWidgets.add(ProviderManagerEntryWidget.createBuiltInProviderEntry(builtInProvider, parent));
            }
        }

        Services.MapData.getJsonProviderInfos().forEach((providerId, jsonProviderInfo) -> {
            registeredWidgets.add(ProviderManagerEntryWidget.createJsonProviderEntry(
                    jsonProviderInfo, ProviderType.fromJsonProviderType(jsonProviderInfo.providerType()), parent));
        });

        updateWidgetPositions();
    }

    @Override
    protected int getWidgetHeightPadding() {
        return WIDGET_HEIGHT_PADDING;
    }

    private int getHeightWithPadding(int index, int widgetHeight, int totalWidgets) {
        return widgetHeight + (index < totalWidgets - 1 ? getWidgetHeightPadding() : 0);
    }

    @Override
    public List<AbstractWidget> getWidgets() {
        boolean isSearching = !parent.providerSearchWidget.getTextBoxInput().isBlank();

        List<AbstractWidget> layout = new ArrayList<>();

        for (ProviderType providerType : ProviderType.values()) {
            List<ProviderManagerEntryWidget> inProviderType = registeredWidgets.stream()
                    .filter(widget -> widget.getProviderType() == providerType)
                    .filter(widget ->
                            !isSearching || searchMatches(widget.getMessage().getString()))
                    .toList();

            if (inProviderType.isEmpty()) continue;

            layout.add(providerHeaders.get(providerType));
            layout.addAll(inProviderType);
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

        List<AbstractWidget> widgets = getWidgets();
        for (int i = 0; i < widgets.size(); i++) {
            AbstractWidget abstractWidget = widgets.get(i);
            int widgetHeight = abstractWidget.getHeight();

            if (currentY + widgetHeight >= viewportTop && currentY <= viewportBottom) {
                abstractWidget.visible = true;
                abstractWidget.setX(viewportLeft);
                abstractWidget.setY(currentY);
                abstractWidget.setWidth(getViewportWidth());
            } else {
                abstractWidget.visible = false;
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

        if (isInsideViewport(event.x(), event.y())) {
            for (AbstractWidget abstractWidget : getWidgets()) {
                if (abstractWidget.isHovered() && abstractWidget.mouseClicked(event, isDoubleClick)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        super.mouseReleased(event);

        if (isInsideViewport(event.x(), event.y())) {
            for (AbstractWidget abstractWidget : getWidgets()) {
                if (abstractWidget.isHovered() && abstractWidget.mouseReleased(event)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (super.mouseDragged(event, dragX, dragY)) {
            return true;
        }

        if (isInsideViewport(event.x(), event.y())) {
            for (AbstractWidget abstractWidget : getWidgets()) {
                if (abstractWidget.isHovered() && abstractWidget.mouseDragged(event, dragX, dragY)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (!isInsideViewport(mouseX, mouseY)) {
            return false;
        }

        for (AbstractWidget abstractWidget : getWidgets()) {
            if (abstractWidget.isHovered() && abstractWidget.mouseScrolled(mouseX, mouseY, deltaX, deltaY)) {
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    private boolean searchMatches(String name) {
        return StringUtils.partialMatch(name, parent.providerSearchWidget.getTextBoxInput());
    }
}
