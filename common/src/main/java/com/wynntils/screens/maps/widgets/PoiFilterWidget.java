/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.maps.PoiManagementScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class PoiFilterWidget extends AbstractWidget {
    private final boolean included;
    private final float iconRenderX;
    private final float iconRenderY;
    private final List<Component> tooltip;
    private final PoiManagementScreen managementScreen;
    private final Texture icon;

    public PoiFilterWidget(
            int x, int y, int width, int height, Texture icon, PoiManagementScreen managementScreen, boolean included) {
        super(x, y, width, height, Component.literal("Poi Icon Widget"));
        this.icon = icon;
        this.managementScreen = managementScreen;
        this.included = included;

        iconRenderX = (x + width / 2f) - icon.width() / 2f;
        iconRenderY = (y + height / 2f) - icon.height() / 2f;

        tooltip = included
                ? List.of(Component.translatable("screens.wynntils.poiManagementGui.filterExclude.tooltip"))
                : List.of(Component.translatable("screens.wynntils.poiManagementGui.filterInclude.tooltip"));
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(
                poseStack, CommonColors.BLACK.withAlpha(isHovered ? 0.7f : 0.5f), getX(), getY(), 0, width, height);

        RenderUtils.drawTexturedRect(poseStack, icon, iconRenderX, iconRenderY);

        if (isHovered) {
            McUtils.mc().screen.setTooltipForNextRenderPass(Lists.transform(tooltip, Component::getVisualOrderText));
        }

        if (included) {
            RenderUtils.drawRect(poseStack, CommonColors.RED.withAlpha(35), getX(), getY(), 1, width, height);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        managementScreen.toggleIcon(icon);

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
