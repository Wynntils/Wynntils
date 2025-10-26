/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.changelog.widgets;

import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.RenderDirection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class ExitFlagButton extends WynntilsButton {
    private final Runnable onClickRunnable;

    public ExitFlagButton(int x, int y, Runnable onClickRunnable) {
        super(x, y, Texture.EXIT_FLAG.width(), Texture.EXIT_FLAG.height() / 2, Component.literal("Exit Button"));
        this.onClickRunnable = onClickRunnable;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawHoverableTexturedRect(
                guiGraphics, Texture.EXIT_FLAG, this.getX(), this.getY(), isHovered, RenderDirection.VERTICAL);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        onClickRunnable.run();
    }
}
