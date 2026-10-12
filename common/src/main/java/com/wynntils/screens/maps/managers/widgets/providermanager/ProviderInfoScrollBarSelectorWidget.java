/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.managers.ProviderManagementScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ProviderInfoScrollBarSelectorWidget extends AbstractWidget {
    private final StyledText text;
    private final ProviderInfoScrollBarWidget.Mode scrollListCategory;
    private final int x;
    private final int y;
    private final ProviderManagementScreen parent;

    public ProviderInfoScrollBarSelectorWidget(
            StyledText text,
            ProviderInfoScrollBarWidget.Mode scrollListCategory,
            int x,
            int y,
            int width,
            int height,
            ProviderManagementScreen parent) {
        super(x, y, width, height, Component.literal("Provider Info Scroll Bar Selector Widget"));
        this.text = text;
        this.scrollListCategory = scrollListCategory;
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);

        if (parent.providerInfoScrollBarWidget.getSelectedMode() == scrollListCategory) {
            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics, Texture.PROVIDER_SCROLL_LIST_TOP_BUTTON_RED, x, y, this.width, this.height);
        } else {
            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics, Texture.BUILD_LOADOUTS_SCROLL_LIST_TOP_BUTTON, x, y, this.width, this.height);
        }

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        this.text,
                        (this.x + this.width / 2f),
                        (this.y + this.height / 2f) + 2,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        this.playDownSound(Minecraft.getInstance().getSoundManager());

        parent.providerInfoScrollBarWidget.setSelectedMode(scrollListCategory);

        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
