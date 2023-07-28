/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.services.itemfilter.SearchQuery;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class ContainerSearchWidget extends SearchWidget {
    private SearchQuery searchQuery;

    private Consumer<SearchQuery> onSearchQueryUpdateConsumer;

    public ContainerSearchWidget(
            int x,
            int y,
            int width,
            int height,
            Consumer<SearchQuery> onSearchQueryUpdateConsumer,
            TextboxScreen textboxScreen) {
        super(x, y, width, height, null, textboxScreen);
        this.onSearchQueryUpdateConsumer = onSearchQueryUpdateConsumer;
        this.searchQuery = SearchQuery.fromQueryString("");
    }

    @Override
    protected void renderText(
            PoseStack poseStack,
            Pair<String, Integer> renderedTextDetails,
            String firstPortion,
            String highlightedPortion,
            String lastPortion,
            Font font,
            int firstWidth,
            int highlightedWidth,
            int lastWidth,
            boolean defaultText) {
        if (defaultText || !getTextBoxInput().equals(searchQuery.getQueryString())) {
            super.renderText(
                    poseStack,
                    renderedTextDetails,
                    firstPortion,
                    highlightedPortion,
                    lastPortion,
                    font,
                    firstWidth,
                    highlightedWidth,
                    lastWidth,
                    defaultText);
            return;
        }

        int charIndex = renderedTextDetails.b();

        StyledText firstPortionStyled = StyledText.EMPTY;
        for (char c : firstPortion.toCharArray())
            firstPortionStyled = firstPortionStyled.append(getStyledText(charIndex++, c));

        StyledText highlightedPortionStyled = StyledText.EMPTY;
        for (char c : highlightedPortion.toCharArray())
            highlightedPortionStyled = highlightedPortionStyled.append(getStyledText(charIndex++, c));

        StyledText lastPortionStyled = StyledText.EMPTY;
        for (char c : lastPortion.toCharArray())
            lastPortionStyled = lastPortionStyled.append(getStyledText(charIndex++, c));

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        firstPortionStyled,
                        this.getX() + textPadding,
                        this.getX() + this.width - textPadding - lastWidth - highlightedWidth,
                        this.getY() + VERTICAL_OFFSET,
                        0,
                        defaultText ? CommonColors.LIGHT_GRAY : CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderAlignedHighlightedTextInBox(
                        poseStack,
                        highlightedPortionStyled,
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
                        lastPortionStyled,
                        this.getX() + textPadding + firstWidth + highlightedWidth,
                        this.getX() + this.width - textPadding,
                        this.getY() + VERTICAL_OFFSET,
                        0,
                        defaultText ? CommonColors.LIGHT_GRAY : CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        TextShadow.NORMAL);

        String renderedText = renderedTextDetails.a();
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

    private StyledText getStyledText(int charIndex, char c) {
        if (searchQuery.getIgnoredCharIndices().contains(charIndex))
            return StyledText.fromComponent(Component.literal(String.valueOf(c)).withStyle(ChatFormatting.RED));
        else if (searchQuery.getValidFilterCharIndices().contains(charIndex))
            return StyledText.fromComponent(Component.literal(String.valueOf(c)).withStyle(ChatFormatting.YELLOW));
        else return StyledText.fromString(String.valueOf(c));
    }

    @Override
    protected int getMaxTextWidth() {
        return super.getMaxTextWidth() - 4;
    }

    @Override
    protected void onUpdate(String text) {
        searchQuery = SearchQuery.fromQueryString(text);
        onSearchQueryUpdateConsumer.accept(searchQuery);

        if (searchQuery.getErrors().isEmpty()) setTooltip(null);
        else
            setTooltip(Tooltip.create(Component.literal(String.join("\n\n", searchQuery.getErrors()))
                    .withStyle(ChatFormatting.RED)));
    }
}
