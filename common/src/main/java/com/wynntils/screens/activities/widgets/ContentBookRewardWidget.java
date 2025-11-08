/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.activities.widgets;

import com.google.common.collect.Lists;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ContentBookRewardWidget extends AbstractWidget {
    private final List<MutableComponent> tooltip;
    private final StyledText text;
    private final Texture texture;

    public ContentBookRewardWidget(
            int x, int y, int width, Texture texture, List<MutableComponent> tooltip, StyledText text) {
        super(x, y, width, 10, Component.literal("Content Book Reward Widget"));

        this.texture = texture;
        this.tooltip = tooltip;
        this.text = text;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(guiGraphics.pose(), texture, getX() + width - 10, getY() - 1);

        if (text != null) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics.pose(),
                            text,
                            getX(),
                            getY(),
                            CommonColors.GREEN,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.NORMAL,
                            0.9f);
        }

        if (isHovered) {
            McUtils.screen().setTooltipForNextRenderPass(Lists.transform(tooltip, Component::getVisualOrderText));
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
