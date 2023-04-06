/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText2;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public abstract class WynntilsMenuScreenBase extends WynntilsScreen {
    private static final ResourceLocation BOOK_OPEN_ID = new ResourceLocation("wynntils:ui.book.open");
    private static final SoundEvent BOOK_OPEN_SOUND = SoundEvent.createVariableRangeEvent(BOOK_OPEN_ID);

    protected WynntilsMenuScreenBase(Component component) {
        super(component);
    }

    public static void openBook(Screen screen) {
        McUtils.mc().setScreen(screen);
        McUtils.playSoundUI(BOOK_OPEN_SOUND);
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
                        StyledText2.of(version),
                        59f * 1.3f,
                        (Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30f) * 1.3f,
                        Texture.QUEST_BOOK_BACKGROUND.height() * 1.3f - 6f,
                        0,
                        CommonColors.YELLOW,
                        HorizontalAlignment.CENTER,
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
                        StyledText2.of(titleString),
                        5,
                        18,
                        CommonColors.YELLOW,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
        poseStack.popPose();
    }

    protected void renderDescription(PoseStack poseStack, String description) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText2.of(description),
                        20,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 10,
                        140,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        TextShadow.NONE);
    }

    public float getTranslationX() {
        return (this.width - Texture.QUEST_BOOK_BACKGROUND.width()) / 2f;
    }

    public float getTranslationY() {
        return (this.height - Texture.QUEST_BOOK_BACKGROUND.height()) / 2f;
    }
}
