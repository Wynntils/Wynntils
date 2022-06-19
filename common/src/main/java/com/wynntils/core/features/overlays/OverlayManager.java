/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.wynntils.core.Reference;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.overlays.annotations.RegisteredOverlay;
import com.wynntils.mc.event.RenderEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OverlayManager {
    private static final Set<Class<? extends Overlay>> registeredOverlays = new HashSet<>();
    private static final Map<Class<? extends Overlay>, List<Overlay>> overlayInstanceMap = new HashMap<>();

    public static void searchAndRegisterOverlays(Class<? extends Feature> classToSearch) {
        Set<Class<?>> declaredOverlays = Arrays.stream(classToSearch.getDeclaredClasses())
                .filter(clazz -> clazz.isAnnotationPresent(RegisteredOverlay.class))
                .filter(clazz -> clazz.getSuperclass() == Overlay.class)
                .collect(Collectors.toUnmodifiableSet());

        for (Class<?> declaredOverlay : declaredOverlays) {
            Class<? extends Overlay> overlay = (Class<? extends Overlay>) declaredOverlay;
            registeredOverlays.add(overlay);

            if (overlay.getAnnotation(RegisteredOverlay.class).enabled()) {
                instantiateOverlay(overlay);
            }
        }

        if (!registeredOverlays.isEmpty())
            Reference.LOGGER.info(
                    classToSearch.getName() + " registered " + registeredOverlays.size() + " overlay(s).");
    }

    private static Overlay instantiateOverlay(Class<? extends Overlay> overlay) {
        try {
            Overlay instance = overlay.getConstructor().newInstance();

            overlayInstanceMap.putIfAbsent(overlay, new ArrayList<>());
            overlayInstanceMap.get(overlay).add(instance);

            return instance;
        } catch (InvocationTargetException
                | NoSuchMethodException
                | InstantiationException
                | IllegalAccessException e) {
            Reference.LOGGER.error("Error when instantiating " + overlay.getName());
            e.printStackTrace();
        }
        return null;
    }

    public static Overlay createNewInstance(Class<? extends Overlay> overlay) {
        if (!registeredOverlays.contains(overlay)) {
            throw new RuntimeException(
                    "Tried to create a " + overlay.getName() + " instance without it being a registered type.");
        }

        return instantiateOverlay(overlay);
    }

    public static void addNewInstance(Overlay instance) {
        if (!registeredOverlays.contains(instance.getClass())) {
            throw new RuntimeException("Tried to add a " + instance.getClass().getName()
                    + " instance without it being a registered type.");
        }

        overlayInstanceMap.putIfAbsent(instance.getClass(), new ArrayList<>());
        overlayInstanceMap.get(instance.getClass()).add(instance);
    }

    @SubscribeEvent
    public static void onRenderPre(RenderEvent.Pre event) {
        renderOverlays(event, RegisteredOverlay.RenderState.Pre);
    }

    @SubscribeEvent
    public static void onRenderPost(RenderEvent.Post event) {
        renderOverlays(event, RegisteredOverlay.RenderState.Post);
    }

    private static void renderOverlays(RenderEvent event, RegisteredOverlay.RenderState renderState) {
        for (List<Overlay> overlays : overlayInstanceMap.values()) {
            for (Overlay overlay : overlays) {
                RegisteredOverlay annotation = overlay.getClass().getAnnotation(RegisteredOverlay.class);
                if (!annotation.enabled()
                        || annotation.renderType() != event.getType()
                        || annotation.renderAt() != renderState) {
                    break; // Break because overlays of the same type have the same requirements
                }
                overlay.render(event.getPoseStack(), event.getPartialTicks(), event.getWindow());
            }
        }
    }

    public static void init() {
        WynntilsMod.getEventBus().register(OverlayManager.class);
    }
}
