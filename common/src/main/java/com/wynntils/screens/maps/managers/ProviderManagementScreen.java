/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers;

import com.google.common.collect.Lists;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.AbstractMapScreen;
import com.wynntils.screens.maps.managers.type.ProviderManagerMode;
import com.wynntils.screens.maps.managers.type.ProviderType;
import com.wynntils.screens.maps.managers.widgets.ManagerSearchWidget;
import com.wynntils.screens.maps.managers.widgets.providermanager.DescriptionScrollBarWidget;
import com.wynntils.screens.maps.managers.widgets.providermanager.NewProviderButton;
import com.wynntils.screens.maps.managers.widgets.providermanager.OpenLocalProviderFolderButton;
import com.wynntils.screens.maps.managers.widgets.providermanager.ProviderInfoScrollBarWidget;
import com.wynntils.screens.maps.managers.widgets.providermanager.ProviderManagerInfoWidget;
import com.wynntils.screens.maps.managers.widgets.providermanager.ProviderScrollBarWidget;
import com.wynntils.screens.maps.managers.widgets.providermanager.RefreshLocalProvidersButton;
import com.wynntils.screens.maps.managers.widgets.providermanager.RemoteProviderCreationWidget;
import com.wynntils.screens.maps.managers.widgets.providermanager.RemoveProviderButton;
import com.wynntils.screens.maps.managers.widgets.providermanager.SaveRemoteProviderButton;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.services.mapdata.providers.json.JsonProvider;
import com.wynntils.services.mapdata.providers.json.JsonProviderInfo;
import com.wynntils.services.mapdata.providers.type.MapDataProvider;
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
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

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

    public ProviderInfoScrollBarWidget providerInfoScrollBarWidget;
    public DescriptionScrollBarWidget descriptionScrollBarWidget;
    public RemoveProviderButton removeProviderButton;

    public NewProviderButton newProviderButton;
    public RefreshLocalProvidersButton refreshLocalProvidersButton;

    public OpenLocalProviderFolderButton openLocalProviderFolderButton;
    public RemoteProviderCreationWidget remoteProviderCreationWidget;

    private ProviderManagementScreen(AbstractMapScreen previousScreen) {
        super(Component.literal("Provider Management Screen"));
        this.previousScreen = previousScreen;
    }

    public static Screen create(AbstractMapScreen oldMapScreen) {
        return new ProviderManagementScreen(oldMapScreen);
    }

    // This makes it so when we open a color picker screen or an icon selection screen that the widgets do not get
    // cleared.
    @Override
    protected void rebuildWidgets() {
        providerInfoScrollBarWidget.populateWidgets();
    }

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

        // provider info menu

        providerInfoScrollBarWidget = new ProviderInfoScrollBarWidget(
                offsetX + WIDTH_OFFSET + 250 + 5,
                offsetY + HEIGHT_OFFSET + 25 + 18,
                291,
                284 - 25 - 100 - 27,
                this);
        this.addRenderableWidget(providerInfoScrollBarWidget);

        descriptionScrollBarWidget = new DescriptionScrollBarWidget(
                offsetX + WIDTH_OFFSET + 250 + 5,
                offsetY + HEIGHT_OFFSET + 207 - 27,
                291,
                79,
                this);
        this.addRenderableWidget(descriptionScrollBarWidget);

        removeProviderButton = new RemoveProviderButton(
                offsetX + WIDTH_OFFSET + 250 + 5 + 291 / 2 - 100 /2,
                offsetY + HEIGHT_OFFSET + 207 + 52 + 5,
                100,
                20,
                this);
        this.addRenderableWidget(removeProviderButton);

        // end region

        // new provider menu

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

        remoteProviderCreationWidget = new RemoteProviderCreationWidget(
                offsetX + WIDTH_OFFSET + 250 + 5,
                offsetY + HEIGHT_OFFSET + 25 + 100,
                291,
                284 - 50 - 100,
                this);
        this.addRenderableWidget(remoteProviderCreationWidget);

        // end region

        updateMenu();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(guiGraphics);

        renderNewProviderMenu(guiGraphics, mouseX, mouseY, partialTick);

        renderables.forEach(renderable -> renderable.render(guiGraphics, mouseX, mouseY, partialTick));
        renderTooltips(guiGraphics, mouseX, mouseY);
    }

    private void renderBackgroundTexture(GuiGraphics guiGraphics) {
        RenderUtils.drawTexturedRect(guiGraphics, Texture.MANAGER_BACKGROUND, offsetX, offsetY);
    }

    private void renderNewProviderMenu(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
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
                            StyledText.fromComponent(
                                    Component.translatable("screens.wynntils.map.managers.providerManager.localProviderText")),
                            offsetX + WIDTH_OFFSET + 250 + 5 + 10 + 20,
                            offsetY + HEIGHT_OFFSET + 25 + 1,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
            // end region

            // The remote provider section is rendered by the RemoteProviderCreationWidget
        }
    }

    private void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (GuiEventListener child : children()) {
            if (!child.isMouseOver(mouseX, mouseY)) continue;

            TooltipProvider tooltipProvider = null;

            if (child instanceof TooltipProvider provider) {
                tooltipProvider = provider;

            } else if (child == remoteProviderCreationWidget) {
                SaveRemoteProviderButton saveButton = remoteProviderCreationWidget.getSaveButton();

                if (saveButton.isMouseOver(mouseX, mouseY)) {
                    tooltipProvider = saveButton;
                }
            }

            if (tooltipProvider == null) continue;

            List<Component> lines = tooltipProvider.getTooltipLines();
            if (lines.isEmpty()) continue;

            guiGraphics.setTooltipForNextFrame(
                    Lists.transform(lines, Component::getVisualOrderText), mouseX, mouseY);
            break;
        }
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        TextInputBoxWidget focusedBefore = getFocusedTextInput();
        boolean handled = super.doMouseClicked(event, isDoubleClick);

        TextInputBoxWidget focusedAfter = getFocusedTextInput();
        if (focusedAfter != null && focusedAfter == focusedBefore && !focusedAfter.isMouseOver(event.x(), event.y())) {
            setFocusedTextInput(null);
        }

        return handled;
    }

    @Override
    public void onClose() {
        McUtils.setScreen(previousScreen);
    }

    private void updateMenu() {
        providerInfoScrollBarWidget.visible = false;
        descriptionScrollBarWidget.visible = false;
        openLocalProviderFolderButton.visible = false;
        removeProviderButton.visible = false;
        remoteProviderCreationWidget.setVisible(providerManagerMode == ProviderManagerMode.NEW_PROVIDER);

        if (providerManagerMode == ProviderManagerMode.DEFAULT) {
            providerInfoScrollBarWidget.visible = true;
            descriptionScrollBarWidget.visible = true;
            descriptionScrollBarWidget.updateContentHeight();

            String providerUrl = Services.MapData.getProviderFromId(getSelectedProviderId())
                    .flatMap(Services.MapData::getJsonProviderInfo)
                    .map(JsonProviderInfo::providerUrl)
                    .orElse(null);

            if (providerUrl != null) {
                removeProviderButton.visible = true;
                descriptionScrollBarWidget.setHeight(79);
            } else {
                removeProviderButton.visible = false;
                descriptionScrollBarWidget.setHeight(79 + 25);
            }

        } else if (providerManagerMode == ProviderManagerMode.NEW_PROVIDER) {
            openLocalProviderFolderButton.visible = true;
        }
    }

    public void setSelectedProvider(String providerId, ProviderType providerType) {
        selectedProviderId = providerId;
        selectedProviderType = providerType;
        providerInfoScrollBarWidget.populateWidgets();

        Optional<MapDataProvider> providerOptional = Services.MapData.getProviderFromId(getSelectedProviderId());

        if (providerOptional.isEmpty()) {
            descriptionScrollBarWidget.setDescription(null);
            setProviderManagerMode(ProviderManagerMode.NONE);
            return;
        }

        if (providerOptional.isPresent()) {
            MapDataProvider provider = providerOptional.get();

            if (provider instanceof BuiltInProvider builtInProvider) {
                descriptionScrollBarWidget.setDescription(StyledText.fromComponent(builtInProvider.getDescription()));

            } else if (provider instanceof JsonProvider jsonProvider) {
                descriptionScrollBarWidget.setDescription(
                        jsonProvider.getDescription()
                                .map(StyledText::fromComponent)
                                .orElse(null));

            } else {
                descriptionScrollBarWidget.setDescription(null);
            }
        }

        descriptionScrollBarWidget.updateContentHeight();
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
