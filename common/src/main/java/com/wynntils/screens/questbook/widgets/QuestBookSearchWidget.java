/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.questbook.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.SearchWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;

import java.util.Objects;
import java.util.function.Consumer;

public class QuestBookSearchWidget extends SearchWidget {
    public QuestBookSearchWidget(
            int x, int y, int width, int height, Consumer<String> onUpdateConsumer, TextboxScreen textboxScreen) {
        super(x, y, width, height, onUpdateConsumer, textboxScreen);
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.QUEST_BOOK_SEARCH.resource(),
                this.getX(),
                this.getY(),
                0,
                this.width,
                this.height,
                Texture.QUEST_BOOK_SEARCH.width(),
                Texture.QUEST_BOOK_SEARCH.height());

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
                        this.getX() + 17,
                        this.getX() + this.width - 5 - FontRenderer.getInstance().getFont().width(lastNormalPortion) - FontRenderer.getInstance().getFont().width(highlightedPortion),
                        this.getY() + 11f,
                        this.width,
                        defaultText ? CommonColors.LIGHT_GRAY : CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderAlignedHighlightedTextInBox(
                        poseStack,
                        highlightedPortion,
                        this.getX() + 17 + FontRenderer.getInstance().getFont().width(firstNormalPortion),
                        this.getX() + this.width - 5 - FontRenderer.getInstance().getFont().width(lastNormalPortion),
                        this.getY() + 11f,
                        this.getY() + 11f,
                        this.width,
                        CommonColors.BLUE,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        defaultText ? DEFAULT_TEXT.getString() : lastNormalPortion,
                        this.getX() + 17 + FontRenderer.getInstance().getFont().width(firstNormalPortion) + FontRenderer.getInstance().getFont().width(highlightedPortion),
                        this.getX() + this.width - 5,
                        this.getY() + 11f,
                        this.width,
                        defaultText ? CommonColors.LIGHT_GRAY : CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        TextShadow.NORMAL);
    }
}
