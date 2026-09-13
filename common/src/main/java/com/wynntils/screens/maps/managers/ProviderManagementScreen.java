/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers;

import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.screens.maps.AbstractMapScreen;
import com.wynntils.screens.maps.managers.type.ProviderManagerMode;
import com.wynntils.screens.maps.managers.type.ProviderType;
import com.wynntils.screens.maps.managers.widgets.ManagerSearchWidget;
import com.wynntils.screens.maps.managers.widgets.providermanager.NewProviderButton;
import com.wynntils.screens.maps.managers.widgets.providermanager.ProviderInfoWidget;
import com.wynntils.screens.maps.managers.widgets.providermanager.ProviderManagerInfoWidget;
import com.wynntils.screens.maps.managers.widgets.providermanager.ProviderScrollBarWidget;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ProviderManagementScreen extends WynntilsScreen {
    private static final int WIDTH_OFFSET = 15;
    private static final int HEIGHT_OFFSET = 18;

    private final AbstractMapScreen previousScreen;
    private int offsetX;
    private int offsetY;

    private ProviderManagerMode providerManagerMode = ProviderManagerMode.NONE;

    private String selectedProviderId = "";
    private ProviderType selectedProviderType;

    public ProviderManagerInfoWidget infoWidget;
    public ManagerSearchWidget providerSearchWidget;
    public ProviderScrollBarWidget providerScrollBarWidget;

    public ProviderInfoWidget providerInfoWidget;

    public NewProviderButton newProviderButton;

    private ProviderManagementScreen(AbstractMapScreen previousScreen) {
        super(Component.literal("Provider Management Screen"));
        this.previousScreen = previousScreen;
    }

    public static Screen create(AbstractMapScreen oldMapScreen) {
        return new ProviderManagementScreen(oldMapScreen);
    }

    @Override
    protected void doInit() {
        super.doInit();

        offsetX = (int) ((this.width - Texture.MANAGER_BACKGROUND.width()) / 2f);
        offsetY = (int) ((this.height - Texture.MANAGER_BACKGROUND.height()) / 2f);

        infoWidget =  new ProviderManagerInfoWidget(
                offsetX + WIDTH_OFFSET + 546 - 20,
                offsetY + HEIGHT_OFFSET,
                20,
                20,
                this);
        this.addRenderableWidget(infoWidget);

        providerSearchWidget = new ManagerSearchWidget(
                offsetX + WIDTH_OFFSET,
                offsetY + HEIGHT_OFFSET,
                250 - 5 - Texture.BUILD_LOADOUTS_SEARCH_ICON.width(),
                20,
                (text) -> providerScrollBarWidget.updateWidgetPositions(),
                this);
        this.addRenderableWidget(providerSearchWidget);

        providerScrollBarWidget = new ProviderScrollBarWidget(
                offsetX + WIDTH_OFFSET,
                offsetY + HEIGHT_OFFSET + 25,
                250,
                284 - 50,
                this);
        this.addRenderableWidget(providerScrollBarWidget);

        providerInfoWidget = new ProviderInfoWidget(
                offsetX + WIDTH_OFFSET + 250 + 5,
                offsetY + HEIGHT_OFFSET + 25,
                291,
                284 - 25,
                this);
        this.addRenderableWidget(providerInfoWidget);

        newProviderButton = new NewProviderButton(
                offsetX + WIDTH_OFFSET,
                offsetY + HEIGHT_OFFSET + 25 + 284 - 45,
                100,
                20,
                this);
        this.addRenderableWidget(newProviderButton);

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
    public void onClose() {
        McUtils.setScreen(previousScreen);
    }

    private void updateMenu() {
        providerInfoWidget.visible = false;

        if (providerManagerMode == ProviderManagerMode.DEFAULT) {
            providerInfoWidget.visible = true;
        } else if (providerManagerMode == ProviderManagerMode.NEW_PROVIDER) {

        }
    }

    public void setSelectedProvider(String providerId, ProviderType providerType) {
        selectedProviderId = providerId;
        selectedProviderType = providerType;
        setProviderManagerMode(ProviderManagerMode.DEFAULT);
        updateMenu();
    }

    public String getSelectedProviderId() {
        return selectedProviderId;
    }

    public ProviderType getSelectedProviderType() {
        return selectedProviderType;
    }

    public void setProviderManagerMode(ProviderManagerMode providerManagerMode) {
        this.providerManagerMode = providerManagerMode;
        updateMenu();
    }
}
