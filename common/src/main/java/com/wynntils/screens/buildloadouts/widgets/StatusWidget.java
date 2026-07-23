/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class StatusWidget extends AbstractWidget {
    private StyledText text;
    private CustomColor color;
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;

    public StatusWidget(int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, 133 - 10, 85, Component.literal("Status Widget"));
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_LIGHT, this.x, this.y, this.width, this.height);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.buildLoadouts.statusWidget.text")),
                        this.x + this.width / 2f,
                        this.y + 10,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        text,
                        this.x + this.width / 2f,
                        this.y + 25,
                        this.width - 10,
                        color,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    private void setStatus(String text, CustomColor color) {
        this.text = StyledText.fromString(text);
        this.color = color;
    }

    public void completed(String text) {
        setStatus(text, parent.COMPLETED_COLOR);
    }

    public void busy(String text) {
        setStatus(text, parent.BUSY_COLOR);
    }

    public void error(String text) {
        setStatus(text, parent.ERROR_COLOR);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
