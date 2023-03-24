/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import java.util.Objects;
import java.util.function.Consumer;

import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class SearchWidget extends TextInputBoxWidget {
    protected static final Component DEFAULT_TEXT =
            Component.translatable("screens.wynntils.searchWidget.defaultSearchText");

    public SearchWidget(
            int x, int y, int width, int height, Consumer<String> onUpdateConsumer, TextboxScreen textboxScreen) {
        super(x, y, width, height, Component.literal("Search Box"), onUpdateConsumer, textboxScreen);
        textPadding = 5;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
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

        boolean defaultText = Objects.equals(textBoxInput, "") && !isFocused();

        Pair<String, Integer> renderedTextDetails = getRenderedText(this.width - 18, true);
        String renderedText = renderedTextDetails.a();

        Pair<Integer, Integer> highlightedOutputInterval = getRenderedHighlighedInterval(renderedText, renderedTextDetails.b());

        String firstPortion = renderedText.substring(0, highlightedOutputInterval.a());
        String highlightedPortion = renderedText.substring(highlightedOutputInterval.a(), highlightedOutputInterval.b());
        String lastPortion = renderedText.substring(highlightedOutputInterval.b());

        int firstWidth = FontRenderer.getInstance().getFont().width(firstPortion);
        int highlightedWidth = FontRenderer.getInstance().getFont().width(highlightedPortion);
        int lastWidth = FontRenderer.getInstance().getFont().width(lastPortion);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        defaultText ? DEFAULT_TEXT.getString() : firstPortion,
                        this.getX() + textPadding,
                        this.getX() + this.width - textPadding - lastWidth - highlightedWidth,
                        this.getY() + 6.5f,
                        0,
                        defaultText ? CommonColors.LIGHT_GRAY : CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        TextShadow.NORMAL);

        if (defaultText) return;

        FontRenderer.getInstance()
                .renderAlignedHighlightedTextInBox(
                        poseStack,
                        highlightedPortion,
                        this.getX() + textPadding + firstWidth,
                        this.getX() + this.width - textPadding - lastWidth,
                        this.getY() + 6.5f,
                        this.getY() + 6.5f,
                        0,
                        CommonColors.BLUE,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        lastPortion,
                        this.getX() + textPadding + firstWidth + highlightedWidth,
                        this.getX() + this.width - textPadding,
                        this.getY() + 6.5f,
                        0,
                        defaultText ? CommonColors.LIGHT_GRAY : CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        TextShadow.NORMAL);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= this.getX()
                && mouseX <= this.getX() + this.width
                && mouseY >= this.getY()
                && mouseY <= this.getY() + this.height) {
            McUtils.playSound(SoundEvents.UI_BUTTON_CLICK.value());
            setCursorAndHighlightPositions(getIndexAtPosition(mouseX));
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
}
