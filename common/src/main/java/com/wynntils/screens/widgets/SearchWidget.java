/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.Texture;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.screens.settings.WynntilsSettingsScreen;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class SearchWidget extends TextInputBoxWidget {
    private final Component DEFAULT_TEXT = new TranslatableComponent("screens.wynntils.searchWidget.defaultSearchText");

    public SearchWidget(
            int x,
            int y,
            int width,
            int height,
            Consumer<String> onUpdateConsumer,
            WynntilsSettingsScreen settingsScreen) {
        super(x, y, width, height, new TextComponent("Search..."), onUpdateConsumer, settingsScreen);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBg(poseStack, McUtils.mc(), mouseX, mouseY);

        String cursorChar = getRenderCursorChar();

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        Objects.equals(textBoxInput, "") && !isFocused()
                                ? DEFAULT_TEXT.getString()
                                : (textBoxInput.substring(0, cursorPosition)
                                        + cursorChar
                                        + textBoxInput.substring(cursorPosition)),
                        this.x + 5,
                        this.x + this.width - 5,
                        this.y + 6.5f,
                        this.width,
                        CommonColors.WHITE,
                        FontRenderer.TextAlignment.LEFT_ALIGNED,
                        FontRenderer.TextShadow.NORMAL);
    }

    @Override
    protected void renderBg(PoseStack poseStack, Minecraft minecraft, int mouseX, int mouseY) {
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.SEARCH_BAR.resource(),
                this.x,
                this.y,
                0,
                this.width,
                this.height,
                0,
                0,
                Texture.SEARCH_BAR.width(),
                Texture.SEARCH_BAR.height(),
                Texture.SEARCH_BAR.width(),
                Texture.SEARCH_BAR.height());
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    protected void removeFocus() {
        this.setTextBoxInput("");
        super.removeFocus();
    }
}
