/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.network.chat.Component;

public class TextWidget extends WynntilsButton {
    public TextWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        drawString(poseStack, McUtils.mc().font, getMessage(), this.getX(), this.getY(), CommonColors.WHITE.asInt());
    }

    @Override
    public void onPress() {}
}
