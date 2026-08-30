/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers;

import com.google.common.collect.Lists;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.MainMapScreen;
import com.wynntils.screens.maps.managers.type.OptionCategory;
import com.wynntils.screens.maps.managers.type.OverrideType;
import com.wynntils.screens.maps.managers.widgets.CategorySearchWidget;
import com.wynntils.screens.maps.managers.widgets.CategoryTreeWidget;
import com.wynntils.screens.maps.managers.widgets.DeleteButtonWidget;
import com.wynntils.screens.maps.managers.widgets.InfoWidget;
import com.wynntils.screens.maps.managers.widgets.OptionsScrollBarWidget;
import com.wynntils.screens.maps.managers.widgets.OverrideSelectionWidget;
import com.wynntils.screens.maps.managers.widgets.ResetButtonWidget;
import com.wynntils.screens.maps.managers.widgets.SaveButtonWidget;
import com.wynntils.screens.maps.managers.widgets.options.AbstractOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.ColorOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.FloatSliderOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.IconOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.IntSliderOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.TextOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.TextShadowOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.ToggleOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.VisiblityOptionWidget;
import com.wynntils.services.mapdata.attributes.DefaultMapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapMarkerOptions;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class CategoryManagementScreen extends WynntilsScreen {
    private static final int WIDTH_OFFSET = 15;
    private static final int HEIGHT_OFFSET = 18;

    private final MainMapScreen previousScreen;
    private int offsetX;
    private int offsetY;

    public CategorySearchWidget categorySearchWidget;
    public CategoryTreeWidget categoryTreeWidget;

    public OverrideSelectionWidget overrideSelectionWidget;
    public InfoWidget infoWidget;
    public SaveButtonWidget saveButtonWidget;
    public ResetButtonWidget resetButtonWidget;
    public DeleteButtonWidget deleteButtonWidget;
    public OptionsScrollBarWidget optionsScrollBar;

    public IntSliderOptionWidget priorityOptionWidget;
    public IntSliderOptionWidget levelOptionWidget;

    public TextOptionWidget labelOptionWidget;
    public TextOptionWidget descriptionOptionWidget;
    public ColorOptionWidget labelColorOptionWidget;
    public TextShadowOptionWidget labelShadowOptionWidget;
    public VisiblityOptionWidget labelVisibilityOptionWidget;

    public IconOptionWidget iconOptionWidget;
    public ColorOptionWidget iconColorOptionWidget;
    public VisiblityOptionWidget iconVisibilityOptionWidget;

    public ToggleOptionWidget hasMarkerOptionWidget;

    public FloatSliderOptionWidget markerMinDistanceOptionWidget;
    public FloatSliderOptionWidget markerMaxDistanceOptionWidget;
    public FloatSliderOptionWidget markerFadeOptionWidget;

    public ColorOptionWidget markerBeaconColorOptionWidget;
    public ToggleOptionWidget markerHasLabelOptionWidget;
    public ToggleOptionWidget markerHasDistanceLabelOptionWidget;
    public ToggleOptionWidget markerHasIconOptionWidget;

    // endregion

    // region Area & Border Attributes

    public ColorOptionWidget fillColorOptionWidget;
    public ColorOptionWidget borderColorOptionWidget;
    public FloatSliderOptionWidget borderWidthOptionWidget;

    // endregion

    private String selectedCategory;
    private OverrideType selectedOverrideType = OverrideType.MAP_LOCATION_OVERRIDE;

    private CategoryManagementScreen(MainMapScreen previousScreen) {
        super(Component.literal("Category Management Screen"));
        this.previousScreen = previousScreen;
    }

    public static Screen create(MainMapScreen previousScreen) {
        return new CategoryManagementScreen(previousScreen);
    }

    // This makes it so when we open a color picker screen or an icon selection screen that the widgets do not get
    // cleared.
    @Override
    protected void rebuildWidgets() {}

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        super.rebuildWidgets();
    }

    @Override
    protected void doInit() {
        super.doInit();
        offsetX = (int) ((this.width - Texture.MANAGER_BACKGROUND.width()) / 2f);
        offsetY = (int) ((this.height - Texture.MANAGER_BACKGROUND.height()) / 2f);

        infoWidget = new InfoWidget(offsetX + WIDTH_OFFSET + 546 - 20, offsetY + HEIGHT_OFFSET, 20, 20, this);
        this.addRenderableWidget(infoWidget);

        categorySearchWidget = new CategorySearchWidget(
                offsetX + WIDTH_OFFSET, offsetY + HEIGHT_OFFSET, (text) -> categoryTreeWidget.filter(text), this);
        this.addRenderableWidget(categorySearchWidget);

        categoryTreeWidget =
                new CategoryTreeWidget(offsetX + WIDTH_OFFSET, offsetY + HEIGHT_OFFSET + 25, 200, 284 - 25, this);

        categoryTreeWidget.setCategories(
                Services.MapData.allPossibleCategories().toList());
        this.addRenderableWidget(categoryTreeWidget);

        // region Feature (common) Attributes

        priorityOptionWidget = new IntSliderOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.priority"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.priority.description"),
                OptionCategory.GENERAL,
                1,
                1000,
                MapAttributes::getPriority,
                this);

        levelOptionWidget = new IntSliderOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.level"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.level.description1")
                        .append("\n\n")
                        .append(Component.translatable(
                                "screens.wynntils.map.managers.categoryManager.level.description2"))
                        .append("\n\n")
                        .append(Component.translatable(
                                "screens.wynntils.map.managers.categoryManager.level.description3")),
                OptionCategory.GENERAL,
                0,
                Models.CombatXp.MAX_LEVEL,
                MapAttributes::getLevel,
                this);

        // endregion

        // region Label Attributes

        labelOptionWidget = new TextOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.label"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.label.description"),
                OptionCategory.LABEL,
                MapAttributes::getLabel,
                this);

        descriptionOptionWidget = new TextOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.description"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.description.description"),
                OptionCategory.LABEL,
                MapAttributes::getDescription,
                this);

        labelColorOptionWidget = new ColorOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.labelColor"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.labelColor.description"),
                OptionCategory.LABEL,
                MapAttributes::getLabelColor,
                this);

        labelShadowOptionWidget = new TextShadowOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.labelShadow"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.labelShadow.description"),
                OptionCategory.LABEL,
                MapAttributes::getLabelShadow);

        labelVisibilityOptionWidget = new VisiblityOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.labelVisibility"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.labelVisibility.description"),
                OptionCategory.LABEL,
                DefaultMapAttributes.LABEL_NEVER,
                DefaultMapAttributes.LABEL_ALWAYS,
                Component.translatable("screens.wynntils.map.managers.categoryManager.labelVisibilityMin"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.labelVisibilityMin.description"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.labelVisibilityMax"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.labelVisibilityMax.description"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.labelVisibilityFade"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.labelVisibilityFade.description"),
                MapAttributes::getLabelVisibility,
                this);

        // endregion

        // region Icon Attributes

        iconOptionWidget = new IconOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.icon"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.icon.description"),
                OptionCategory.ICON,
                MapAttributes::getIconId);

        iconColorOptionWidget = new ColorOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconColor"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconColor.description"),
                OptionCategory.ICON,
                MapAttributes::getIconColor,
                this);

        iconVisibilityOptionWidget = new VisiblityOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconVisibility"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconVisibility.description"),
                OptionCategory.ICON,
                DefaultMapAttributes.ICON_NEVER,
                DefaultMapAttributes.ICON_ALWAYS,
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconVisibilityMin"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconVisibilityMin.description"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconVisibilityMax"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconVisibilityMax.description"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconVisibilityFade"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconVisibilityFade.description"),
                MapAttributes::getIconVisibility,
                this);

        // endregion

        // region MapLocation Marker Attributes

        hasMarkerOptionWidget = new ToggleOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.hasMarker"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.hasMarker.description1")
                        .append("\n\n")
                        .append(Component.translatable(
                                "screens.wynntils.map.managers.categoryManager.hasMarker.description2")),
                OptionCategory.MARKER,
                MapAttributes::getHasMarker);

        markerMinDistanceOptionWidget = new FloatSliderOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.markerMinDistance"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.markerMinDistance.description"),
                OptionCategory.MARKER,
                0f,
                15000f,
                attrs -> attrs.getMarkerOptions().flatMap(MapMarkerOptions::getMinDistance),
                this);

        markerMaxDistanceOptionWidget = new FloatSliderOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.markerMaxDistance"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.markerMaxDistance.description"),
                OptionCategory.MARKER,
                0f,
                15000f,
                attrs -> attrs.getMarkerOptions().flatMap(MapMarkerOptions::getMaxDistance),
                this);

        markerFadeOptionWidget = new FloatSliderOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.markerFade"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.markerFade.description"),
                OptionCategory.MARKER,
                0f,
                100f,
                attrs -> attrs.getMarkerOptions().flatMap(MapMarkerOptions::getFade),
                this);

        markerBeaconColorOptionWidget = new ColorOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.markerBeaconColor"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.markerBeaconColor.description"),
                OptionCategory.MARKER,
                attrs -> attrs.getMarkerOptions().flatMap(MapMarkerOptions::getBeaconColor),
                this);

        markerHasLabelOptionWidget = new ToggleOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.markerHasLabel"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.markerHasLabel.description"),
                OptionCategory.MARKER,
                attrs -> attrs.getMarkerOptions().flatMap(MapMarkerOptions::getHasLabel));

        markerHasDistanceLabelOptionWidget = new ToggleOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.markerHasDistanceLabel"),
                Component.translatable(
                        "screens.wynntils.map.managers.categoryManager.markerHasDistanceLabel.description"),
                OptionCategory.MARKER,
                attrs -> attrs.getMarkerOptions().flatMap(MapMarkerOptions::getHasDistanceLabel));

        markerHasIconOptionWidget = new ToggleOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.markerHasIcon"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.markerHasIcon.description"),
                OptionCategory.MARKER,
                attrs -> attrs.getMarkerOptions().flatMap(MapMarkerOptions::getHasIcon));

        // endregion

        // region Area & Border Attributes

        fillColorOptionWidget = new ColorOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.fillColor"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.fillColor.description"),
                OptionCategory.AREA_BORDER,
                MapAttributes::getFillColor,
                this);

        borderColorOptionWidget = new ColorOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.borderColor"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.borderColor.description"),
                OptionCategory.AREA_BORDER,
                MapAttributes::getBorderColor,
                this);

        borderWidthOptionWidget = new FloatSliderOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.borderWidth"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.borderWidth.description"),
                OptionCategory.AREA_BORDER,
                0f,
                10f,
                MapAttributes::getBorderWidth,
                this);

        // endregion

        optionsScrollBar = new OptionsScrollBarWidget(
                offsetX + WIDTH_OFFSET + 200 + 5, offsetY + HEIGHT_OFFSET + 25, 341, 284 - 50);

        optionsScrollBar.addWidget(priorityOptionWidget);
        optionsScrollBar.addWidget(levelOptionWidget);

        optionsScrollBar.addWidget(labelOptionWidget);
        optionsScrollBar.addWidget(descriptionOptionWidget);
        optionsScrollBar.addWidget(labelColorOptionWidget);
        optionsScrollBar.addWidget(labelShadowOptionWidget);
        optionsScrollBar.addWidget(labelVisibilityOptionWidget);

        optionsScrollBar.addWidget(iconOptionWidget);
        optionsScrollBar.addWidget(iconColorOptionWidget);
        optionsScrollBar.addWidget(iconVisibilityOptionWidget);

        optionsScrollBar.addWidget(hasMarkerOptionWidget);
        optionsScrollBar.addWidget(markerMinDistanceOptionWidget);
        optionsScrollBar.addWidget(markerMaxDistanceOptionWidget);
        optionsScrollBar.addWidget(markerFadeOptionWidget);
        optionsScrollBar.addWidget(markerBeaconColorOptionWidget);
        optionsScrollBar.addWidget(markerHasLabelOptionWidget);
        optionsScrollBar.addWidget(markerHasDistanceLabelOptionWidget);
        optionsScrollBar.addWidget(markerHasIconOptionWidget);

        optionsScrollBar.addWidget(fillColorOptionWidget);
        optionsScrollBar.addWidget(borderColorOptionWidget);
        optionsScrollBar.addWidget(borderWidthOptionWidget);

        this.addRenderableWidget(optionsScrollBar);

        overrideSelectionWidget =
                new OverrideSelectionWidget(offsetX + WIDTH_OFFSET + 200 + 5, offsetY + HEIGHT_OFFSET, 150, 20, this);
        this.addRenderableWidget(overrideSelectionWidget);

        saveButtonWidget = new SaveButtonWidget(
                offsetX + WIDTH_OFFSET + 200 + 5, offsetY + HEIGHT_OFFSET + 284 - 20, 103, 20, this);
        this.addRenderableWidget(saveButtonWidget);

        resetButtonWidget = new ResetButtonWidget(
                offsetX + WIDTH_OFFSET + 200 + 5 + (103 + 16) * 1, offsetY + HEIGHT_OFFSET + 284 - 20, 103, 20, this);
        this.addRenderableWidget(resetButtonWidget);

        deleteButtonWidget = new DeleteButtonWidget(
                offsetX + WIDTH_OFFSET + 200 + 5 + (103 + 16) * 2, offsetY + HEIGHT_OFFSET + 284 - 20, 103, 20, this);
        this.addRenderableWidget(deleteButtonWidget);

        updateMenu();
        updateOptionWidgetsVisibility();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(guiGraphics);

        renderables.forEach(renderable -> renderable.render(guiGraphics, mouseX, mouseY, partialTick));
        renderTooltips(guiGraphics, mouseX, mouseY);
    }

    private void renderBackgroundTexture(GuiGraphics guiGraphics) {
        RenderUtils.drawTexturedRect(guiGraphics, Texture.MANAGER_BACKGROUND, offsetX, offsetY);
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        // Handle overrideSelectionWidget first to prevent overlap issues with optionsScrollBar
        if (overrideSelectionWidget.mouseClicked(event, isDoubleClick)) {
            return true;
        }

        for (AbstractOptionWidget<?> widget : optionsScrollBar.getRegisteredWidgets()) {
            if (widget.isMouseOverTextBox(event.x(), event.y())) {
                setFocusedTextInput(widget.getTextInputBoxWidget(event.x(), event.y()));
            }
        }

        TextInputBoxWidget focused = getFocusedTextInput();
        boolean handled = super.doMouseClicked(event, isDoubleClick);

        if (focused != null && !focused.isMouseOver(event.x(), event.y())) {
            setFocusedTextInput(null);
        }

        return handled;
    }

    private void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (GuiEventListener child : children()) {
            if (child instanceof TooltipProvider tooltipProvider && child.isMouseOver(mouseX, mouseY)) {
                guiGraphics.setTooltipForNextFrame(
                        Lists.transform(tooltipProvider.getTooltipLines(), Component::getVisualOrderText),
                        mouseX,
                        mouseY);
                break;
            }
        }

        for (AbstractOptionWidget<?> widget : optionsScrollBar.getWidgets()) {
            if (!overrideSelectionWidget.isMouseOver(mouseX, mouseY)
                    && widget.isMouseOver(mouseX, mouseY)
                    && optionsScrollBar.isInsideViewport(mouseX, mouseY)) {
                guiGraphics.setTooltipForNextFrame(
                        Lists.transform(widget.getTooltipLines(mouseX, mouseY), Component::getVisualOrderText),
                        mouseX,
                        mouseY);
            }
        }
    }

    @Override
    public void onClose() {
        McUtils.mc().setScreen(previousScreen);
    }

    private void updateMenu() {
        overrideSelectionWidget.visible = false;
        infoWidget.visible = false;
        saveButtonWidget.visible = false;
        resetButtonWidget.visible = false;
        deleteButtonWidget.visible = false;
        optionsScrollBar.visible = false;

        if (this.selectedCategory != null) {
            overrideSelectionWidget.visible = true;
            infoWidget.visible = true;
            saveButtonWidget.visible = true;
            resetButtonWidget.visible = true;
            deleteButtonWidget.visible = true;
            optionsScrollBar.visible = true;
        }
    }

    private void updateOptionWidgets() {
        Optional<MapAttributes> ownAttributes = Services.MapData.getOwnAttributesForCategory(this.selectedCategory);
        Optional<MapAttributes> resolvedAttributes =
                Services.MapData.getInheritedAttributesForCategory(this.selectedCategory);

        for (AbstractOptionWidget<?> widget : optionsScrollBar.getRegisteredWidgets()) {
            widget.updateFromAttributes(ownAttributes, resolvedAttributes);
        }

        optionsScrollBar.updateWidgetPositions();
    }

    private void updateOptionWidgetsVisibility() {
        optionsScrollBar.scrollOffsetY = 0;

        boolean isLocation = selectedOverrideType == OverrideType.MAP_LOCATION_OVERRIDE;
        boolean isArea = selectedOverrideType == OverrideType.MAP_AREA_OVERRIDE;

        // Feature (common) Attributes - always visible
        priorityOptionWidget.display = true;
        levelOptionWidget.display = true;

        // Label Attributes - always visible
        labelOptionWidget.display = true;
        descriptionOptionWidget.display = true;
        labelColorOptionWidget.display = true;
        labelShadowOptionWidget.display = true;
        labelVisibilityOptionWidget.display = true;

        // Icon Attributes - MapLocation only
        iconOptionWidget.display = isLocation;
        iconVisibilityOptionWidget.display = isLocation;
        iconColorOptionWidget.display = isLocation;

        // MapLocation Marker Attributes - MapLocation only
        hasMarkerOptionWidget.display = isLocation;
        markerMinDistanceOptionWidget.display = isLocation;
        markerMaxDistanceOptionWidget.display = isLocation;
        markerFadeOptionWidget.display = isLocation;
        markerBeaconColorOptionWidget.display = isLocation;
        markerHasLabelOptionWidget.display = isLocation;
        markerHasDistanceLabelOptionWidget.display = isLocation;
        markerHasIconOptionWidget.display = isLocation;

        // Area & Border Attributes - MapArea only
        fillColorOptionWidget.display = isArea;
        borderColorOptionWidget.display = isArea;
        borderWidthOptionWidget.display = isArea;

        optionsScrollBar.updateWidgetPositions();
    }

    public void setSelectedCategory(String category) {
        this.selectedCategory = category;
        updateMenu();
        updateOptionWidgets();
        saveButtonWidget.generateTooltip();
        deleteButtonWidget.generateTooltip();
    }

    public String getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedOverrideType(OverrideType newOverrideType) {
        selectedOverrideType = newOverrideType;
        updateOptionWidgetsVisibility();
        infoWidget.generateTooltip();
        saveButtonWidget.generateTooltip();
        deleteButtonWidget.generateTooltip();
    }

    public OverrideType getSelectedOverrideType() {
        return selectedOverrideType;
    }

    public String getOverrideName(boolean includeJsonOverridePrefix) {
        return getOverrideName(getSelectedOverrideType(), getSelectedCategory(), includeJsonOverridePrefix);
    }

    public String getOverrideName(OverrideType overrideType, String categoryId, boolean includeJsonOverridePrefix) {
        String overrideName = "";

        if (includeJsonOverridePrefix) {
            overrideName += "json-override:";
        }
        overrideName += overrideType.name().toLowerCase(Locale.ROOT) + ":" + categoryId;

        return overrideName;
    }
}
