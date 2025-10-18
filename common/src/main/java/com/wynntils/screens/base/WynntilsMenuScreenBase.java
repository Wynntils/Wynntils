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

    protected int offsetX;
    protected int offsetY;

    protected WynntilsMenuScreenBase(Component component) {
        super(component);
    }

    @Override
    protected void doInit() {
        super.doInit();

        offsetX = (int) ((this.width - Texture.CONTENT_BOOK_BACKGROUND.width()) / 2f);
        offsetY = (int) ((this.height - Texture.CONTENT_BOOK_BACKGROUND.height()) / 2f);
    }

    public static void openBook(Screen screen) {
        McUtils.setScreen(screen);
        McUtils.playSoundUI(BOOK_OPEN_SOUND);
    }

    protected void renderBackgroundTexture(PoseStack poseStack) {
        RenderUtils.drawTexturedRect(poseStack, Texture.CONTENT_BOOK_BACKGROUND, offsetX, offsetY);
    }

    protected void renderVersion(PoseStack poseStack) {
        String version = WynntilsMod.isDevelopmentBuild() ? "Development Build" : WynntilsMod.getVersion();
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString(version),
                        54 + offsetX,
                        127 + offsetX,
                        196 + offsetY,
                        202 + offsetY,
                        0,
                        CommonColors.YELLOW,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        0.7f);
    }

    protected void renderTitle(PoseStack poseStack, String titleString) {
        RenderUtils.drawTexturedRect(poseStack, Texture.CONTENT_BOOK_TITLE, offsetX, 30 + offsetY);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(titleString),
                        10 + offsetX,
                        36 + offsetY,
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
                        20 + offsetX,
                        Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 10 + offsetX,
                        80 + offsetY,
                        Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 30,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        TextShadow.NONE);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString(filterHelper),
                        20 + offsetX,
                        Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 10 + offsetY,
                        105 + offsetY,
                        Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 30,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        TextShadow.NONE);
    }
}
