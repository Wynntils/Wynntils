/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.maps.managers.ProviderManagementScreen;
import com.wynntils.screens.maps.managers.widgets.ScrollBarWidget;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.providers.type.MapDataProvider;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.services.mapdata.type.MapIcon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ProviderInfoScrollBarWidget extends ScrollBarWidget implements TooltipProvider {
    private static final int WIDGET_HEIGHT_PADDING = 4;

    private final ProviderManagementScreen parent;
    private Mode selectedMode = Mode.FEATURES;

    private final ProviderInfoScrollBarSelectorWidget featuresButton;
    private final ProviderInfoScrollBarSelectorWidget categoriesButton;
    private final ProviderInfoScrollBarSelectorWidget iconsButton;

    private List<AbstractWidget> registeredWidgets = new ArrayList<>();

    public ProviderInfoScrollBarWidget(int x, int y, int width, int height, ProviderManagementScreen parent) {
        super(x, y, width, height);
        this.parent = parent;

        int buttonWidth = (this.width - 6) / 3;
        featuresButton = new ProviderInfoScrollBarSelectorWidget(
                StyledText.fromComponent(Component.translatable(
                        "screens.wynntils.map.managers.providerManager.providerInfoScrollBarWidget.features.text")),
                Mode.FEATURES,
                x + 3,
                y - 2 - 15,
                buttonWidth,
                18,
                parent);

        categoriesButton = new ProviderInfoScrollBarSelectorWidget(
                StyledText.fromComponent(Component.translatable(
                        "screens.wynntils.map.managers.providerManager.providerInfoScrollBarWidget.categories.text")),
                Mode.CATEGORIES,
                x + 3 + buttonWidth - 1,
                y - 2 - 15,
                buttonWidth + 2,
                18,
                parent);

        iconsButton = new ProviderInfoScrollBarSelectorWidget(
                StyledText.fromComponent(Component.translatable(
                        "screens.wynntils.map.managers.providerManager.providerInfoScrollBarWidget.icons.text")),
                Mode.ICONS,
                x + 3 + buttonWidth * 2,
                y - 2 - 15,
                buttonWidth,
                18,
                parent);

        populateWidgets();
    }

    public void populateWidgets() {
        registeredWidgets = new ArrayList<>();
        scrollOffsetY = 0;
        String providerId = parent.getSelectedProviderId();
        Optional<MapDataProvider> providerOpt = Services.MapData.getProviderFromId(providerId);

        if (providerOpt.isEmpty()) return;

        MapDataProvider provider = providerOpt.get();

        if (selectedMode == Mode.FEATURES) {
            for (MapFeature<?> mapFeature : provider.getFeatures().toList()) {
                registeredWidgets.add(new FeatureEntryWidget(0, 0, 0, 28, mapFeature));
            }
        } else if (selectedMode == Mode.CATEGORIES) {
            for (MapCategory mapCategory : provider.getCategories().toList()) {
                registeredWidgets.add(new CategoryEntryWidget(0, 0, 0, 28, mapCategory));
            }
        } else if (selectedMode == Mode.ICONS) {
            for (MapIcon mapIcon : provider.getIcons().toList()) {
                registeredWidgets.add(new IconEntryWidget(0, 0, 0, 28, mapIcon));
            }
        }

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
        return Collections.unmodifiableList(registeredWidgets);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        featuresButton.render(guiGraphics, mouseX, mouseY, partialTick);
        categoriesButton.render(guiGraphics, mouseX, mouseY, partialTick);
        iconsButton.render(guiGraphics, mouseX, mouseY, partialTick);

        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        getWidgets().forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public List<Component> getTooltipLines() {
        for (AbstractWidget widget : getWidgets()) {
            if (widget instanceof TooltipProvider tooltipProvider && widget.isHovered() && widget.visible) {
                return tooltipProvider.getTooltipLines();
            }
        }

        return List.of();
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
        if (featuresButton.isMouseOver(event.x(), event.y())) {
            return featuresButton.mouseClicked(event, isDoubleClick);
        } else if (categoriesButton.isMouseOver(event.x(), event.y())) {
            return categoriesButton.mouseClicked(event, isDoubleClick);
        } else if (iconsButton.isMouseOver(event.x(), event.y())) {
            return iconsButton.mouseClicked(event, isDoubleClick);
        }

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

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY)
                || featuresButton.isMouseOver(mouseX, mouseY)
                || categoriesButton.isMouseOver(mouseX, mouseY)
                || iconsButton.isMouseOver(mouseX, mouseY);
    }

    public Mode getSelectedMode() {
        return selectedMode;
    }

    public void setSelectedMode(Mode mode) {
        selectedMode = mode;
        populateWidgets();
    }

    public enum Mode {
        FEATURES,
        CATEGORIES,
        ICONS
    }
}
