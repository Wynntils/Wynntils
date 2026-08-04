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
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.Optional;
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

    private String selectedCategory;

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
                offsetX + WIDTH_OFFSET, offsetY + HEIGHT_OFFSET, (text) -> categoryTreeWidget.filter(text), this);
        this.addRenderableWidget(categorySearchWidget);

        categoryTreeWidget =
                new CategoryTreeWidget(offsetX + WIDTH_OFFSET, offsetY + HEIGHT_OFFSET + 25, 200, 284 - 25, this);
        categoryTreeWidget.setCategories(
                Services.MapData.allPossibleCategories().toList());
        this.addRenderableWidget(categoryTreeWidget);

        Services.MapData.getCategories().forEach((thing) -> {
            WynntilsMod.info("category: " + thing.getCategoryId());
        });
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

    public void setSelectedCategory(String category) {
        this.selectedCategory = category;
    }

    public Optional<String> getSelectedCategory() {
        return Optional.ofNullable(selectedCategory);
    }
}
