/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.TextShadow;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.objects.CommonColors;
import net.minecraft.network.chat.Component;

public abstract class WynntilsMenuScreenBase extends WynntilsScreen {
    protected WynntilsMenuScreenBase(Component component) {
        super(component);
    }

    protected void renderBackgroundTexture(PoseStack poseStack) {
        int txWidth = Texture.QUEST_BOOK_BACKGROUND.width();
        int txHeight = Texture.QUEST_BOOK_BACKGROUND.height();

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.QUEST_BOOK_BACKGROUND.resource(),
                (this.width - txWidth) / 2f,
                (this.height - txHeight) / 2f,
                0,
                txWidth,
                txHeight,
                txWidth,
                txHeight);
    }

    protected void renderVersion(PoseStack poseStack) {
        // FIXME: Replace with better scaling support

        poseStack.pushPose();
        String version = WynntilsMod.isDevelopmentBuild() ? "Development Build" : WynntilsMod.getVersion();
        poseStack.scale(0.7f, 0.7f, 0);
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        version,
                        59f * 1.3f,
                        (Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30f) * 1.3f,
                        Texture.QUEST_BOOK_BACKGROUND.height() * 1.3f - 6f,
                        0,
                        CommonColors.YELLOW,
                        HorizontalAlignment.Center,
                        TextShadow.NORMAL);
        poseStack.popPose();
    }

    protected void renderTitle(PoseStack poseStack, String titleString) {
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

    protected void renderDescription(PoseStack poseStack, String description) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        description,
                        20,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 10,
                        140,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30,
                        CommonColors.BLACK,
                        HorizontalAlignment.Left,
                        TextShadow.NONE);
    }

    public float getTranslationX() {
        return (this.width - Texture.QUEST_BOOK_BACKGROUND.width()) / 2f;
    }

    public float getTranslationY() {
        return (this.height - Texture.QUEST_BOOK_BACKGROUND.height()) / 2f;
    }
}
