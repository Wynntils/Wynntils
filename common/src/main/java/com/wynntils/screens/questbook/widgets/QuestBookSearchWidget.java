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

        String renderedText = getRenderedText(this.width - 18).a();

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        defaultText ? DEFAULT_TEXT.getString() : renderedText,
                        this.getX() + 17,
                        this.getX() + this.width - 5,
                        this.getY() + 11f,
                        this.width,
                        defaultText ? CommonColors.LIGHT_GRAY : CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        TextShadow.NORMAL);
    }
}
