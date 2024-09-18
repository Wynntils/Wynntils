/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class HeldItemCooldownOverlay extends Overlay {
    public HeldItemCooldownOverlay() {
        super(
                new OverlayPosition(
                        0,
                        0,
                        VerticalAlignment.MIDDLE,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.MIDDLE),
                new OverlaySize(80, 14));
        this.userEnabled.store(false);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        Player player = McUtils.player();
        float cooldownPercent = player.getCooldowns()
                .getCooldownPercent(
                        player.getItemInHand(InteractionHand.MAIN_HAND).getItem(),
                        deltaTracker.getGameTimeDeltaPartialTick(true));
        if (cooldownPercent <= 0f) return;
        renderOverlay(poseStack, bufferSource, cooldownPercent);
    }

    @Override
    public void renderPreview(
            PoseStack poseStack, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        renderOverlay(poseStack, bufferSource, 1F);
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, float cooldownPercent) {
        float width = getWidth();
        float x1 = getRenderX();
        float x2 = x1 + width;
        float height = getHeight();
        float y = getRenderY();
        CustomColor color = CommonColors.WHITE;

        // Draw guidelines
        float ym = y + height / 2f;
        BufferedRenderUtils.drawLine(poseStack, bufferSource, color, x1, ym, x1 + width * 0.4f, ym, 0, 1f);
        BufferedRenderUtils.drawLine(poseStack, bufferSource, color, x2 - width * 0.4f, ym, x2, ym, 0, 1f);

        // Draw bars
        float barWidth = 3f;
        float offset = (1f - cooldownPercent) * (width - barWidth) / 2f;
        BufferedRenderUtils.drawRect(poseStack, bufferSource, color, x1 + offset, y, 0, barWidth, height);
        BufferedRenderUtils.drawRect(poseStack, bufferSource, color, x2 - barWidth - offset, y, 0, barWidth, height);
    }
}
