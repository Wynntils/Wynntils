/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.CategorySearchWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.CategoryTreeWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.ResetButtonWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.OptionsScrollBarWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.OverrideSelectionWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.SaveButtonWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.optionwidgets.ColorOptionWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.optionwidgets.IconOptionWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.optionwidgets.FloatSliderOptionWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.optionwidgets.IntSliderOptionWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.optionwidgets.TextOptionWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.optionwidgets.TextShadowOptionWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.optionwidgets.ToggleOptionWidget;
import com.wynntils.screens.maps.type.OptionCategory;
import com.wynntils.screens.maps.type.OverrideType;
import com.wynntils.screens.maps.type.ScrollableWidget;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapMarkerOptions;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.TextShadow;
import net.minecraft.client.gui.GuiGraphics;
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

    public OptionsScrollBarWidget optionsScrollBar;


    // region Feature (common) Attributes

    private IntSliderOptionWidget priorityOptionWidget;
    private IntSliderOptionWidget levelOptionWidget;

    // endregion

    // region Label Attributes

    private TextOptionWidget labelOptionWidget;
    private TextOptionWidget descriptionOptionWidget;

    private FloatSliderOptionWidget labelVisibilityMinOptionWidget;
    private FloatSliderOptionWidget labelVisibilityMaxOptionWidget;
    private FloatSliderOptionWidget labelVisibilityFadeOptionWidget;
    private ColorOptionWidget labelColorOptionWidget;
    private TextShadowOptionWidget labelShadowOptionWidget;

    // endregion

    // region Icon Attributes

    private IconOptionWidget iconOptionWidget;
    private FloatSliderOptionWidget iconVisibilityMinOptionWidget;
    private FloatSliderOptionWidget iconVisibilityMaxOptionWidget;
    private FloatSliderOptionWidget iconVisibilityFadeOptionWidget;
    private ColorOptionWidget iconColorOptionWidget;

    //TODO: look at map decotation will probably need toggle for isvisible? idk

    // endregion

    // region MapLocation Marker Attributes

    private ToggleOptionWidget hasMarkerOptionWidget;

    private FloatSliderOptionWidget markerMinDistanceOptionWidget;
    private FloatSliderOptionWidget markerMaxDistanceOptionWidget;
    private FloatSliderOptionWidget markerFadeOptionWidget;
    private ColorOptionWidget markerBeaconColorOptionWidget;
    private ToggleOptionWidget markerHasLabelOptionWidget;
    private ToggleOptionWidget markerHasDistanceLabelOptionWidget;
    private ToggleOptionWidget markerHasIconOptionWidget;

    // endregion

    // region Area & Border Attributes

    private ColorOptionWidget fillColorOptionWidget;
    private ColorOptionWidget borderColorOptionWidget;
    private FloatSliderOptionWidget borderWidthOptionWidget;

    // endregion


    private ColorOptionWidget colorOptionWidget;
    private FloatSliderOptionWidget floatSliderOptionWidget;
    private IntSliderOptionWidget intSliderOptionWidget;
    private ToggleOptionWidget toggleOptionWidget;
    private TextOptionWidget textOptionWidget;
    private TextShadowOptionWidget textShadowOptionWidget;

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

    //TODO: fix this and like make it so it sets the positions instead of rebuilding.
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

        priorityOptionWidget = new IntSliderOptionWidget("Priority", OptionCategory.GENERAL, 0, 0, 100, this);
        levelOptionWidget = new IntSliderOptionWidget("Level", OptionCategory.GENERAL, 0, 0, 110, this);

        // endregion

        // region Label Attributes

        labelOptionWidget = new TextOptionWidget("Label", OptionCategory.LABEL, "None", this);
        descriptionOptionWidget = new TextOptionWidget("Description", OptionCategory.LABEL, "None", this);

        labelVisibilityMinOptionWidget = new FloatSliderOptionWidget("Label Visibility Min", OptionCategory.LABEL, 0f, 0f, 1f, this);
        labelVisibilityMaxOptionWidget = new FloatSliderOptionWidget("Label Visibility Max", OptionCategory.LABEL, 0f, 0f, 1f, this);
        labelVisibilityFadeOptionWidget = new FloatSliderOptionWidget("Label Visibility Fade", OptionCategory.LABEL, 0f, 0f, 1f, this);
        labelColorOptionWidget = new ColorOptionWidget("Label Color", OptionCategory.LABEL, CustomColor.NONE, this);
        labelShadowOptionWidget = new TextShadowOptionWidget("Label Shadow", OptionCategory.LABEL, TextShadow.NORMAL);

        // endregion

        // region Icon Attributes

        iconOptionWidget = new IconOptionWidget("Icon", OptionCategory.ICON, "none");
        iconVisibilityMinOptionWidget = new FloatSliderOptionWidget("Icon Visibility Min", OptionCategory.ICON, 0f, 0f, 1f, this);
        iconVisibilityMaxOptionWidget = new FloatSliderOptionWidget("Icon Visibility Max", OptionCategory.ICON, 0f, 0f, 1f, this);
        iconVisibilityFadeOptionWidget = new FloatSliderOptionWidget("Icon Visibility Fade", OptionCategory.ICON, 0f, 0f, 1f, this);
        iconColorOptionWidget = new ColorOptionWidget("Icon Color", OptionCategory.ICON, CustomColor.NONE, this);

        // endregion

        // region MapLocation Marker Attributes

        hasMarkerOptionWidget = new ToggleOptionWidget("Has Marker", OptionCategory.MARKER, false);

        markerMinDistanceOptionWidget = new FloatSliderOptionWidget("Marker Min Distance", OptionCategory.MARKER,  0f, 0f, 500f, this);
        markerMaxDistanceOptionWidget = new FloatSliderOptionWidget("Marker Max Distance", OptionCategory.MARKER, 0f, 0f, 500f, this);
        markerFadeOptionWidget = new FloatSliderOptionWidget("Marker Fade", OptionCategory.MARKER, 0f, 0f, 500f, this);
        markerBeaconColorOptionWidget = new ColorOptionWidget("Marker Beacon Color", OptionCategory.MARKER, CustomColor.NONE, this);
        markerHasLabelOptionWidget = new ToggleOptionWidget("Marker Has Label", OptionCategory.MARKER,  false);
        markerHasDistanceLabelOptionWidget = new ToggleOptionWidget("Marker Has Distance Label", OptionCategory.MARKER, false);
        markerHasIconOptionWidget = new ToggleOptionWidget("Marker Has Icon", OptionCategory.MARKER, false);

        // endregion

        // region Area & Border Attributes

        fillColorOptionWidget = new ColorOptionWidget("Fill Color", OptionCategory.AREA_BORDER, CustomColor.NONE, this);
        borderColorOptionWidget = new ColorOptionWidget("Border Color", OptionCategory.AREA_BORDER, CustomColor.NONE, this);
        borderWidthOptionWidget = new FloatSliderOptionWidget("Border Width", OptionCategory.AREA_BORDER, 0f, 0f, 10f, this);

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
                offsetX + WIDTH_OFFSET + 200 + 5 + 40,
                offsetY + HEIGHT_OFFSET + 284 - 20,
                103,
                20,
                this
        );
        this.addRenderableWidget(saveButtonWidget);

        resetButtonWidget = new ResetButtonWidget(
                offsetX + WIDTH_OFFSET + 200 + 5 + (103 + 16) * 2 - 40,
                offsetY + HEIGHT_OFFSET + 284 - 20,
                103,
                20,
                this
        );
        this.addRenderableWidget(resetButtonWidget);

        updateMenu();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(guiGraphics);

        renderables.forEach(renderable -> renderable.render(guiGraphics, mouseX, mouseY, partialTick));
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

        for (ScrollableWidget<?> widget : optionsScrollBar.getRegisteredWidgets()) {
            if (widget.isMouseOverTextBox(event.x(), event.y())) {
                setFocusedTextInput(widget.getTextInputBoxWidget());
                WynntilsMod.info("setting to text box");
            }
        }

        TextInputBoxWidget focused = getFocusedTextInput();
        boolean handled = super.doMouseClicked(event, isDoubleClick);

        if (focused != null && !focused.isMouseOver(event.x(), event.y())) {
            setFocusedTextInput(null);
        }

        return handled;
    }
    @Override
    public void onClose() {
        McUtils.mc().setScreen(previousScreen);
    }


    private void updateMenu() {
        overrideSelectionWidget.visible = false;
        saveButtonWidget.visible = false;
        resetButtonWidget.visible = false;
        optionsScrollBar.visible = false;

        if (this.selectedCategory != null) {
            overrideSelectionWidget.visible = true;
            saveButtonWidget.visible = true;
            resetButtonWidget.visible = true;
            optionsScrollBar.visible = true;
        }
    }

    private void updateOptionWidgets() {
        Optional<MapAttributes> attributesOptional = Services.MapData.getAttributesForCategory(this.selectedCategory);

        if (attributesOptional.isEmpty()) return;
        MapAttributes attributes = attributesOptional.get();


        if (attributes.getPriority().isPresent()) {
            priorityOptionWidget.setValue(attributes.getPriority().get());
        }

        if (attributes.getLevel().isPresent()) {
            levelOptionWidget.setValue(attributes.getLevel().get());
        }

        if (attributes.getLabel().isPresent()) {
            labelOptionWidget.setValue(attributes.getLabel().get());
        }

        if (attributes.getDescription().isPresent()) {
            descriptionOptionWidget.setValue(attributes.getDescription().get());
        }

        if (attributes.getLabelVisibility().isPresent()) {
            MapVisibility labelVisibility = attributes.getLabelVisibility().get();

            if (labelVisibility.getMin().isPresent()) {
                labelVisibilityMinOptionWidget.setValue(labelVisibility.getMin().get());
            }

            if (labelVisibility.getMax().isPresent()) {
                labelVisibilityMaxOptionWidget.setValue(labelVisibility.getMax().get());
            }

            if (labelVisibility.getFade().isPresent()) {
                labelVisibilityFadeOptionWidget.setValue(labelVisibility.getFade().get());
            }
        }

        if (attributes.getLabelColor().isPresent()) {
            labelColorOptionWidget.setValue(attributes.getLabelColor().get());
        }

        if (attributes.getLabelShadow().isPresent()) {
            labelShadowOptionWidget.setValue(attributes.getLabelShadow().get());
        }

        if (attributes.getIconId().isPresent()) {
            iconOptionWidget.setValue(attributes.getIconId().get());
        }

        if (attributes.getIconVisibility().isPresent()) {
            MapVisibility iconVisibility = attributes.getIconVisibility().get();

            if (iconVisibility.getMin().isPresent()) {
                iconVisibilityMinOptionWidget.setValue(iconVisibility.getMin().get());
            }

            if (iconVisibility.getMax().isPresent()) {
                iconVisibilityMaxOptionWidget.setValue(iconVisibility.getMax().get());
            }

            if (iconVisibility.getFade().isPresent()) {
                iconVisibilityFadeOptionWidget.setValue(iconVisibility.getFade().get());
            }
        }

        if (attributes.getIconColor().isPresent()) {
            iconColorOptionWidget.setValue(attributes.getIconColor().get());
        }



        if (attributes.getHasMarker().isPresent()) {
            hasMarkerOptionWidget.setValue(attributes.getHasMarker().get());
        }

        if (attributes.getMarkerOptions().isPresent()) {
            MapMarkerOptions markerOptions = attributes.getMarkerOptions().get();

            if (markerOptions.getMinDistance().isPresent()) {
                markerMinDistanceOptionWidget.setValue(markerOptions.getMinDistance().get());
            }

            if (markerOptions.getMaxDistance().isPresent()) {
                markerMaxDistanceOptionWidget.setValue(markerOptions.getMaxDistance().get());
            }

            if (markerOptions.getFade().isPresent()) {
                markerFadeOptionWidget.setValue(markerOptions.getFade().get());
            }

            if (markerOptions.getBeaconColor().isPresent()) {
                markerBeaconColorOptionWidget.setValue(markerOptions.getBeaconColor().get());
            }

            if (markerOptions.getHasLabel().isPresent()) {
                markerHasLabelOptionWidget.setValue(markerOptions.getHasLabel().get());
            }

            if (markerOptions.getHasDistanceLabel().isPresent()) {
                markerHasDistanceLabelOptionWidget.setValue(markerOptions.getHasDistanceLabel().get());
            }

            if (markerOptions.getHasIcon().isPresent()) {
                markerHasIconOptionWidget.setValue(markerOptions.getHasIcon().get());
            }
        }

        if (attributes.getFillColor().isPresent()) {
            fillColorOptionWidget.setValue(attributes.getFillColor().get());
        }

        if (attributes.getBorderColor().isPresent()) {
            borderColorOptionWidget.setValue(attributes.getBorderColor().get());
        }

        if (attributes.getBorderWidth().isPresent()) {
            borderWidthOptionWidget.setValue(attributes.getBorderWidth().get());
        }

    }

    public void setSelectedCategory(String category) {
        this.selectedCategory = category;
        updateMenu();
        updateOptionWidgets();
    }

    public String getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedOverrideType(OverrideType newOverrideType) {
        selectedOverrideType = newOverrideType;
    }

    public OverrideType getSelectedOverrideType() {
        return selectedOverrideType;
    }
}
