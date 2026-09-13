/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers;

import com.google.common.collect.Lists;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.maps.AbstractMapScreen;
import com.wynntils.screens.maps.managers.type.ProviderManagerMode;
import com.wynntils.screens.maps.managers.type.ProviderType;
import com.wynntils.screens.maps.managers.widgets.ManagerSearchWidget;
import com.wynntils.screens.maps.managers.widgets.StyledButton;
import com.wynntils.screens.maps.managers.widgets.TexturedTextInputBoxWidget;
import com.wynntils.screens.maps.managers.widgets.providermanager.NewProviderButton;
import com.wynntils.screens.maps.managers.widgets.providermanager.OpenLocalProviderFolderButton;
import com.wynntils.screens.maps.managers.widgets.providermanager.ProviderInfoWidget;
import com.wynntils.screens.maps.managers.widgets.providermanager.ProviderManagerInfoWidget;
import com.wynntils.screens.maps.managers.widgets.providermanager.ProviderScrollBarWidget;
import com.wynntils.screens.maps.managers.widgets.providermanager.RefreshLocalProvidersButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
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
    public RefreshLocalProvidersButton refreshLocalProvidersButton;

    public OpenLocalProviderFolderButton openLocalProviderFolderButton;
    public TexturedTextInputBoxWidget remoteProviderNameTextBox;
    public TexturedTextInputBoxWidget remoteProviderURLTextBox;
    public StyledButton saveRemoteProviderButton;

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

        refreshLocalProvidersButton = new RefreshLocalProvidersButton(
                offsetX + WIDTH_OFFSET + 100 + 10,
                offsetY + HEIGHT_OFFSET + 25 + 284 - 45,
                140,
                20,
                this);
        this.addRenderableWidget(refreshLocalProvidersButton);

        openLocalProviderFolderButton = new OpenLocalProviderFolderButton(
                offsetX + WIDTH_OFFSET + 250 + 5 + 5,
                offsetY + HEIGHT_OFFSET + 55,
                291 - 10,
                20,
                this);
        this.addRenderableWidget(openLocalProviderFolderButton);

        remoteProviderNameTextBox = new TexturedTextInputBoxWidget(
                offsetX + WIDTH_OFFSET + 250 + 5 + 5,
                offsetY + HEIGHT_OFFSET + 30 + 15 + 110,
                291 - 10,
                20,
                (string) -> {},
                this,
                TexturedTextInputBoxWidget.Mode.STRING);
        this.addRenderableWidget(remoteProviderNameTextBox);

        remoteProviderURLTextBox = new TexturedTextInputBoxWidget(
                offsetX + WIDTH_OFFSET + 250 + 5 + 5,
                offsetY + HEIGHT_OFFSET + 55 + 30 + 110,
                291 - 10,
                20,
                (string) -> {},
                this,
                TexturedTextInputBoxWidget.Mode.STRING);
        this.addRenderableWidget(remoteProviderURLTextBox);

        saveRemoteProviderButton = new StyledButton(
                offsetX + WIDTH_OFFSET + 255 + (291 / 2) - 50,
                offsetY + HEIGHT_OFFSET + 258 - 25,
                100,
                20,
                Component.literal("Save"),
                Texture.MANAGER_WIDGET_BACKGROUND_GREEN,
                () -> {});
        this.addRenderableWidget(saveRemoteProviderButton);

        updateMenu();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(guiGraphics);

        if (providerManagerMode == ProviderManagerMode.NEW_PROVIDER) {
            // Local provider
            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics,
                    Texture.MANAGER_WIDGET_BACKGROUND,
                    offsetX + WIDTH_OFFSET + 250 + 5,
                    offsetY + HEIGHT_OFFSET + 25,
                    291,
                    100 - 44);

            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics,
                    Texture.CATEGORY_MANAGER_OPTION_HEADER,
                    offsetX + WIDTH_OFFSET + 250 + 5 + 10,
                    offsetY + HEIGHT_OFFSET + 25 - 8 + 1,
                    150,
                    16);

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString("Local Provider"),
                            offsetX + WIDTH_OFFSET + 250 + 5 + 10 + 20,
                            offsetY + HEIGHT_OFFSET + 25 + 1,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
            // end region

            // Remote provider
            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics,
                    Texture.MANAGER_WIDGET_BACKGROUND,
                    offsetX + WIDTH_OFFSET + 250 + 5,
                    offsetY + HEIGHT_OFFSET + 25 + 100,
                    291,
                    284 - 50 - 100);

            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics,
                    Texture.CATEGORY_MANAGER_OPTION_HEADER,
                    offsetX + WIDTH_OFFSET + 250 + 5 + 10,
                    offsetY + HEIGHT_OFFSET + 25 + 100 - 8 + 1,
                    150,
                    16);

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString("Remote Provider"),
                            offsetX + WIDTH_OFFSET + 250 + 5 + 10 + 20,
                            offsetY + HEIGHT_OFFSET + 25 + 100 + 1,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString("Provider name:"),
                            offsetX + WIDTH_OFFSET + 250 + 5 + 5,
                            offsetY + HEIGHT_OFFSET + 35 + 3 + 110,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString("Provider URL:"),
                            offsetX + WIDTH_OFFSET + 250 + 5 + 5,
                            offsetY + HEIGHT_OFFSET + 55 + 20 + 3 + 110,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            // end region
        }

        renderables.forEach(renderable -> renderable.render(guiGraphics, mouseX, mouseY, partialTick));
        renderTooltips(guiGraphics, mouseX, mouseY);
    }

    private void renderBackgroundTexture(GuiGraphics guiGraphics) {
        RenderUtils.drawTexturedRect(guiGraphics, Texture.MANAGER_BACKGROUND, offsetX, offsetY);
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
    }

    @Override
    public void onClose() {
        McUtils.setScreen(previousScreen);
    }

    private void updateMenu() {
        providerInfoWidget.visible = false;
        openLocalProviderFolderButton.visible = false;
        remoteProviderNameTextBox.visible = false;
        remoteProviderURLTextBox.visible = false;
        saveRemoteProviderButton.visible = false;

        if (providerManagerMode == ProviderManagerMode.DEFAULT) {
            providerInfoWidget.visible = true;
        } else if (providerManagerMode == ProviderManagerMode.NEW_PROVIDER) {
            openLocalProviderFolderButton.visible = true;
            remoteProviderNameTextBox.visible = true;
            remoteProviderURLTextBox.visible = true;
            saveRemoteProviderButton.visible = true;
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
