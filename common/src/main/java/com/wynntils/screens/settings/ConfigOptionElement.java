/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.RenderUtils;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public class ConfigOptionElement {
    private final ConfigHolder configHolder;

    public ConfigOptionElement(ConfigHolder configHolder) {
        this.configHolder = configHolder;
    }

    public void render(
            PoseStack poseStack,
            float x,
            float y,
            float width,
            float height,
            int mouseX,
            int mouseY,
            float partialTick) {
        poseStack.pushPose();

        poseStack.translate(x, y, 0);

        RenderUtils.drawRect(poseStack, CommonColors.BLACK, 0, 0, 0, width, height);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        configHolder.getDisplayName(),
                        0,
                        0,
                        0,
                        CommonColors.WHITE,
                        FontRenderer.TextAlignment.LEFT_ALIGNED,
                        FontRenderer.TextShadow.OUTLINE);

        poseStack.popPose();
    }

    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
