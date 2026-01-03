/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.SectionCoordinates;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class FixCrosshairPositionFeature extends Feature {
    public FixCrosshairPositionFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }

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

        event.getGuiGraphics().nextStratum();

        SectionCoordinates section = Managers.Overlay.getSection(OverlayPosition.AnchorSection.MIDDLE);
        int x = (section.x1() + section.x2() - 15) / 2;
        int y = (section.y1() + section.y2() - 15) / 2;
        event.getGuiGraphics().blitSprite(RenderPipelines.CROSSHAIR, Gui.CROSSHAIR_SPRITE, x, y, 15, 15);
        // Don't need to render the attack indicator, since Wynncraft doesn't ever use it
    }
}
