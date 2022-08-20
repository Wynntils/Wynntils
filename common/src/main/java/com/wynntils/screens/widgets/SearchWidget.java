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
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

// FIXME: This is a very basic text box. Selection, and cursor does not work.
public class SearchWidget extends AbstractWidget {
    private final Component DEFAULT_TEXT = new TranslatableComponent("screens.wynntils.searchWidget.defaultSearchText");

    private String searchText = "";

    public SearchWidget(int x, int y, int width, int height) {
        super(x, y, width, height, new TextComponent("Search..."));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBg(poseStack, McUtils.mc(), mouseX, mouseY);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        Objects.equals(searchText, "") ? DEFAULT_TEXT.getString() : searchText,
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
    public void onClick(double mouseX, double mouseY) {
        this.changeFocus(true);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (Character.isLetterOrDigit(codePoint)) {
            if (searchText == null) {
                searchText = "";
            }

            searchText += codePoint;
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (searchText.length() == 0) {
                return false;
            }

            searchText = searchText.substring(0, searchText.length() - 1);
        }

        return false;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    public String getSearchText() {
        return searchText;
    }
}
