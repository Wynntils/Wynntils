/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
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
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Window window) {
        Player player = McUtils.player();
        float cooldownPercent = player.getCooldowns()
                .getCooldownPercent(
                        player.getItemInHand(InteractionHand.MAIN_HAND),
                        deltaTracker.getGameTimeDeltaPartialTick(true));
        if (cooldownPercent <= 0f) return;
        renderOverlay(guiGraphics, cooldownPercent);
    }

    @Override
    public void renderPreview(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Window window) {
        renderOverlay(guiGraphics, 1F);
    }

    private void renderOverlay(GuiGraphics guiGraphics, float cooldownPercent) {
        float width = getWidth();
        float x1 = getRenderX();
        float x2 = x1 + width;
        float height = getHeight();
        float y = getRenderY();
        CustomColor color = CommonColors.WHITE;

        // Draw guidelines
        float ym = y + height / 2f;
        RenderUtils.drawLine(guiGraphics, color, x1, ym, x1 + width * 0.4f, ym, 1f);
        RenderUtils.drawLine(guiGraphics, color, x2 - width * 0.4f, ym, x2, ym, 1f);

        // Draw bars
        float barWidth = 3f;
        float offset = (1f - cooldownPercent) * (width - barWidth) / 2f;
        RenderUtils.drawRect(guiGraphics, color, x1 + offset, y, barWidth, height);
        RenderUtils.drawRect(guiGraphics, color, x2 - barWidth - offset, y, barWidth, height);
    }
}
