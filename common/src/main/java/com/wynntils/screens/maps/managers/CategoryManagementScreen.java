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
import com.wynntils.screens.maps.managers.widgets.CategorySearchWidget;
import com.wynntils.screens.maps.managers.widgets.CategoryTreeWidget;
import com.wynntils.screens.maps.managers.widgets.DeleteButtonWidget;
import com.wynntils.screens.maps.managers.widgets.ResetButtonWidget;
import com.wynntils.screens.maps.managers.widgets.OptionsScrollBarWidget;
import com.wynntils.screens.maps.managers.widgets.OverrideSelectionWidget;
import com.wynntils.screens.maps.managers.widgets.SaveButtonWidget;
import com.wynntils.screens.maps.managers.widgets.options.AbstractOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.ColorOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.IconOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.FloatSliderOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.IntSliderOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.TextOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.TextShadowOptionWidget;
import com.wynntils.screens.maps.managers.widgets.options.ToggleOptionWidget;
import com.wynntils.screens.maps.managers.type.OptionCategory;
import com.wynntils.screens.maps.managers.type.OverrideType;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapMarkerOptions;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public final class CategoryManagementScreen extends WynntilsScreen {
    private static final int WIDTH_OFFSET = 15;
    private static final int HEIGHT_OFFSET = 18;

    private final MainMapScreen previousScreen;
    private int offsetX;
    private int offsetY;

    public CategorySearchWidget categorySearchWidget;
    public CategoryTreeWidget categoryTreeWidget;
    public OverrideSelectionWidget overrideSelectionWidget;

    public SaveButtonWidget saveButtonWidget;
    public ResetButtonWidget resetButtonWidget;
    public DeleteButtonWidget deleteButtonWidget;

    public OptionsScrollBarWidget optionsScrollBar;


    // region Feature (common) Attributes

    public IntSliderOptionWidget priorityOptionWidget;
    public IntSliderOptionWidget levelOptionWidget;

    // endregion

    // region Label Attributes

    public TextOptionWidget labelOptionWidget;
    public TextOptionWidget descriptionOptionWidget;

    public FloatSliderOptionWidget labelVisibilityMinOptionWidget;
    public FloatSliderOptionWidget labelVisibilityMaxOptionWidget;
    public FloatSliderOptionWidget labelVisibilityFadeOptionWidget;
    public ColorOptionWidget labelColorOptionWidget;
    public TextShadowOptionWidget labelShadowOptionWidget;

    // endregion

    // region Icon Attributes

    public IconOptionWidget iconOptionWidget;
    public FloatSliderOptionWidget iconVisibilityMinOptionWidget;
    public FloatSliderOptionWidget iconVisibilityMaxOptionWidget;
    public FloatSliderOptionWidget iconVisibilityFadeOptionWidget;
    public ColorOptionWidget iconColorOptionWidget;

    //TODO: look at map decotation will probably need toggle for isvisible? idk

    // endregion

    // region MapLocation Marker Attributes

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

    // This makes it so when we open a color picker screen or an icon selection screen that the widgets do not get cleared.
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
                        .append(Component.translatable("screens.wynntils.map.managers.categoryManager.level.description2"))
                        .append("\n\n")
                        .append(Component.translatable("screens.wynntils.map.managers.categoryManager.level.description3")),
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

        labelVisibilityMinOptionWidget = new FloatSliderOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.labelVisibilityMin"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.labelVisibilityMin.description"),
                OptionCategory.LABEL,
                0f,
                100f,
                attrs -> attrs.getLabelVisibility().flatMap(MapVisibility::getMin),
                this);

        labelVisibilityMaxOptionWidget = new FloatSliderOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.labelVisibilityMax"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.labelVisibilityMax.description"),
                OptionCategory.LABEL,
                0f,
                100f,
                attrs -> attrs.getLabelVisibility().flatMap(MapVisibility::getMax),
                this);

        labelVisibilityFadeOptionWidget = new FloatSliderOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.labelVisibilityFade"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.labelVisibilityFade.description"),
                OptionCategory.LABEL,
                0f,
                100f,
                attrs -> attrs.getLabelVisibility().flatMap(MapVisibility::getFade),
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

        // endregion

        // region Icon Attributes

        iconOptionWidget = new IconOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.icon"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.icon.description"),
                OptionCategory.ICON,
                MapAttributes::getIconId);

        iconVisibilityMinOptionWidget = new FloatSliderOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconVisibilityMin"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconVisibilityMin.description"),
                OptionCategory.ICON,
                0f,
                100f,
                attrs -> attrs.getIconVisibility().flatMap(MapVisibility::getMin),
                this);

        iconVisibilityMaxOptionWidget = new FloatSliderOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconVisibilityMax"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconVisibilityMax.description"),
                OptionCategory.ICON,
                0f,
                100f,
                attrs -> attrs.getIconVisibility().flatMap(MapVisibility::getMax),
                this);

        iconVisibilityFadeOptionWidget = new FloatSliderOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconVisibilityFade"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconVisibilityFade.description"),
                OptionCategory.ICON,
                0f,
                100f,
                attrs -> attrs.getIconVisibility().flatMap(MapVisibility::getFade),
                this);

        iconColorOptionWidget = new ColorOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconColor"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconColor.description"),
                OptionCategory.ICON,
                MapAttributes::getIconColor,
                this);

        // endregion

        // region MapLocation Marker Attributes

        hasMarkerOptionWidget = new ToggleOptionWidget(
                Component.translatable("screens.wynntils.map.managers.categoryManager.hasMarker"),
                Component.translatable("screens.wynntils.map.managers.categoryManager.hasMarker.description1")
                        .append("\n\n")
                        .append(Component.translatable("screens.wynntils.map.managers.categoryManager.hasMarker.description2")),
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
                Component.translatable("screens.wynntils.map.managers.categoryManager.markerHasDistanceLabel.description"),
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

        categorySearchWidget = new CategorySearchWidget(
                offsetX + WIDTH_OFFSET,
                offsetY + HEIGHT_OFFSET,
                (text) -> categoryTreeWidget.filter(text),
                this);
        this.addRenderableWidget(categorySearchWidget);

        categoryTreeWidget =
                new CategoryTreeWidget(
                        offsetX + WIDTH_OFFSET,
                        offsetY + HEIGHT_OFFSET + 25,
                        200,
                        284 - 25,
                        this);
        categoryTreeWidget.setCategories(Services.MapData.allPossibleCategories().toList());
        this.addRenderableWidget(categoryTreeWidget);

        optionsScrollBar = new OptionsScrollBarWidget(
                offsetX + WIDTH_OFFSET + 200 + 5,
                offsetY + HEIGHT_OFFSET + 25,
                341,
                284 - 50,
                this);

        optionsScrollBar.addWidget(priorityOptionWidget);
        optionsScrollBar.addWidget(levelOptionWidget);

        optionsScrollBar.addWidget(labelOptionWidget);
        optionsScrollBar.addWidget(descriptionOptionWidget);
        optionsScrollBar.addWidget(labelVisibilityMinOptionWidget);
        optionsScrollBar.addWidget(labelVisibilityMaxOptionWidget);
        optionsScrollBar.addWidget(labelVisibilityFadeOptionWidget);
        optionsScrollBar.addWidget(labelColorOptionWidget);
        optionsScrollBar.addWidget(labelShadowOptionWidget);

        optionsScrollBar.addWidget(iconOptionWidget);
        optionsScrollBar.addWidget(iconVisibilityMinOptionWidget);
        optionsScrollBar.addWidget(iconVisibilityMaxOptionWidget);
        optionsScrollBar.addWidget(iconVisibilityFadeOptionWidget);
        optionsScrollBar.addWidget(iconColorOptionWidget);

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

        overrideSelectionWidget = new OverrideSelectionWidget(
                offsetX + WIDTH_OFFSET + 200 + 5,
                offsetY + HEIGHT_OFFSET,
                150,
                20,
                this
        );
        this.addRenderableWidget(overrideSelectionWidget);

        saveButtonWidget = new SaveButtonWidget(
                offsetX + WIDTH_OFFSET + 200 + 5,
                offsetY + HEIGHT_OFFSET + 284 - 20,
                103,
                20,
                this
        );
        this.addRenderableWidget(saveButtonWidget);

        resetButtonWidget = new ResetButtonWidget(
                offsetX + WIDTH_OFFSET + 200 + 5 + (103 + 16) * 1,
                offsetY + HEIGHT_OFFSET + 284 - 20,
                103,
                20,
                this
        );
        this.addRenderableWidget(resetButtonWidget);

        deleteButtonWidget = new DeleteButtonWidget(
                offsetX + WIDTH_OFFSET + 200 + 5 + (103 + 16) * 2,
                offsetY + HEIGHT_OFFSET + 284 - 20,
                103,
                20,
                this
        );
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
                setFocusedTextInput(widget.getTextInputBoxWidget());
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
            if (widget.isMouseOver(mouseX, mouseY) && optionsScrollBar.isInsideViewport(mouseX, mouseY)) {
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
        saveButtonWidget.visible = false;
        resetButtonWidget.visible = false;
        deleteButtonWidget.visible = false;
        optionsScrollBar.visible = false;

        if (this.selectedCategory != null) {
            overrideSelectionWidget.visible = true;
            saveButtonWidget.visible = true;
            resetButtonWidget.visible = true;
            deleteButtonWidget.visible = true;
            optionsScrollBar.visible = true;
        }
    }

    private void updateOptionWidgets() {
        Optional<MapAttributes> ownAttributes = Services.MapData.getOwnAttributesForCategory(this.selectedCategory);
        Optional<MapAttributes> resolvedAttributes = Services.MapData.getInheritedAttributesForCategory(this.selectedCategory);

        for (AbstractOptionWidget<?> widget : optionsScrollBar.getRegisteredWidgets()) {
            widget.updateFromAttributes(ownAttributes, resolvedAttributes);
        }
    }

    private void updateOptionWidgetsVisibility() {
        optionsScrollBar.scrollOffsetY = 0;

        boolean isLocation = selectedOverrideType == OverrideType.MAP_LOCATION_OVERRIDE;
        boolean isArea = selectedOverrideType == OverrideType.MAP_AREA_OVERRIDE;

        // Feature (common) Attributes - always visible
        priorityOptionWidget.visible = true;
        levelOptionWidget.visible = true;

        // Label Attributes - always visible
        labelOptionWidget.visible = true;
        descriptionOptionWidget.visible = true;
        labelVisibilityMinOptionWidget.visible = true;
        labelVisibilityMaxOptionWidget.visible = true;
        labelVisibilityFadeOptionWidget.visible = true;
        labelColorOptionWidget.visible = true;
        labelShadowOptionWidget.visible = true;

        // Icon Attributes - MapLocation only
        iconOptionWidget.visible = isLocation;
        iconVisibilityMinOptionWidget.visible = isLocation;
        iconVisibilityMaxOptionWidget.visible = isLocation;
        iconVisibilityFadeOptionWidget.visible = isLocation;
        iconColorOptionWidget.visible = isLocation;

        // MapLocation Marker Attributes - MapLocation only
        hasMarkerOptionWidget.visible = isLocation;
        markerMinDistanceOptionWidget.visible = isLocation;
        markerMaxDistanceOptionWidget.visible = isLocation;
        markerFadeOptionWidget.visible = isLocation;
        markerBeaconColorOptionWidget.visible = isLocation;
        markerHasLabelOptionWidget.visible = isLocation;
        markerHasDistanceLabelOptionWidget.visible = isLocation;
        markerHasIconOptionWidget.visible = isLocation;

        // Area & Border Attributes - MapArea only
        fillColorOptionWidget.visible = isArea;
        borderColorOptionWidget.visible = isArea;
        borderWidthOptionWidget.visible = isArea;
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
        saveButtonWidget.generateTooltip();
        deleteButtonWidget.generateTooltip();
    }

    public OverrideType getSelectedOverrideType() {
        return selectedOverrideType;
    }
}
