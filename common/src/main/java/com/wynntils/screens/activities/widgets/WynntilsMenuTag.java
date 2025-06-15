/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.activities.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class WynntilsMenuTag extends WynntilsButton {
    private final Consumer<Integer> onClick;

    private int offset = 0;

    public WynntilsMenuTag(int x, int y, Consumer<Integer> onClick) {
        super(x, y, Texture.CONTENT_BOOK_TAG.width(), 22, Component.literal("Wynntils Menu Button"));

        this.onClick = onClick;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        if (isHovered) {
            offset = Math.min(2, offset + 1);
        } else {
            offset = Math.max(0, offset - 1);
        }

        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.CONTENT_BOOK_TAG.resource(),
                getX(),
                getY(),
                0,
                Texture.CONTENT_BOOK_TAG.width(),
                height,
                0,
                height * offset,
                Texture.CONTENT_BOOK_TAG.width(),
                height,
                Texture.CONTENT_BOOK_TAG.width(),
                Texture.CONTENT_BOOK_TAG.height());
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.wynntilsMenu.name")),
                        getX() + width / 2f + offset,
                        getY() + height / 2f - 2,
                        isHovered ? CommonColors.YELLOW : CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        this.playDownSound(Minecraft.getInstance().getSoundManager());
        onClick.accept(button);

        return true;
    }

    @Override
    public void onPress() {}
}
