/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.loading;

import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.RenderHoverDirection;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public final class LoadingScreen extends WynntilsScreen {
    private static final String LOGO_STRING = "\uE005\uDAFF\uDFFF\uE006";
    private static final String TEXT_LOGO_STRING = "Wynncraft";
    private static final FontDescription LOGO_FONT_LOCATION =
            new FontDescription.Resource(ResourceLocation.withDefaultNamespace("screen/static"));
    private static final CustomColor MOSS_GREEN = CustomColor.fromInt(0x527529).withAlpha(255);
    private static final int SPINNER_SPEED = 1200;
    private final Runnable onClose;

    private int offsetX;
    private int offsetY;

    private String message = "";
    private String stageTitle = "";
    private String subtitle = "";

    private LoadingScreen(Runnable onClose) {
        super(Component.translatable("screens.wynntils.characterSelection.name"));
        this.onClose = onClose;
    }

    public static LoadingScreen create(Runnable onClose) {
        return new LoadingScreen(onClose);
    }

    @Override
    public void onClose() {
        ClientPacketListener connection = McUtils.mc().getConnection();
        if (connection != null) {
            connection.close();
        }
        onClose.run();

        super.onClose();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStageTitle(String stageTitle) {
        this.stageTitle = stageTitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    public void doInit() {
        super.doInit();

        offsetX = (int) ((this.width - Texture.SCROLL_BACKGROUND.width()) / 2f);
        offsetY = (int) ((this.height - Texture.SCROLL_BACKGROUND.height()) / 2f);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int textureWidth = Texture.BACKGROUND_SPLASH.width();
        int textureHeight = Texture.BACKGROUND_SPLASH.height();
        float widthScaleFactor = (float) this.width / textureWidth;
        float heightScaleFactor = (float) this.height / textureHeight;
        float scaleFactor = Math.max(widthScaleFactor, heightScaleFactor);

        float scaledWidth = textureWidth * scaleFactor;
        float scaledHeight = textureHeight * scaleFactor;

        // Draw background
        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                Texture.BACKGROUND_SPLASH.resource(),
                (int) ((this.width - scaledWidth) / 2f),
                (int) ((this.height - scaledHeight) / 2f),
                (int) scaledWidth,
                (int) scaledHeight,
                textureWidth,
                textureHeight);

        // Draw notebook background
        RenderUtils.drawTexturedRect(guiGraphics, Texture.SCROLL_BACKGROUND, offsetX, offsetY);

        // Draw logo
        int centerX = (int) (Texture.SCROLL_BACKGROUND.width() / 2f + 15 + offsetX);
        Component logoComponent = Services.ResourcePack.isPreloadedPackSelected()
                ? Component.literal(LOGO_STRING).withStyle(Style.EMPTY.withFont(LOGO_FONT_LOCATION))
                : Component.literal(TEXT_LOGO_STRING);
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(logoComponent),
                        centerX,
                        60 + offsetY,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);

        // Draw loading progress
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(message),
                        centerX,
                        100 + offsetY,
                        MOSS_GREEN,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);

        // Draw additional messages (typically about queue position)
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(stageTitle),
                        centerX,
                        120 + offsetY,
                        MOSS_GREEN,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(subtitle),
                        centerX,
                        130 + offsetY,
                        MOSS_GREEN,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);

        // Draw spinner
        boolean state = (System.currentTimeMillis() % SPINNER_SPEED) < SPINNER_SPEED / 2;
        drawSpinner(guiGraphics, centerX, 150 + offsetY, state);
    }

    private void drawSpinner(GuiGraphics guiGraphics, int x, int y, boolean state) {
        RenderUtils.drawHoverableTexturedRect(
                guiGraphics,
                Texture.RELOAD_ICON_OFFSET,
                (int) (x - (Texture.RELOAD_ICON_OFFSET.width() / 2f) / 2f),
                y,
                state,
                RenderHoverDirection.HORIZONTAL);
    }
}
