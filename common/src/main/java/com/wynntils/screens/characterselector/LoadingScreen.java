/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.characterselector;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.WynntilsScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.network.chat.Component;

public final class LoadingScreen extends WynntilsScreen {
    private float currentTextureScale = 1f;

    public void setMessage(String message) {
        this.message = message;
    }

    String message;

    private LoadingScreen() {
        super(Component.translatable("screens.wynntils.characterSelection.name"));
    }

    public static LoadingScreen create() {
        return new LoadingScreen();
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        renderTitle(poseStack, message);
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.BACKGROUND_SPLASH.resource(),
                0,
                0,
                0,
                this.width,
                this.height,
                Texture.BACKGROUND_SPLASH.width(),
                Texture.BACKGROUND_SPLASH.height());
    }

    private void renderTitle(PoseStack poseStack, String titleString) {
        int txWidth = Texture.QUEST_BOOK_TITLE.width();
        int txHeight = Texture.QUEST_BOOK_TITLE.height();
        RenderUtils.drawScalingTexturedRect(
                poseStack, Texture.QUEST_BOOK_TITLE.resource(), 0, 30, 0, txWidth, txHeight, txWidth, txHeight);

        poseStack.pushPose();
        poseStack.scale(2f, 2f, 0f);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        titleString,
                        5,
                        18,
                        CommonColors.YELLOW,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        TextShadow.NORMAL);
        poseStack.popPose();
    }
}
