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

        int highlightedStart = Math.min(cursorPosition, highlightPosition);
        int highlightedEnd = Math.max(cursorPosition, highlightPosition);

        if (highlightedStart >= renderedTextDetails.b() && highlightedEnd <= renderedTextDetails.b() + renderedText.length()) {
            // Entirety of the highlighted text is within the rendered text
            highlightedStart -= renderedTextDetails.b();
            highlightedEnd -= renderedTextDetails.b();
        } else if (highlightedStart >= renderedTextDetails.b() && highlightedEnd > renderedTextDetails.b() + renderedText.length()) {
            // The highlighted text starts within the rendered text, but ends outside of it
            highlightedStart -= renderedTextDetails.b();
            highlightedEnd = renderedText.length();
        } else if (highlightedStart < renderedTextDetails.b() && highlightedEnd <= renderedTextDetails.b() + renderedText.length()) {
            // The highlighted text starts outside of the rendered text, but ends within it
            highlightedStart = 0;
            highlightedEnd -= renderedTextDetails.b();
        } else {
            // The highlighted text is not within the rendered text
            highlightedStart = 0;
            highlightedEnd = 0;
        }

        String firstNormalPortion = renderedText.substring(0, highlightedStart);
        String highlightedPortion = renderedText.substring(highlightedStart, highlightedEnd);
        String lastNormalPortion = renderedText.substring(highlightedEnd);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        defaultText ? DEFAULT_TEXT.getString() : firstNormalPortion,
                        this.getX() + textPadding,
                        this.getX() + this.width - textPadding - FontRenderer.getInstance().getFont().width(lastNormalPortion) - FontRenderer.getInstance().getFont().width(highlightedPortion),
                        this.getY() + 6.5f,
                        0,
                        defaultText ? CommonColors.LIGHT_GRAY : CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderAlignedHighlightedTextInBox(
                        poseStack,
                        highlightedPortion,
                        this.getX() + textPadding + FontRenderer.getInstance().getFont().width(firstNormalPortion),
                        this.getX() + this.width - textPadding - FontRenderer.getInstance().getFont().width(lastNormalPortion),
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
                        defaultText ? DEFAULT_TEXT.getString() : lastNormalPortion,
                        this.getX() + textPadding + FontRenderer.getInstance().getFont().width(firstNormalPortion) + FontRenderer.getInstance().getFont().width(highlightedPortion),
                        this.getX() + this.width - textPadding,
                        this.getY() + 6.5f,
                        0,
                        defaultText ? CommonColors.LIGHT_GRAY : CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        TextShadow.NORMAL);
        //System.out.println(isDragging);
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
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isDragging) {
            isDragging = false;
            setCursorPosition(getIndexAtPosition(mouseX));
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            setCursorPosition(getIndexAtPosition(mouseX));
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected void removeFocus() {
        this.setTextBoxInput("");
        super.removeFocus();
    }
}
