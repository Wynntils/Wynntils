/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.screens.maps.managers.ProviderManagementScreen;
import com.wynntils.screens.maps.managers.type.ProviderType;
import com.wynntils.screens.maps.managers.widgets.ScrollBarWidget;
import com.wynntils.screens.maps.managers.widgets.providermanager.AbstractProviderWidget;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.services.mapdata.providers.json.JsonProvider;
import com.wynntils.services.mapdata.providers.json.JsonProviderInfo;
import com.wynntils.services.mapdata.providers.type.MapDataProvider;
import com.wynntils.utils.StringUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ProviderScrollBarWidget extends ScrollBarWidget {
    private static final int WIDGET_HEIGHT_PADDING = 4;

    private final ProviderManagementScreen parent;

    private List<AbstractProviderWidget> registeredWidgets = new ArrayList<>();
    private final Map<ProviderType, ProviderHeaderWidget> providerHeaders = new EnumMap<>(ProviderType.class);

    public ProviderScrollBarWidget(int x, int y, int width, int height, ProviderManagementScreen parent) {
        super(x, y, width, height);
        this.parent = parent;

        for (ProviderType providerType : ProviderType.values()) {
            providerHeaders.put(providerType, new ProviderHeaderWidget(providerType));
        }

        populateWidgets();
    }


    public void populateWidgets() {
        registeredWidgets = new ArrayList<>();

        for (MapDataProvider provider : Services.MapData.getProviders().toList()) {
            if (provider instanceof BuiltInProvider builtInProvider) {
                registeredWidgets.add(ProviderEntryWidget.createBuiltInProviderEntry(
                        builtInProvider,
                        parent));
            }
        }

        Services.MapData.getJsonProviderInfos().forEach((providerId, jsonProviderInfo) -> {
            registeredWidgets.add(ProviderEntryWidget.createJsonProviderEntry(
                    jsonProviderInfo,
                    ProviderType.fromJsonProviderType(jsonProviderInfo.providerType()),
                    parent));
        });

        updateWidgetPositions();
    }


    public List<AbstractProviderWidget> getRegisteredWidgets() {
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
    public List<AbstractProviderWidget> getWidgets() {
        boolean isSearching = !parent.providerSearchWidget.getTextBoxInput().isBlank();

        List<AbstractProviderWidget> layout = new ArrayList<>();

        for (ProviderType providerType : ProviderType.values()) {
            List<AbstractProviderWidget> inProviderType = registeredWidgets.stream()
                    .filter(widget -> widget.display)
                    .filter(widget -> widget.getProviderType() == providerType)
                    .filter(widget -> !isSearching || searchMatches(widget.getMessage().getString()))
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

        List<AbstractProviderWidget> widgets = getWidgets();
        for (int i = 0; i < widgets.size(); i++) {
            AbstractProviderWidget abstractProviderWidget = widgets.get(i);
            int widgetHeight = abstractProviderWidget.getHeight();
            if (currentY + widgetHeight >= viewportTop && currentY <= viewportBottom) {
                abstractProviderWidget.visible = true;
                abstractProviderWidget.setX(viewportLeft);
                abstractProviderWidget.setY(currentY);
                abstractProviderWidget.setWidth(getViewportWidth());
            } else {
                abstractProviderWidget.visible = false;
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
            for (AbstractProviderWidget abstractProviderWidget : getWidgets()) {
                if (abstractProviderWidget.isHovered()
                        && abstractProviderWidget.mouseClicked(event, isDoubleClick)) {
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
            for (AbstractProviderWidget abstractProviderWidget : getWidgets()) {
                if (abstractProviderWidget.isHovered()
                        && abstractProviderWidget.mouseReleased(event)) {
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
            for (AbstractProviderWidget abstractProviderWidget : getWidgets()) {
                if (abstractProviderWidget.isHovered()
                        && abstractProviderWidget.mouseDragged(event, dragX, dragY)) {
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

        for (AbstractProviderWidget abstractProviderWidget : getWidgets()) {
                if (abstractProviderWidget.isHovered()
                        && abstractProviderWidget.mouseScrolled(mouseX, mouseY, deltaX, deltaY)) {
                    return true;
                }
        }

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    private boolean searchMatches(String name) {
        return StringUtils.partialMatch(name, parent.providerSearchWidget.getTextBoxInput());
    }
}
