/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

public class SearchWidget extends TextInputBoxWidget {
    private static final Component DEFAULT_TEXT =
            Component.translatable("screens.wynntils.searchWidget.defaultSearchText");

    protected static final float VERTICAL_OFFSET = 6.5f;

    public SearchWidget(
            int x, int y, int width, int height, Consumer<String> onUpdateConsumer, TextboxScreen textboxScreen) {
        super(x, y, width, height, Component.literal("Search Box"), onUpdateConsumer, textboxScreen);
        textPadding = 5;
    }

    @Override
    protected void doRenderWidget(
            PoseStack poseStack,
            String renderedText,
            int renderedTextStart,
            String firstPortion,
            String highlightedPortion,
            String lastPortion,
            Font font,
            int firstWidth,
            int highlightedWidth,
            int lastWidth) {
        boolean defaultText = Objects.equals(textBoxInput, "");

        renderBackground(poseStack);

        renderText(
                poseStack,
                renderedText,
                renderedTextStart,
                firstPortion,
                highlightedPortion,
                lastPortion,
                font,
                firstWidth,
                highlightedWidth,
                lastWidth,
                defaultText);
    }

    protected void renderText(
            PoseStack poseStack,
            String renderedText,
            int renderedTextStart,
            String firstPortion,
            String highlightedPortion,
            String lastPortion,
            Font font,
            int firstWidth,
            int highlightedWidth,
            int lastWidth,
            boolean defaultText) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString(defaultText ? DEFAULT_TEXT.getString() : firstPortion),
                        this.getX() + textPadding,
                        this.getX() + this.width - textPadding - lastWidth - highlightedWidth,
                        this.getY() + VERTICAL_OFFSET,
                        0,
                        defaultText ? CommonColors.LIGHT_GRAY : CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        TextShadow.NORMAL);

        if (!defaultText) {
            FontRenderer.getInstance()
                    .renderAlignedHighlightedTextInBox(
                            poseStack,
                            StyledText.fromString(highlightedPortion),
                            this.getX() + textPadding + firstWidth,
                            this.getX() + this.width - textPadding - lastWidth,
                            this.getY() + VERTICAL_OFFSET,
                            this.getY() + VERTICAL_OFFSET,
                            0,
                            CommonColors.BLUE,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP);

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            StyledText.fromString(lastPortion),
                            this.getX() + textPadding + firstWidth + highlightedWidth,
                            this.getX() + this.width - textPadding,
                            this.getY() + VERTICAL_OFFSET,
                            0,
                            defaultText ? CommonColors.LIGHT_GRAY : CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            TextShadow.NORMAL);
        }

        drawCursor(
                poseStack,
                this.getX()
                        + font.width(renderedText.substring(0, Math.min(cursorPosition, renderedText.length())))
                        + textPadding
                        - 2,
                this.getY() + VERTICAL_OFFSET,
                VerticalAlignment.TOP,
                false);
    }

    protected void renderBackground(PoseStack poseStack) {
        RenderUtils.drawRect(poseStack, CommonColors.BLACK, this.getX(), this.getY(), 0, this.width, this.height);
        RenderUtils.drawRectBorders(
                poseStack,
                isHovered ? CommonColors.LIGHT_GRAY : CommonColors.GRAY,
                this.getX(),
                this.getY(),
                this.getX() + this.width,
                this.getY() + this.height,
                0,
                1f);
    }

    @Override
    protected int getMaxTextWidth() {
        return this.width - 18;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= this.getX()
                && mouseX <= this.getX() + this.width
                && mouseY >= this.getY()
                && mouseY <= this.getY() + this.height) {
            McUtils.playSoundUI(SoundEvents.UI_BUTTON_CLICK.value());
            if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
                setTextBoxInput("");
                setCursorAndHighlightPositions(0);
            } else {
                setCursorAndHighlightPositions(getIndexAtPosition(mouseX));
            }
            isDragging = true;
            textboxScreen.setFocusedTextInput(this);
            return true;
        } else {
            textboxScreen.setFocusedTextInput(null);
        }

        return false;
    }

    @Override
    protected void removeFocus() {
        this.setTextBoxInput("");
        super.removeFocus();
    }

    public void opened() {
        setCursorPosition(textBoxInput.length());
        setHighlightPosition(0);
    }
}
