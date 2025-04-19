/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
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
    private static final ResourceLocation BOOK_OPEN_ID =
            ResourceLocation.fromNamespaceAndPath("wynntils", "ui.book.open");
    private static final SoundEvent BOOK_OPEN_SOUND = SoundEvent.createVariableRangeEvent(BOOK_OPEN_ID);

    protected WynntilsMenuScreenBase(Component component) {
        super(component);
    }

    public static void openBook(Screen screen) {
        McUtils.mc().setScreen(screen);
        McUtils.playSoundUI(BOOK_OPEN_SOUND);
    }

    protected void renderBackgroundTexture(PoseStack poseStack) {
        int txWidth = Texture.CONTENT_BOOK_BACKGROUND.width();
        int txHeight = Texture.CONTENT_BOOK_BACKGROUND.height();

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.CONTENT_BOOK_BACKGROUND.resource(),
                (this.width - txWidth) / 2f,
                (this.height - txHeight) / 2f,
                0,
                txWidth,
                txHeight,
                txWidth,
                txHeight);
    }

    protected void renderVersion(PoseStack poseStack) {
        String version = WynntilsMod.isDevelopmentBuild() ? "Development Build" : WynntilsMod.getVersion();
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString(version),
                        53f + getTranslationX(),
                        127f + getTranslationX(),
                        196 + getTranslationY(),
                        202 + getTranslationY(),
                        0,
                        CommonColors.YELLOW,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL,
                        0.7f);
    }

    protected void renderTitle(PoseStack poseStack, String titleString) {
        int txWidth = Texture.CONTENT_BOOK_TITLE.width();
        int txHeight = Texture.CONTENT_BOOK_TITLE.height();
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.CONTENT_BOOK_TITLE.resource(),
                0 + getTranslationX(),
                30 + getTranslationY(),
                0,
                txWidth,
                txHeight,
                txWidth,
                txHeight);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(titleString),
                        10 + getTranslationX(),
                        36 + getTranslationY(),
                        CommonColors.YELLOW,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL,
                        2f);
    }

    protected void renderDescription(PoseStack poseStack, String description, String filterHelper) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString(description),
                        20 + getTranslationX(),
                        Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 10 + getTranslationX(),
                        80 + getTranslationY(),
                        Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 30,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        TextShadow.NONE);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString(filterHelper),
                        20 + getTranslationX(),
                        Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 10 + getTranslationY(),
                        105 + getTranslationY(),
                        Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 30,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        TextShadow.NONE);
    }

    public float getTranslationX() {
        return (this.width - Texture.CONTENT_BOOK_BACKGROUND.width()) / 2f;
    }

    public float getTranslationY() {
        return (this.height - Texture.CONTENT_BOOK_BACKGROUND.height()) / 2f;
    }
}
