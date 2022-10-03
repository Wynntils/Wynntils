/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.screens.SearchableScreen;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.utils.McUtils;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;

public class SearchWidget extends TextInputBoxWidget {
    protected static final Component DEFAULT_TEXT =
            new TranslatableComponent("screens.wynntils.searchWidget.defaultSearchText");

    public SearchWidget(
            int x, int y, int width, int height, Consumer<String> onUpdateConsumer, SearchableScreen searchableScreen) {
        super(x, y, width, height, new TextComponent("SearchTextBox"), onUpdateConsumer, searchableScreen);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBg(poseStack, McUtils.mc(), mouseX, mouseY);

        boolean defaultText = Objects.equals(textBoxInput, "") && !isFocused();

        String renderedText = getRenderedText(this.width - 18);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        defaultText ? DEFAULT_TEXT.getString() : renderedText,
                        this.x + 5,
                        this.x + this.width - 5,
                        this.y + 6.5f,
                        this.width,
                        defaultText ? CommonColors.LIGHT_GRAY : CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        FontRenderer.TextShadow.NORMAL);
    }

    @Override
    protected void renderBg(PoseStack poseStack, Minecraft minecraft, int mouseX, int mouseY) {
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.SEARCH_BAR.resource(),
                this.x,
                this.y,
                0,
                this.width,
                this.height,
                Texture.SEARCH_BAR.width(),
                Texture.SEARCH_BAR.height());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height) {
            McUtils.soundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            searchableScreen.setFocusedTextInput(this);

            return true;
        }

        return false;
    }

    @Override
    protected void removeFocus() {
        this.setTextBoxInput("");
        super.removeFocus();
    }
}
