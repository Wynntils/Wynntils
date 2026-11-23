/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets;

import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class StyledInfoButton extends AbstractWidget implements TooltipProvider {
    private static final int TOOLTIP_WIDTH = 210;

    private List<Component> tooltipLines;

    public StyledInfoButton(int x, int y, Component label, List<Component> tooltipLines) {
        super(x, y, 20, 20, label);
        setTooltipLines(tooltipLines);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                graphics, Texture.MANAGER_WIDGET_BACKGROUND_RED, getX(), getY(), getWidth(), getHeight());

        Texture icon = Texture.MANAGER_QUESTION_MARK_ICON;
        RenderUtils.drawTexturedRect(
                graphics, icon, getX() + (getWidth() - icon.width()) / 2f, getY() + (getHeight() - icon.height()) / 2f);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        defaultButtonNarrationText(narration);
    }

    @Override
    public List<Component> getTooltipLines() {
        return tooltipLines;
    }

    public void setTooltipLines(List<Component> tooltipLines) {
        this.tooltipLines = ComponentUtils.wrapTooltips(tooltipLines, TOOLTIP_WIDTH);
    }
}
