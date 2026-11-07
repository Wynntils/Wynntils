/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.soundtriggers.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.BasicHoverableButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class TriggerSideButton extends BasicHoverableButton {
    private final StyledText message;

    public TriggerSideButton(
            int x,
            int y,
            int width,
            int height,
            Texture texture,
            Consumer<Integer> onClick,
            Component tooltip,
            Component message) {
        super(x, y, width, height, texture, onClick, List.of(tooltip));
        this.message = StyledText.fromComponent(message);
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderContents(guiGraphics, mouseX, mouseY, partialTick);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        message,
                        getX() + 2,
                        getX() + getWidth() - 4,
                        getY() + 10,
                        getY() + getHeight() - 10,
                        getWidth() - 4,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
    }
}
