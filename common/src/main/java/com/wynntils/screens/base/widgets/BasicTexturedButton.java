/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.TooltipUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

public class BasicTexturedButton extends WynntilsButton {
    private final Texture texture;

    private final Consumer<Integer> onClick;
    private List<Component> tooltip;

    public BasicTexturedButton(
            int x, int y, int width, int height, Texture texture, Consumer<Integer> onClick, List<Component> tooltip) {
        super(x, y, width, height, Component.literal("Basic Button"));
        this.texture = texture;
        this.onClick = onClick;
        this.setTooltip(tooltip);
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(poseStack, texture, this.getX(), this.getY());

        if (this.isHovered) {
            RenderUtils.drawTooltipAt(
                    poseStack,
                    mouseX,
                    mouseY - TooltipUtils.getToolTipHeight(TooltipUtils.componentToClientTooltipComponent(tooltip)),
                    0,
                    tooltip,
                    FontRenderer.getInstance().getFont(),
                    true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        onClick.accept(button);

        return true;
    }

    @Override
    public void onPress() {}

    public void setTooltip(List<Component> newTooltip) {
        tooltip = ComponentUtils.wrapTooltips(newTooltip, 250);
    }
}
