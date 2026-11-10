/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.iconselection;

import com.wynntils.core.components.Services;
import com.wynntils.screens.maps.managers.IconSelectionScreen;
import com.wynntils.screens.maps.managers.widgets.ProviderEntryWidget;
import com.wynntils.screens.maps.managers.widgets.ProviderHeaderWidget;
import com.wynntils.screens.maps.managers.widgets.ScrollBarWidget;
import com.wynntils.utils.StringUtils;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class IconSelectionProviderScrollBarWidget extends ScrollBarWidget {
    private static final int WIDGET_HEIGHT_PADDING = 4;

    private final IconSelectionScreen parent;

    private final Map<ProviderGroup, List<ProviderEntryWidget>> registeredWidgets = new EnumMap<>(ProviderGroup.class);
    private final Map<ProviderGroup, ProviderHeaderWidget> providerHeaders = new EnumMap<>(ProviderGroup.class);

    private String filter = "";

    public IconSelectionProviderScrollBarWidget(int x, int y, int width, int height, IconSelectionScreen parent) {
        super(x, y, width, height);
        this.parent = parent;

        for (ProviderGroup group : ProviderGroup.values()) {
            providerHeaders.put(group, new ProviderHeaderWidget(Component.translatable(group.headerKey)));
        }

        populateWidgets();
    }

    public void populateWidgets() {
        registeredWidgets.clear();

        addEntry(
                ProviderGroup.ICONS,
                IconSelectionScreen.PROVIDER_ALL,
                Component.translatable("screens.wynntils.map.iconSelectionScreen.provider.all"));
        addEntry(
                ProviderGroup.ICONS,
                IconSelectionScreen.PROVIDER_BUILT_IN,
                Component.translatable("screens.wynntils.map.iconSelectionScreen.provider.builtIn"));
        addEntry(
                ProviderGroup.ICONS,
                IconSelectionScreen.PROVIDER_CUSTOM,
                Component.translatable("screens.wynntils.map.iconSelectionScreen.provider.custom"));

        Services.MapData.getJsonProviderInfos().values().stream()
                .sorted()
                .filter(info -> Services.MapData.getProviderFromId(info.completeId())
                        .map(provider -> provider.getIcons().findAny().isPresent())
                        .orElse(false))
                .forEach(info -> addEntry(ProviderGroup.JSON, info.completeId(), Component.literal(info.providerId())));

        updateWidgetPositions();
    }

    public void setFilter(String filter) {
        this.filter = filter == null ? "" : filter;
        scrollOffsetY = 0;
        updateWidgetPositions();
    }

    public void syncSelection() {
        String selected = parent.getSelectedProviderId();

        boolean exists = registeredWidgets.values().stream()
                .flatMap(List::stream)
                .anyMatch(entry -> entry.getProviderId().equals(selected));

        if (!exists) {
            parent.setSelectedProviderId(IconSelectionScreen.PROVIDER_ALL);
        }
    }

    private void addEntry(ProviderGroup group, String providerId, Component name) {
        registeredWidgets
                .computeIfAbsent(group, key -> new ArrayList<>())
                .add(new ProviderEntryWidget(
                        providerId, name, parent::getSelectedProviderId, parent::setSelectedProviderId));
    }

    @Override
    protected int getWidgetHeightPadding() {
        return WIDGET_HEIGHT_PADDING;
    }

    @Override
    protected List<AbstractWidget> getWidgets() {
        List<AbstractWidget> layout = new ArrayList<>();

        for (ProviderGroup group : ProviderGroup.values()) {
            List<ProviderEntryWidget> matching = registeredWidgets.getOrDefault(group, List.of()).stream()
                    .filter(widget -> filter.isBlank()
                            || StringUtils.partialMatch(widget.getMessage().getString(), filter))
                    .toList();

            if (matching.isEmpty()) continue;

            layout.add(providerHeaders.get(group));
            layout.addAll(matching);
        }

        return layout;
    }

    @Override
    protected void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        getWidgets().forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (super.mouseClicked(event, isDoubleClick)) {
            return true;
        }

        if (isInsideViewport(event.x(), event.y())) {
            for (AbstractWidget widget : getWidgets()) {
                if (!widget.visible || !widget.isMouseOver(event.x(), event.y())) continue;

                if (widget.mouseClicked(event, isDoubleClick)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void onScrollOffsetChanged() {
        updateWidgetPositions();
    }

    public void updateWidgetPositions() {
        int viewportTop = getY() + SCROLL_BAR_HEIGHT_PADDING;
        int viewportBottom = viewportTop + getViewportHeight();
        int viewportLeft = getX() + SCROLL_BAR_WIDTH_PADDING;
        int currentY = viewportTop - scrollOffsetY;

        for (AbstractWidget widget : getWidgets()) {
            int widgetHeight = widget.getHeight();

            if (currentY + widgetHeight >= viewportTop && currentY <= viewportBottom) {
                widget.visible = true;
                widget.setX(viewportLeft);
                widget.setY(currentY);
                widget.setWidth(getViewportWidth());
            } else {
                widget.visible = false;
            }

            currentY += widgetHeight + getWidgetHeightPadding();
        }
    }

    private enum ProviderGroup {
        ICONS("screens.wynntils.map.iconSelectionScreen.provider.header.icons"),
        JSON("screens.wynntils.map.iconSelectionScreen.provider.header.json");

        private final String headerKey;

        ProviderGroup(String headerKey) {
            this.headerKey = headerKey;
        }
    }
}
