/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.CategorySearchWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.CategoryTreeWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.DeleteButtonWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.OptionsScrollBarWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.OverrideSelectionWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.ResetButtonWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.SaveButtonWidget;
import com.wynntils.screens.maps.categorymanagerwidgets.optionwidgets.ToggleOptionWidget;
import com.wynntils.screens.maps.type.OptionCategory;
import com.wynntils.screens.maps.type.OverrideType;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
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

    public SaveButtonWidget saveButtonWidget;
    public ResetButtonWidget resetButtonWidget;
    public DeleteButtonWidget deleteButtonWidget;

    public OptionsScrollBarWidget optionsScrollBar;

    private final ToggleOptionWidget autoScroll =
            new ToggleOptionWidget("Auto-Scroll", OptionCategory.GENERAL, true);
    private final ToggleOptionWidget showWaypoints =
            new ToggleOptionWidget("Show Waypoints", OptionCategory.DISPLAY, true);
    private final ToggleOptionWidget enableAnimations =
            new ToggleOptionWidget("Enable Animations", OptionCategory.DISPLAY, false);
    private final ToggleOptionWidget smoothScrolling =
            new ToggleOptionWidget("Smooth Scrolling", OptionCategory.PERFORMANCE, true);
    private final ToggleOptionWidget cacheMapData =
            new ToggleOptionWidget("Cache Map Data", OptionCategory.PERFORMANCE, true);
    private final ToggleOptionWidget debugMode =
            new ToggleOptionWidget("Debug Mode", OptionCategory.ADVANCED, false);
    private final ToggleOptionWidget showCoordinates =
            new ToggleOptionWidget("Show Coordinates", OptionCategory.GENERAL, false);
    private final ToggleOptionWidget compactMode =
            new ToggleOptionWidget("Compact Mode", OptionCategory.DISPLAY, false);
    private final ToggleOptionWidget preloadTextures =
            new ToggleOptionWidget("Preload Textures", OptionCategory.PERFORMANCE, true);
    private final ToggleOptionWidget experimentalFeatures =
            new ToggleOptionWidget("Experimental Features", OptionCategory.ADVANCED, false);
    private final ToggleOptionWidget showGrid =
            new ToggleOptionWidget("Show Grid", OptionCategory.DISPLAY, true);
    private final ToggleOptionWidget verboseLogging =
            new ToggleOptionWidget("Verbose Logging", OptionCategory.ADVANCED, false);


    private String selectedCategory;
    private OverrideType selectedOverrideType = OverrideType.MAP_LOCATION_OVERRIDE;

    private CategoryManagementScreen(MainMapScreen previousScreen) {
        super(Component.literal("Category Management Screen"));
        this.previousScreen = previousScreen;
    }

    public static Screen create(MainMapScreen previousScreen) {
        return new CategoryManagementScreen(previousScreen);
    }

    @Override
    protected void doInit() {
        super.doInit();

        offsetX = (int) ((this.width - Texture.MANAGER_BACKGROUND.width()) / 2f);
        offsetY = (int) ((this.height - Texture.MANAGER_BACKGROUND.height()) / 2f);

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

        optionsScrollBar.addWidget(autoScroll);
        optionsScrollBar.addWidget(showWaypoints);
        optionsScrollBar.addWidget(enableAnimations);
        optionsScrollBar.addWidget(smoothScrolling);
        optionsScrollBar.addWidget(cacheMapData);
        optionsScrollBar.addWidget(debugMode);
        optionsScrollBar.addWidget(showCoordinates);
        optionsScrollBar.addWidget(compactMode);
        optionsScrollBar.addWidget(preloadTextures);
        optionsScrollBar.addWidget(experimentalFeatures);
        optionsScrollBar.addWidget(showGrid);
        optionsScrollBar.addWidget(verboseLogging);

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
                offsetX + WIDTH_OFFSET + 200 + 5 + 103 + 16,
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

    public void setSelectedCategory(String category) {
        this.selectedCategory = category;
        updateMenu();
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
