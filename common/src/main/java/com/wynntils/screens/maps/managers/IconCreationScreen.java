/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.managers.widgets.StyledButton;
import com.wynntils.screens.maps.managers.widgets.TexturedTextInputBoxWidget;
import com.wynntils.services.mapdata.impl.MapIconImpl;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.io.IOException;
import java.util.Base64;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class IconCreationScreen extends WynntilsScreen {
    private static final int BACKGROUND_HEIGHT = 124;
    private static final int BACKGROUND_WIDTH = 248;
    private static final int BUTTON_GAP = 10;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_TOP_GAP = 5;
    private static final int BUTTON_WIDTH = 80;
    private static final int ICON_BORDER = 4;

    private final Screen previousScreen;

    private int backgroundX;
    private int backgroundY;

    private Component errorText = Component.translatable("screens.wynntils.iconCreation.invalidName");
    private MapIconImpl newIcon;
    private StyledButton saveIconButton;
    private TexturedTextInputBoxWidget iconNameInput;
    private TexturedTextInputBoxWidget iconBase64Input;

    private IconCreationScreen(Screen previousScreen) {
        super(Component.literal("Icon Creation Screen"));

        this.previousScreen = previousScreen;
    }

    public static Screen create(Screen previousScreen) {
        return new IconCreationScreen(previousScreen);
    }

    @Override
    protected void doInit() {
        super.doInit();

        backgroundX = (this.width - BACKGROUND_WIDTH) / 2;
        backgroundY = (this.height - BACKGROUND_HEIGHT) / 2;

        iconNameInput = new TexturedTextInputBoxWidget(
                backgroundX + 60,
                backgroundY + 6,
                BACKGROUND_WIDTH - 70,
                20,
                (s) -> tryParseIcon(),
                this,
                TexturedTextInputBoxWidget.Mode.IDENTIFIER);
        this.addRenderableWidget(iconNameInput);

        iconBase64Input = new TexturedTextInputBoxWidget(
                backgroundX + 60,
                backgroundY + 30,
                BACKGROUND_WIDTH - 70,
                20,
                (s) -> tryParseIcon(),
                this,
                TexturedTextInputBoxWidget.Mode.STRING);
        iconBase64Input.setTooltip(
                Tooltip.create(Component.translatable("screens.wynntils.iconCreation.base64Tooltip")));
        this.addRenderableWidget(iconBase64Input);

        setFocusedTextInput(iconNameInput);

        this.addRenderableWidget(new StyledButton(
                (this.width / 2) - BUTTON_WIDTH - BUTTON_GAP / 2,
                backgroundY + BACKGROUND_HEIGHT + BUTTON_TOP_GAP,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconSelectionScreen.cancel"),
                Texture.MANAGER_WIDGET_BACKGROUND_RED,
                this::onClose));

        saveIconButton = new StyledButton(
                (this.width / 2) + BUTTON_GAP / 2,
                backgroundY + BACKGROUND_HEIGHT + BUTTON_TOP_GAP,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                Component.translatable("screens.wynntils.map.managers.categoryManager.iconSelectionScreen.save"),
                Texture.MANAGER_WIDGET_BACKGROUND_GREEN,
                () -> {
                    Services.Waypoints.addCustomIcon(newIcon);
                    this.onClose();
                });

        saveIconButton.active = newIcon != null;
        this.addRenderableWidget(saveIconButton);
    }

    @Override
    public void onClose() {
        McUtils.mc().setScreen(previousScreen);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_WIDGET_BACKGROUND,
                backgroundX,
                backgroundY,
                BACKGROUND_WIDTH,
                BACKGROUND_HEIGHT);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.iconCreation.iconName")),
                        backgroundX + 4,
                        backgroundY + 16,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString("base64"),
                        backgroundX + 4,
                        backgroundY + 40,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.iconCreation.help")),
                        backgroundX + BACKGROUND_WIDTH / 2f - 60,
                        backgroundX + BACKGROUND_WIDTH / 2f + 60,
                        backgroundY + BACKGROUND_HEIGHT - 40,
                        backgroundY + BACKGROUND_HEIGHT,
                        BACKGROUND_WIDTH,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        int previewTop = backgroundY + 60;
        int previewBottom = backgroundY + BACKGROUND_HEIGHT - 40;
        int previewCenterX = backgroundX + BACKGROUND_WIDTH / 2;

        if (newIcon == null) {
            Font font = FontRenderer.getInstance().getFont();

            int textWidth = font.width(errorText);
            int textHeight = font.lineHeight;

            int iconBoxWidth = textWidth + ICON_BORDER * 2;
            int iconBoxHeight = textHeight + ICON_BORDER * 2;

            int iconBoxX = previewCenterX - iconBoxWidth / 2;
            int iconBoxY = previewTop + (previewBottom - previewTop - iconBoxHeight) / 2;

            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics, Texture.MANAGER_TEXT_BOX_BACKGROUND, iconBoxX, iconBoxY, iconBoxWidth, iconBoxHeight);

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromComponent(errorText),
                            iconBoxX + iconBoxWidth / 2f,
                            iconBoxY + iconBoxHeight / 2f,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        } else {
            int iconWidth = newIcon.getWidth();
            int iconHeight = newIcon.getHeight();

            int iconBoxSize = Math.min(Math.max(iconWidth, iconHeight) + ICON_BORDER * 2, previewBottom - previewTop);

            int iconBoxX = previewCenterX - iconBoxSize / 2;
            int iconBoxY = previewTop + (previewBottom - previewTop - iconBoxSize) / 2;

            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics, Texture.MANAGER_TEXT_BOX_BACKGROUND, iconBoxX, iconBoxY, iconBoxSize, iconBoxSize);

            RenderUtils.drawTexturedRect(
                    guiGraphics,
                    RenderPipelines.GUI_TEXTURED,
                    newIcon.getIdentifier(),
                    CommonColors.WHITE,
                    iconBoxX + (iconBoxSize - iconWidth) / 2f,
                    iconBoxY + (iconBoxSize - iconHeight) / 2f,
                    iconWidth,
                    iconHeight,
                    0f,
                    0f,
                    iconWidth,
                    iconHeight,
                    iconWidth,
                    iconHeight);
        }

        renderables.forEach(renderable -> renderable.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    private void tryParseIcon() {
        if (iconNameInput.getTextBoxInput().isEmpty()
                || iconBase64Input.getTextBoxInput().isEmpty()) {
            newIcon = null;
            saveIconButton.active = false;
            errorText = Component.translatable("screens.wynntils.iconCreation."
                    + (iconNameInput.getTextBoxInput().isEmpty() ? "invalidName" : "invalidBase64"));
            return;
        }

        try {
            byte[] texture = Base64.getDecoder().decode(iconBase64Input.getTextBoxInput());
            newIcon = new MapIconImpl("wynntils:icon:personal:" + iconNameInput.getTextBoxInput(), texture);
            saveIconButton.active = true;
        } catch (IOException | IllegalArgumentException e) {
            WynntilsMod.warn("Bad icon texture for " + iconNameInput.getTextBoxInput(), e);
            newIcon = null;
            errorText = Component.translatable("screens.wynntils.iconCreation.invalidBase64");
        }
    }
}
