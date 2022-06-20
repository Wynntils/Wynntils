/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.wynntils.core.Reference;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.overlays.annotations.Overlay;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.objects.Pair;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OverlayManager {
    private static final Map<OverlayBase, OverlayPosition> overlayInstanceMap = new HashMap<>();

    private static final Map<Class<? extends Feature>, List<Pair<OverlayBase, OverlayPosition>>> overlaysFeatureMap =
            new HashMap<>();

    public static void registerOverlay(Feature registrar, OverlayBase overlay, OverlayPosition position) {
        overlaysFeatureMap.putIfAbsent(registrar.getClass(), new ArrayList<>());

        Pair<OverlayBase, OverlayPosition> overlayInfo = new Pair<>(overlay, position);
        overlaysFeatureMap.get(registrar.getClass()).add(overlayInfo);

        if (overlay.getClass().getAnnotation(Overlay.class).enabled()) {
            instantiateOverlay(overlayInfo);
        }
    }

    private static void instantiateOverlay(Pair<OverlayBase, OverlayPosition> overlayInfo) {
        try {
            OverlayBase instance = overlayInfo.a.getClass().getConstructor().newInstance();

            overlayInstanceMap.put(instance, overlayInfo.b);

        } catch (InvocationTargetException
                | NoSuchMethodException
                | InstantiationException
                | IllegalAccessException e) {
            Reference.LOGGER.error(
                    "Error when instantiating " + overlayInfo.a.getClass().getName());
            e.printStackTrace();
        }
    }

    public static void disableOverlayForFeature(Feature feature) {
        for (Pair<OverlayBase, OverlayPosition> overlayPositionPair : overlaysFeatureMap.get(feature.getClass())) {
            overlayInstanceMap.remove(overlayPositionPair.a);
        }
    }

    public static void enableOverlayForFeature(Feature feature) {
        for (Pair<OverlayBase, OverlayPosition> overlayPositionPair : overlaysFeatureMap.get(feature.getClass())) {
            // Prevent duplication from default enabled Overlays
            if (overlayInstanceMap.keySet().stream()
                    .anyMatch(overlayBase -> overlayPositionPair.a.getClass().isInstance(overlayBase))) continue;
            instantiateOverlay(overlayPositionPair);
        }
    }

    @SubscribeEvent
    public static void onRenderPre(RenderEvent.Pre event) {
        renderOverlays(event, Overlay.RenderState.Pre);
    }

    @SubscribeEvent
    public static void onRenderPost(RenderEvent.Post event) {
        renderOverlays(event, Overlay.RenderState.Post);
    }

    private static void renderOverlays(RenderEvent event, Overlay.RenderState renderState) {
        for (Map.Entry<OverlayBase, OverlayPosition> overlay : overlayInstanceMap.entrySet()) {
            Overlay annotation = overlay.getKey().getClass().getAnnotation(Overlay.class);
            overlay.getKey()
                    .render(overlay.getValue(), event.getPoseStack(), event.getPartialTicks(), event.getWindow());
        }
    }

    public static void init() {
        WynntilsMod.getEventBus().register(OverlayManager.class);
    }
}
