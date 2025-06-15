/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ExportButton extends WynntilsButton implements TooltipProvider {
    private static final List<Component> ADD_TOOLTIP = List.of(
            Component.translatable("screens.wynntils.wynntilsGuides.export.name")
                    .withStyle(ChatFormatting.WHITE),
            Component.translatable("screens.wynntils.wynntilsGuides.export.description")
                    .withStyle(ChatFormatting.GRAY));

    private final Runnable onClickRunnable;

    public ExportButton(int x, int y, int width, int height, Runnable onClickRunnable) {
        super(x, y, width, height, Component.literal("Export Button"));
        this.onClickRunnable = onClickRunnable;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        Texture shareButton = Texture.SHARE_ICON_OFFSET;
        RenderUtils.drawTexturedRect(
                poseStack,
                shareButton.resource(),
                this.getX(),
                this.getY(),
                0,
                this.width,
                this.height,
                0,
                isHovered ? shareButton.height() / 2 : 0,
                shareButton.width(),
                shareButton.height() / 2,
                shareButton.width(),
                shareButton.height());
    }

    @Override
    public void onPress() {
        onClickRunnable.run();
    }

    @Override
    public List<Component> getTooltipLines() {
        return ADD_TOOLTIP;
    }
}
