/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
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
            GuiGraphics guiGraphics,
            String renderedText,
            int renderedTextStart,
            String firstPortion,
            String highlightedPortion,
            String lastPortion,
            Font font,
            int firstWidth,
            int highlightedWidth,
            int lastWidth,
            int mouseX,
            int mouseY) {
        boolean defaultText = Objects.equals(textBoxInput, "");

        if (this.isHovered) {
            guiGraphics.requestCursor(CursorTypes.IBEAM);
        }

        renderBackground(guiGraphics);

        renderText(
                guiGraphics,
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
            GuiGraphics guiGraphics,
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
                        guiGraphics,
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
                            guiGraphics,
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
                            guiGraphics,
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
                guiGraphics,
                this.getX()
                        + font.width(renderedText.substring(0, Math.min(cursorPosition, renderedText.length())))
                        + textPadding
                        - 2,
                this.getY() + VERTICAL_OFFSET,
                VerticalAlignment.TOP,
                false);
    }

    protected void renderBackground(GuiGraphics guiGraphics) {
        RenderUtils.drawRect(guiGraphics, CommonColors.BLACK, this.getX(), this.getY(), this.width, this.height);
        RenderUtils.drawRectBorders(
                guiGraphics,
                isHovered ? CommonColors.LIGHT_GRAY : CommonColors.GRAY,
                this.getX(),
                this.getY(),
                this.getX() + this.width,
                this.getY() + this.height,
                1f);
    }

    @Override
    protected int getMaxTextWidth() {
        return this.width - 18;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.x() >= this.getX()
                && event.x() <= this.getX() + this.width
                && event.y() >= this.getY()
                && event.y() <= this.getY() + this.height) {
            McUtils.playSoundUI(SoundEvents.UI_BUTTON_CLICK.value());
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_2) {
                setTextBoxInput("");
                setCursorAndHighlightPositions(0);
            } else {
                setCursorAndHighlightPositions(getIndexAtPosition(event.x()));
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
