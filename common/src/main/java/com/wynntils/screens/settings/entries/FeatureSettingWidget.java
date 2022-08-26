/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.entries;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.render.RenderUtils;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;

public final class FeatureSettingWidget extends AbstractWidget {
    public FeatureSettingWidget(int x, int y, int width, int height) {
        super(x, y, width, height, new TextComponent("Feature Setting Widget"));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        //        RenderUtils.drawRectBorders(poseStack, CommonColors.WHITE, this.x, this.y, this.x + this.width, this.y
        // + this.height, 0, 2);
        RenderUtils.drawRoundedRectWithBorder(
                poseStack,
                CommonColors.WHITE,
                CommonColors.BLACK,
                this.x,
                this.y,
                0,
                this.width,
                this.height,
                2,
                8,
                10);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
