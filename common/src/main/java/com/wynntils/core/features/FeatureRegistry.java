/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.features.*;
import com.wynntils.features.debug.ConnectionProgressFeature;
import com.wynntils.features.debug.KeyBindTestFeature;
import com.wynntils.features.debug.PacketDebuggerFeature;
import com.wynntils.mc.event.ClientTickEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.utils.CrashReportManager;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.ModelLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/** Loads {@link Feature}s */
public class FeatureRegistry {
    private static final List<Feature> FEATURES = new LinkedList<>();
    private static final List<Overlay> OVERLAYS = new LinkedList<>();

    public static void registerFeature(Feature feature) {
        if (feature instanceof Overlay overlay) {
            OVERLAYS.add(overlay);
        }

        FEATURES.add(feature);
    }

    public static void registerFeatures(List<Feature> features) {
        features.forEach(FeatureRegistry::registerFeature);
    }

    public static List<Feature> getFeatures() {
        return FEATURES;
    }

    public static void init() {
        CrashReportManager.registerCrashContext(
                () -> {
                    List<String> result = new ArrayList<>();

                    for (Feature feature : FEATURES) {
                        if (feature.isEnabled()) {
                            result.add("\t" + feature.getClass().getName());
                        }
                    }

                    if (!result.isEmpty()) result.add(0, "Loaded Features:");

                    return result;
                });
        // debug
        registerFeature(new PacketDebuggerFeature());
        registerFeature(new KeyBindTestFeature());
        registerFeature(new ConnectionProgressFeature());

        registerFeature(new WynncraftButtonFeature());
        registerFeature(new SoulPointTimerFeature());
        registerFeature(new ItemGuessFeature());
        registerFeature(new GammabrightFeature());
        registerFeature(new HealthPotionBlockerFeature());

        WynntilsMod.getEventBus().register(FeatureRegistry.class);
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent e) {
        if (e.getTickPhase() == ClientTickEvent.Phase.END) {

            for (Overlay overlay : OVERLAYS) {
                overlay.tick();
            }
        }
    }

    @SubscribeEvent
    public static void onRender(RenderEvent.Pre e) {
        if (ModelLoader.getWorldState().onServer()) // && !McUtils.mc().playerController.isSpectator())
            return;

        McUtils.mc().getProfiler().push("preRenOverlay");
        for (Overlay overlay : OVERLAYS) {
            if (!overlay.visible) continue;
            //if (!overlay.active) continue;

            if (overlay.hookElements.length != 0) {
                boolean contained = false;
                for (RenderEvent.ElementType type : overlay.hookElements) {
                    if (e.getType() == type) {
                        contained = true;
                        break;
                    }
                }

                if (contained && overlay.visible) {
                    McUtils.mc().getProfiler().push(overlay.displayName);
                    overlay.render(e);
                    McUtils.mc().getProfiler().pop();
                }
            }
        }

        McUtils.mc().getProfiler().pop();

        //McIf.mc().getTextureManager().bindTexture(ICONS);
    }

    @SubscribeEvent
    public static void onRender(RenderEvent.Post e) {
        if (ModelLoader.getWorldState().onServer()) // && !McUtils.mc().playerController.isSpectator())
            return;

        McUtils.mc().getProfiler().push("postRenOverlay");

        for (Overlay overlay : OVERLAYS) {
            if (!overlay.visible)
            //if (!overlay.active) continue;

            if (overlay.hookElements.length != 0) {
                for (RenderEvent.ElementType type : overlay.hookElements) {
                    if (e.getType() == type) {
                        McUtils.mc().getProfiler().push(overlay.displayName);
                        overlay.render(e);
                        McUtils.mc().getProfiler().pop();
                        break;
                    }
                }
            }
        }

        McUtils.mc().getProfiler().pop();

        //McIf.mc().getTextureManager().bindTexture(ICONS);
    }
}
