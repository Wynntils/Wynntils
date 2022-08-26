/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.entries;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.RenderUtils;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;

public final class FeatureSettingWidget extends AbstractWidget {
    private static final CustomColor BORDER_COLOR = new CustomColor(86, 75, 61, 255);
    private static final CustomColor FOREGROUND_COLOR = new CustomColor(177, 152, 120, 255);

    public FeatureSettingWidget(int x, int y, int width, int height) {
        super(x, y, width, height, new TextComponent("Feature Setting Widget"));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRoundedRectWithBorder(
                poseStack,
                BORDER_COLOR,
                FOREGROUND_COLOR,
                this.x + 2,
                this.y + 2,
                0,
                this.width - 4,
                this.height - 4,
                2,
                6,
                8);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
