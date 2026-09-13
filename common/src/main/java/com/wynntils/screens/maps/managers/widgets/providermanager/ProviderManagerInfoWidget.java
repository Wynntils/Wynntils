/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.maps.managers.ProviderManagementScreen;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProviderManagerInfoWidget extends AbstractWidget implements TooltipProvider {
    private final ProviderManagementScreen parent;
    private List<Component> generatedTooltip = new ArrayList<>();

    public ProviderManagerInfoWidget(int x, int y, int width, int height, ProviderManagementScreen parent) {
        super(x, y, width, height, Component.literal("Info Widget"));
        this.parent = parent;
        generateTooltip();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.MANAGER_WIDGET_BACKGROUND_RED, getX(), getY(), this.width, this.height);

        RenderUtils.drawTexturedRect(
                guiGraphics,
                Texture.MANAGER_QUESTION_MARK_ICON,
                getX() + (this.width - Texture.MANAGER_QUESTION_MARK_ICON.width()) / 2f,
                getY() + (this.width - Texture.MANAGER_QUESTION_MARK_ICON.height()) / 2f);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public List<Component> getTooltipLines() {
        return Collections.unmodifiableList(this.generatedTooltip);
    }

    public void generateTooltip() {
        this.generatedTooltip = new ArrayList<>();

        this.generatedTooltip.add(Component.literal("nothing for now").withStyle(ChatFormatting.GOLD));
    }
}
