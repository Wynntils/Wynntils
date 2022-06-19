/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.wynntils.core.Reference;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.overlays.annotations.RegisteredOverlay;
import com.wynntils.mc.event.RenderEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// TODO: This class does not support instance based overlays at the moment. In the future, overlays should be
// instantiated and then rendered, allowing more than one overlay of the same type
public class OverlayManager {
    private final Set<Class<? extends Overlay>> registeredOverlays = new HashSet<>();
    private final Map<Integer, Overlay> overlayInstanceMap = new HashMap<>();

    public void searchAndRegisterOverlays(Class<? extends Feature> classToSearch) {
        Set<Class<?>> declaredOverlays = Arrays.stream(classToSearch.getDeclaredClasses())
                .filter(clazz -> clazz.isAnnotationPresent(RegisteredOverlay.class))
                .filter(clazz -> clazz.getSuperclass() == Overlay.class)
                .collect(Collectors.toUnmodifiableSet());

        for (Class<?> declaredOverlay : declaredOverlays) {
            Class<? extends Overlay> overlay = (Class<? extends Overlay>) declaredOverlay;
            registeredOverlays.add(overlay);

            if (overlay.getAnnotation(RegisteredOverlay.class).enabled()) {
                try {
                    overlayInstanceMap.put(
                            overlay.hashCode(), overlay.getConstructor().newInstance());
                } catch (InvocationTargetException
                        | NoSuchMethodException
                        | InstantiationException
                        | IllegalAccessException e) {
                    Reference.LOGGER.error("Error when instantiating " + overlay.getName());
                    e.printStackTrace();
                }
            }
        }

        if (!registeredOverlays.isEmpty())
            Reference.LOGGER.info(
                    classToSearch.getName() + " registered " + registeredOverlays.size() + " overlay(s).");
    }

    @SubscribeEvent
    public void onRenderPre(RenderEvent.Pre event) {
        for (Overlay overlay : overlayInstanceMap.values()) {
            RegisteredOverlay annotation = overlay.getClass().getAnnotation(RegisteredOverlay.class);
            if (annotation.enabled()
                    && annotation.renderType() == event.getType()
                    && annotation.renderAt() == RegisteredOverlay.RenderState.Pre) {
                overlay.render(event.getPoseStack(), event.getPartialTicks(), event.getWindow());
            }
        }
    }

    @SubscribeEvent
    public void onRenderPost(RenderEvent.Post event) {
        for (Overlay overlay : overlayInstanceMap.values()) {
            RegisteredOverlay annotation = overlay.getClass().getAnnotation(RegisteredOverlay.class);
            if (annotation.enabled()
                    && annotation.renderType() == event.getType()
                    && annotation.renderAt() == RegisteredOverlay.RenderState.Post) {
                overlay.render(event.getPoseStack(), event.getPartialTicks(), event.getWindow());
            }
        }
    }
}
