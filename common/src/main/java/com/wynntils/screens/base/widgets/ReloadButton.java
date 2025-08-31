/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ReloadButton extends WynntilsButton implements TooltipProvider {
    private final List<Component> reloadTooltip;
    private final Runnable onClickRunnable;

    public ReloadButton(int x, int y, int width, int height, String activityType, Runnable onClickRunnable) {
        super(x, y, width, height, Component.literal("Reload Button"));
        this.onClickRunnable = onClickRunnable;

        reloadTooltip = List.of(
                Component.translatable("screens.wynntils.wynntilsActivities.reload.name")
                        .withStyle(ChatFormatting.WHITE),
                Component.translatable("screens.wynntils.wynntilsActivities.reload.description", activityType)
                        .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        Texture reloadButton = Texture.RELOAD_ICON_OFFSET;
        if (this.isHovered) {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    reloadButton.resource(),
                    this.getX(),
                    this.getY(),
                    0,
                    this.width,
                    this.height,
                    reloadButton.width() / 2,
                    0,
                    reloadButton.width() / 2,
                    reloadButton.height(),
                    reloadButton.width(),
                    reloadButton.height());
        } else {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    reloadButton.resource(),
                    this.getX(),
                    this.getY(),
                    0,
                    this.width,
                    this.height,
                    0,
                    0,
                    reloadButton.width() / 2,
                    reloadButton.height(),
                    reloadButton.width(),
                    reloadButton.height());
        }
    }

    @Override
    public void onPress() {
        onClickRunnable.run();
    }

    @Override
    public List<Component> getTooltipLines() {
        return reloadTooltip;
    }
}
