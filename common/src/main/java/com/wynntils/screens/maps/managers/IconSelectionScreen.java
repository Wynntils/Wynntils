/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.managers.widgets.ManagerSearchWidget;
import com.wynntils.screens.maps.managers.widgets.StyledButton;
import com.wynntils.screens.maps.managers.widgets.TexturedTextInputBoxWidget;
import com.wynntils.screens.maps.managers.widgets.iconselection.IconGridScrollBarWidget;
import com.wynntils.screens.maps.managers.widgets.iconselection.IconSelectionProviderScrollBarWidget;
import com.wynntils.services.mapdata.impl.MapIconImpl;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class IconSelectionScreen extends WynntilsScreen {
    private static final int WIDTH_OFFSET = 15;
    private static final int HEIGHT_OFFSET = 18;

    public static final String PROVIDER_ALL = "all";
    public static final String PROVIDER_BUILT_IN = "built-in:icons";
    public static final String PROVIDER_CUSTOM = "built-in:waypoints";
    public static final String CUSTOM_ICON_PREFIX = "wynntils:icon:personal:";

    private final Screen previousScreen;
    private final Consumer<MapIcon> onIconSelect;

    private int offsetX;
    private int offsetY;

    private Mode mode = Mode.BROWSE;
    private String selectedProviderId = PROVIDER_ALL;
    private String selectedIconId;

    private ManagerSearchWidget providerSearchWidget;
    private IconSelectionProviderScrollBarWidget providerListScrollWidget;
    private StyledButton newIconButton;

    private IconGridScrollBarWidget iconGridScrollWidget;
    private StyledButton cancelButton;
    private StyledButton deleteButton;
    private StyledButton selectButton;

    private TexturedTextInputBoxWidget iconNameInput;
    private TexturedTextInputBoxWidget iconBase64Input;
    private StyledButton createCancelButton;
    private StyledButton createSaveButton;
    private MapIconImpl newIcon;
    private Component errorText = Component.translatable("screens.wynntils.map.iconSelectionScreen.invalidName");

    private IconSelectionScreen(Screen previousScreen, Consumer<MapIcon> onIconSelect, String currentIconId) {
        super(Component.literal("Icon Selection Screen"));
        this.previousScreen = previousScreen;
        this.onIconSelect = onIconSelect;
        this.selectedIconId = currentIconId == null || currentIconId.isEmpty() ? MapIcon.NO_ICON_ID : currentIconId;
    }

    public static IconSelectionScreen create(
            Screen previousScreen, Consumer<MapIcon> onIconSelect, String currentIconId) {
        return new IconSelectionScreen(previousScreen, onIconSelect, currentIconId);
    }

    @Override
    public void resize(int width, int height) {
        String name = iconNameInput != null ? iconNameInput.getTextBoxInput() : "";
        String base64 = iconBase64Input != null ? iconBase64Input.getTextBoxInput() : "";

        this.width = width;
        this.height = height;
        rebuildWidgets();

        if (iconNameInput != null && iconBase64Input != null) {
            iconNameInput.setTextBoxInput(name);
            iconBase64Input.setTextBoxInput(base64);
            tryParseIcon();
        }
    }

    @Override
    protected void doInit() {
        super.doInit();

        offsetX = (int) ((this.width - Texture.MANAGER_BACKGROUND.width()) / 2f);
        offsetY = (int) ((this.height - Texture.MANAGER_BACKGROUND.height()) / 2f);

        // Left column: provider search + provider list + new icon button
        providerListScrollWidget = new IconSelectionProviderScrollBarWidget(
                offsetX + WIDTH_OFFSET, offsetY + HEIGHT_OFFSET + 25, 200, 284 - 25 - 5 - 20, this);
        this.addRenderableWidget(providerListScrollWidget);

        providerSearchWidget = new ManagerSearchWidget(
                offsetX + WIDTH_OFFSET,
                offsetY + HEIGHT_OFFSET,
                200 - 5 - Texture.BUILD_LOADOUTS_SEARCH_ICON.width(),
                20,
                text -> {
                    if (providerListScrollWidget != null) {
                        providerListScrollWidget.setFilter(text);
                    }
                },
                this);
        this.addRenderableWidget(providerSearchWidget);

        newIconButton = new StyledButton(
                offsetX + WIDTH_OFFSET,
                offsetY + HEIGHT_OFFSET + 284 - 20,
                200,
                20,
                Component.translatable("screens.wynntils.map.iconSelectionScreen.newIcon"),
                Texture.MANAGER_WIDGET_BACKGROUND_GREEN,
                () -> setMode(Mode.CREATE));
        this.addRenderableWidget(newIconButton);

        // Right column: icon grid + buttons
        int rightColumnX = offsetX + WIDTH_OFFSET + 200 + 5;
        int browseButtonsY = offsetY + HEIGHT_OFFSET + 284 - 20;
        int buttonWidth = 100;
        int buttonGap = (341 - buttonWidth * 3) / 2;

        iconGridScrollWidget =
                new IconGridScrollBarWidget(rightColumnX, offsetY + HEIGHT_OFFSET + 25, 341, 284 - 25 - 5 - 20, this);
        this.addRenderableWidget(iconGridScrollWidget);

        cancelButton = new StyledButton(
                rightColumnX,
                browseButtonsY,
                buttonWidth,
                20,
                Component.translatable("screens.wynntils.map.iconSelectionScreen.cancel"),
                Texture.MANAGER_WIDGET_BACKGROUND_RED,
                this::onClose);
        this.addRenderableWidget(cancelButton);

        deleteButton = new StyledButton(
                rightColumnX + buttonGap + buttonWidth,
                browseButtonsY,
                buttonWidth,
                20,
                Component.translatable("screens.wynntils.map.iconSelectionScreen.delete"),
                Texture.MANAGER_WIDGET_BACKGROUND_RED,
                this::onDeleteClicked);
        this.addRenderableWidget(deleteButton);

        selectButton = new StyledButton(
                rightColumnX + buttonGap * 2 + buttonWidth * 2,
                browseButtonsY,
                buttonWidth,
                20,
                Component.translatable("screens.wynntils.map.iconSelectionScreen.select"),
                Texture.MANAGER_WIDGET_BACKGROUND_GREEN,
                Texture.MANAGER_WIDGET_BACKGROUND_DARKGREEN,
                this::onSelectClicked);
        this.addRenderableWidget(selectButton);

        // Create form
        iconNameInput = new TexturedTextInputBoxWidget(
                offsetX + WIDTH_OFFSET + 93 + 90,
                offsetY + HEIGHT_OFFSET + 63 + 14,
                360 - 90 - 14,
                24,
                text -> tryParseIcon(),
                this,
                TexturedTextInputBoxWidget.Mode.IDENTIFIER);
        this.addRenderableWidget(iconNameInput);

        iconBase64Input = new TexturedTextInputBoxWidget(
                offsetX + WIDTH_OFFSET + 93 + 90,
                offsetY + HEIGHT_OFFSET + 63 + 14 + 24 + 8,
                360 - 90 - 14,
                24,
                text -> tryParseIcon(),
                this,
                TexturedTextInputBoxWidget.Mode.STRING);
        iconBase64Input.setTooltip(
                Tooltip.create(Component.translatable("screens.wynntils.map.iconSelectionScreen.base64Tooltip")));
        this.addRenderableWidget(iconBase64Input);

        createCancelButton = new StyledButton(
                offsetX + WIDTH_OFFSET + 93 + 95,
                offsetY + HEIGHT_OFFSET + 63 + 146 + 8,
                80,
                20,
                Component.translatable("screens.wynntils.map.iconSelectionScreen.cancel"),
                Texture.MANAGER_WIDGET_BACKGROUND_RED,
                () -> {
                    resetCreateForm();
                    setMode(Mode.BROWSE);
                });
        this.addRenderableWidget(createCancelButton);

        createSaveButton = new StyledButton(
                offsetX + WIDTH_OFFSET + 93 + 95 + 80 + 10,
                offsetY + HEIGHT_OFFSET + 63 + 146 + 8,
                80,
                20,
                Component.translatable("screens.wynntils.map.iconSelectionScreen.create"),
                Texture.MANAGER_WIDGET_BACKGROUND_GREEN,
                Texture.MANAGER_WIDGET_BACKGROUND_DARKGREEN,
                () -> {
                    if (newIcon == null) {
                        return;
                    }
                    Services.Waypoints.addCustomIcon(newIcon);
                    onIconCreated(newIcon);
                    resetCreateForm();
                    setMode(Mode.BROWSE);
                });
        createSaveButton.active = false;
        this.addRenderableWidget(createSaveButton);

        providerListScrollWidget.syncSelection();
        iconGridScrollWidget.refreshIcons(true);

        setFocusedTextInput(mode == Mode.CREATE ? iconNameInput : null);

        updateModeVisibility();
        updateBrowseButtons();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (mode == Mode.CREATE) {
            renderCreateForm(guiGraphics);
        } else {
            RenderUtils.drawTexturedRect(guiGraphics, Texture.MANAGER_BACKGROUND, offsetX, offsetY);
        }

        renderables.forEach(renderable -> renderable.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    private void renderCreateForm(GuiGraphics guiGraphics) {
        FontRenderer fontRenderer = FontRenderer.getInstance();

        int cardX = offsetX + WIDTH_OFFSET + 93;
        int cardY = offsetY + HEIGHT_OFFSET + 63;

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_WIDGET_BACKGROUND,
                cardX,
                cardY - Texture.MANAGER_SCROLL_BAR_HEADER.height() / 2f,
                360,
                146 + Texture.MANAGER_SCROLL_BAR_HEADER.height() / 2f);

        int headerX = cardX + 10;
        int headerY = cardY - Texture.MANAGER_SCROLL_BAR_HEADER.height() + 1;

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_SCROLL_BAR_HEADER,
                headerX,
                headerY,
                170,
                Texture.MANAGER_SCROLL_BAR_HEADER.height());

        fontRenderer.renderText(
                guiGraphics,
                StyledText.fromComponent(Component.translatable("screens.wynntils.map.iconSelectionScreen.newIcon")),
                headerX + 10,
                headerY + Texture.MANAGER_SCROLL_BAR_HEADER.height() / 2f,
                CommonColors.WHITE,
                HorizontalAlignment.LEFT,
                VerticalAlignment.MIDDLE,
                TextShadow.NORMAL);

        // Labels, vertically centered on their input rows
        fontRenderer.renderText(
                guiGraphics,
                StyledText.fromComponent(Component.translatable("screens.wynntils.map.iconSelectionScreen.iconName")),
                cardX + 14,
                cardY + 14 + 12,
                CommonColors.WHITE,
                HorizontalAlignment.LEFT,
                VerticalAlignment.MIDDLE,
                TextShadow.NORMAL);

        fontRenderer.renderText(
                guiGraphics,
                StyledText.fromString("base64"),
                cardX + 14,
                cardY + 14 + 24 + 8 + 12,
                CommonColors.WHITE,
                HorizontalAlignment.LEFT,
                VerticalAlignment.MIDDLE,
                TextShadow.NORMAL);

        // Preview tile under the labels
        int tileX = cardX + 14 + 11;
        int tileY = cardY + 14 + 24 + 8 + 24 + 12;

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.MANAGER_TEXT_BOX_BACKGROUND, tileX, tileY, 48, 48);

        Component status;
        CustomColor statusColor;

        if (newIcon == null) {
            status = errorText;
            statusColor = CustomColor.fromInt(0xbf3b46); // red
        } else {
            int iconWidth = newIcon.getWidth();
            int iconHeight = newIcon.getHeight();
            int largestSide = Math.max(iconWidth, iconHeight);

            float scale = largestSide >= 40 ? 40f / largestSide : Math.max(1, 40 / largestSide);
            float drawWidth = iconWidth * scale;
            float drawHeight = iconHeight * scale;

            RenderUtils.drawScalingTexturedRect(
                    guiGraphics,
                    newIcon.getIdentifier(),
                    CommonColors.WHITE,
                    tileX + (48 - drawWidth) / 2f,
                    tileY + (48 - drawHeight) / 2f,
                    drawWidth,
                    drawHeight,
                    iconWidth,
                    iconHeight);

            status = Component.literal(iconWidth + "x" + iconHeight);
            statusColor = CustomColor.fromInt(0x6bc367); // green
        }

        // Status and help text to the right of the tile, under the inputs
        fontRenderer.renderAlignedTextInBox(
                guiGraphics,
                StyledText.fromComponent(status),
                cardX + 90,
                cardX + 360 - 14,
                tileY + 2,
                cardY + 146 - 16,
                360 - 90 - 14,
                statusColor,
                HorizontalAlignment.LEFT,
                VerticalAlignment.TOP,
                TextShadow.NORMAL);

        fontRenderer.renderAlignedTextInBox(
                guiGraphics,
                StyledText.fromComponent(Component.translatable("screens.wynntils.map.iconSelectionScreen.help")),
                cardX + 90,
                cardX + 360 - 14,
                tileY + 2 + fontRenderer.getFont().lineHeight + 4,
                cardY + 146 - 16,
                360 - 90 - 14,
                CommonColors.WHITE,
                HorizontalAlignment.LEFT,
                VerticalAlignment.TOP,
                TextShadow.NORMAL);
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
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE && mode == Mode.CREATE) {
            resetCreateForm();
            setMode(Mode.BROWSE);
            return true;
        }

        if (event.key() == InputConstants.KEY_TAB) {
            List<TextInputBoxWidget> inputs = new ArrayList<>();
            for (GuiEventListener child : children()) {
                if (child instanceof TextInputBoxWidget input && input.visible) {
                    inputs.add(input);
                }
            }

            if (!inputs.isEmpty()) {
                int index = inputs.indexOf(getFocusedTextInput());
                setFocusedTextInput(inputs.get((index + 1) % inputs.size()));
            }
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        McUtils.setScreen(previousScreen);
    }

    public String getSelectedProviderId() {
        return selectedProviderId;
    }

    public void setSelectedProviderId(String providerId) {
        if (providerId == null || selectedProviderId.equals(providerId)) return;

        selectedProviderId = providerId;
        iconGridScrollWidget.refreshIcons(true);
        updateBrowseButtons();
    }

    public String getSelectedIconId() {
        return selectedIconId;
    }

    public void setSelectedIconId(String iconId) {
        selectedIconId = iconId;
        updateBrowseButtons();
    }

    public boolean isCustomIconId(String iconId) {
        return iconId != null && iconId.startsWith(CUSTOM_ICON_PREFIX);
    }

    public List<MapIcon> getIconsForSelectedProvider() {
        if (PROVIDER_ALL.equals(selectedProviderId)) {
            return Services.MapData.getIcons()
                    .sorted(Comparator.comparing(MapIcon::getIconId))
                    .toList();
        }

        return getIconsFromProvider(selectedProviderId);
    }

    private List<MapIcon> getIconsFromProvider(String completeProviderId) {
        if (!Services.MapData.isProviderEnabled(completeProviderId)) return List.of();

        return Services.MapData.getProviderFromId(completeProviderId)
                .map(provider -> provider.getIcons()
                        .sorted(Comparator.comparing(MapIcon::getIconId))
                        .toList())
                .orElse(List.of());
    }

    private void onDeleteClicked() {
        if (selectedIconId == null || !isCustomIconId(selectedIconId)) return;

        Services.MapData.getIcon(selectedIconId)
                .filter(MapIconImpl.class::isInstance)
                .map(MapIconImpl.class::cast)
                .ifPresent(Services.Waypoints::removeCustomIcon);

        setSelectedIconId(null);
        providerListScrollWidget.populateWidgets();
        iconGridScrollWidget.refreshIcons(false);
        updateBrowseButtons();
    }

    private void onSelectClicked() {
        if (selectedIconId == null || selectedIconId.isEmpty()) return;

        if (selectedIconId.equals(MapIcon.NO_ICON_ID)) {
            onIconSelect.accept(null);
        } else {
            Optional<MapIcon> icon = Services.MapData.getIcon(selectedIconId);

            if (icon.isEmpty()) {
                setSelectedIconId(null);
                iconGridScrollWidget.refreshIcons(false);
                return;
            }

            onIconSelect.accept(icon.get());
        }

        this.onClose();
    }

    private void onIconCreated(MapIconImpl icon) {
        selectedIconId = icon.getIconId();
        selectedProviderId = PROVIDER_CUSTOM;
        providerListScrollWidget.syncSelection();
        iconGridScrollWidget.refreshIcons(true);
        updateBrowseButtons();
    }

    private void setMode(Mode newMode) {
        mode = newMode;
        updateModeVisibility();
        if (newMode == Mode.CREATE) {
            setFocusedTextInput(iconNameInput);
        } else {
            setFocusedTextInput(null);
            updateBrowseButtons();
        }
    }

    private void updateModeVisibility() {
        boolean browse = mode == Mode.BROWSE;

        providerSearchWidget.visible = browse;
        providerListScrollWidget.visible = browse;
        newIconButton.visible = browse;

        iconGridScrollWidget.visible = browse;
        cancelButton.visible = browse;
        selectButton.visible = browse;
        deleteButton.visible = browse && isSelectedIconCustom();

        iconNameInput.visible = !browse;
        iconBase64Input.visible = !browse;
        createCancelButton.visible = !browse;
        createSaveButton.visible = !browse;
    }

    private void updateBrowseButtons() {
        if (deleteButton == null) return;

        deleteButton.visible = mode == Mode.BROWSE && isSelectedIconCustom();
        selectButton.active = selectedIconId != null && !selectedIconId.isEmpty();
    }

    private boolean isSelectedIconCustom() {
        return isCustomIconId(selectedIconId);
    }

    private void tryParseIcon() {
        if (createSaveButton == null) return;

        String name = iconNameInput.getTextBoxInput().trim();
        String base64 = iconBase64Input.getTextBoxInput().trim();

        if (name.isEmpty() || base64.isEmpty()) {
            newIcon = null;
            createSaveButton.active = false;
            errorText = Component.translatable(
                    "screens.wynntils.map.iconSelectionScreen." + (name.isEmpty() ? "invalidName" : "invalidBase64"));
            return;
        }

        String candidateId = CUSTOM_ICON_PREFIX + name;
        boolean duplicate = Services.Waypoints.getCustomIcons().stream()
                .anyMatch(i -> i.getIconId().equals(candidateId));
        if (duplicate) {
            newIcon = null;
            createSaveButton.active = false;
            errorText = Component.translatable("screens.wynntils.map.iconSelectionScreen.duplicateName");
            return;
        }

        try {
            byte[] texture = Base64.getDecoder().decode(base64);
            newIcon = new MapIconImpl(candidateId, texture);
            createSaveButton.active = true;
        } catch (IOException | IllegalArgumentException e) {
            WynntilsMod.warn("Bad icon texture for " + name, e);

            newIcon = null;
            createSaveButton.active = false;
            errorText = Component.translatable("screens.wynntils.map.iconSelectionScreen.invalidBase64");
        }
    }

    private void resetCreateForm() {
        if (iconNameInput != null) {
            iconNameInput.setTextBoxInput("");
        }
        if (iconBase64Input != null) {
            iconBase64Input.setTextBoxInput("");
        }
        newIcon = null;
        errorText = Component.translatable("screens.wynntils.map.iconSelectionScreen.invalidName");
        if (createSaveButton != null) {
            createSaveButton.active = false;
        }
    }

    private enum Mode {
        BROWSE,
        CREATE
    }
}
