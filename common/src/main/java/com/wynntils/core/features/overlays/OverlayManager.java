/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.utils.McUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OverlayManager {
    private static final Map<Overlay, OverlayInfo> overlayInfoMap = new HashMap<>();

    private static final Set<Overlay> enabledOverlays = new HashSet<>();

    public static void registerOverlay(Overlay overlay, OverlayInfo overlayInfo) {
        overlayInfoMap.put(overlay, overlayInfo);

        if (overlayInfo.enabled()) {
            enabledOverlays.add(overlay);
        }
    }

    public static void disableAllOverlaysForFeature(List<Overlay> overlays) {
        enabledOverlays.removeIf(overlays::contains);
    }

    public static void enableAllOverlaysForFeature(List<Overlay> overlays) {
        enabledOverlays.addAll(overlays);
    }

    @SubscribeEvent
    public static void onRenderPre(RenderEvent.Pre event) {
        McUtils.mc().getProfiler().push("preRenOverlay");
        renderOverlays(event, OverlayInfo.RenderState.Pre);
        McUtils.mc().getProfiler().pop();
    }

    @SubscribeEvent
    public static void onRenderPost(RenderEvent.Post event) {
        McUtils.mc().getProfiler().push("postRenOverlay");
        renderOverlays(event, OverlayInfo.RenderState.Post);
        McUtils.mc().getProfiler().pop();
    }

    private static void renderOverlays(RenderEvent event, OverlayInfo.RenderState renderState) {
        for (Overlay overlay : enabledOverlays) {
            OverlayInfo annotation = overlayInfoMap.get(overlay);

            if (renderState != annotation.renderAt() || event.getType() != annotation.renderType()) continue;

            overlay.render(overlay.getPosition(), event.getPoseStack(), event.getPartialTicks(), event.getWindow());
        }
    }

    public static void init() {
        WynntilsMod.getEventBus().register(OverlayManager.class);
    }
}
