/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.Texture;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.screens.SearchableScreen;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;

public class QuestBookSearchWidget extends SearchWidget {
    public QuestBookSearchWidget(
            int x, int y, int width, int height, Consumer<String> onUpdateConsumer, SearchableScreen searchableScreen) {
        super(x, y, width, height, onUpdateConsumer, searchableScreen);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        checkForHeldBackspace();

        this.renderBg(poseStack, McUtils.mc(), mouseX, mouseY);

        boolean defaultText = Objects.equals(textBoxInput, "") && !isFocused();

        String renderedText = getRenderedText(this.width - 18);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        defaultText ? DEFAULT_TEXT.getString() : renderedText,
                        this.x + 17,
                        this.x + this.width - 5,
                        this.y + 11f,
                        this.width,
                        defaultText ? CommonColors.LIGHT_GRAY : CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        FontRenderer.TextShadow.NORMAL);
    }

    @Override
    protected void renderBg(PoseStack poseStack, Minecraft minecraft, int mouseX, int mouseY) {
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.QUEST_BOOK_SEARCH.resource(),
                this.x,
                this.y,
                0,
                this.width,
                this.height,
                Texture.QUEST_BOOK_SEARCH.width(),
                Texture.QUEST_BOOK_SEARCH.height());
    }
}
