/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.SectionCoordinates;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class FixCrosshairPositionFeature extends Feature {
    private static boolean shouldOverrideCrosshair() {
        Minecraft mc = McUtils.mc();
        if (!mc.options.getCameraType().isFirstPerson()) return false;
        return !mc.gui.getDebugOverlay().showDebugScreen() // Let vanilla handle the debug crosshair
                || mc.player.isReducedDebugInfo()
                || mc.options.reducedDebugInfo().get();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderCrosshair(RenderEvent.Pre event) {
        if (event.getType() != RenderEvent.ElementType.CROSSHAIR) return;
        if (!shouldOverrideCrosshair()) return;
        event.setCanceled(true);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);

        SectionCoordinates section = Managers.Overlay.getSection(OverlayPosition.AnchorSection.MIDDLE);
        int x = (section.x1() + section.x2() - 15) / 2;
        int y = (section.y1() + section.y2() - 15) / 2;
        event.getGuiGraphics().blitSprite(RenderType::crosshair, Gui.CROSSHAIR_SPRITE, x, y, 15, 15);
        // Don't need to render the attack indicator, since Wynncraft doesn't ever use it

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}
