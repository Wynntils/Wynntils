/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.network.chat.Component;

public class SearchWidget extends TextInputBoxWidget {
    private static final Component DEFAULT_TEXT =
            Component.translatable("screens.wynntils.searchWidget.defaultSearchText");
    private static final float VERTICAL_OFFSET = 6.5f;

    public SearchWidget(
            int x, int y, int width, int height, Consumer<String> onUpdateConsumer, ContainerEventHandler focusAccess) {
        super(x, y, width, height, Component.literal("Search Box"), onUpdateConsumer, focusAccess);
        textPadding = 5;
    }

    @Override
    protected void doRenderWidget(
            PoseStack poseStack,
            String renderedText,
            String firstPortion,
            String highlightedPortion,
            String lastPortion,
            Font font,
            int firstWidth,
            int highlightedWidth,
            int lastWidth) {
        boolean defaultText = Objects.equals(textBoxInput, "") && !isFocused();

        renderBackground(poseStack);

        renderText(
                poseStack,
                renderedText,
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

        if (defaultText) return;

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
                CommonColors.GRAY,
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
    protected void removeFocus() {
        this.setTextBoxInput("");
        super.removeFocus();
    }

    public void opened() {
        setCursorPosition(textBoxInput.length());
        setHighlightPosition(0);
    }
}
