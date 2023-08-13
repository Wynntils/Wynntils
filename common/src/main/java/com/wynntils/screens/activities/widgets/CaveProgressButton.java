/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.activities.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.network.chat.Component;

public class CaveProgressButton extends WynntilsButton implements TooltipProvider {
    public CaveProgressButton(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Cave info"));
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.CAVE.resource(),
                this.getX(),
                this.getY(),
                this.width,
                this.height,
                Texture.CAVE.width(),
                Texture.CAVE.height());
    }

    @Override
    public void onPress() {}

    @Override
    public List<Component> getTooltipLines() {
        return Models.Cave.getCaveProgress().stream()
                .map(StyledText::getComponent)
                .map(c -> (Component) c)
                .toList();
    }
}
