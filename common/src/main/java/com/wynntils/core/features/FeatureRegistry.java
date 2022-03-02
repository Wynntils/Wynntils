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
import com.wynntils.wc.utils.WynnUtils;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Loads {@link Feature}s */
public class FeatureRegistry {
    private static final List<Feature> features = new LinkedList<>();
    private static final OverlayListener overlayListener = new OverlayListener();

    public static void registerFeature(Feature feature) {
        if (feature instanceof Overlay overlay) {
            overlayListener.register(overlay);
        }

        features.add(feature);
    }

    public static void registerFeatures(List<Feature> features) {
        features.forEach(FeatureRegistry::registerFeature);
    }

    public static List<Feature> getFeatures() {
        return features;
    }

    public static List<Overlay> getOverlays() {
        return overlayListener.overlays;
    }

    public static void init() {
        // debug
        registerFeature(new PacketDebuggerFeature());
        registerFeature(new KeyBindTestFeature());
        registerFeature(new ConnectionProgressFeature());

        registerFeature(new WynncraftButtonFeature());
        registerFeature(new SoulPointTimerFeature());
        registerFeature(new ItemGuessFeature());
        registerFeature(new GammabrightFeature());
        registerFeature(new HealthPotionBlockerFeature());
        registerFeature(new PlayerGhostTransparencyFeature());
        registerFeature(new ItemStatInfoFeature());

        features.forEach(Feature::init);

        WynntilsMod.getEventBus().register(OverlayListener.class);

        addCrashCallbacks();
    }

    private static void addCrashCallbacks() {
        CrashReportManager.registerCrashContext(
                new CrashReportManager.ICrashContext() {
                    @Override
                    public String name() {
                        return "Loaded Features";
                    }

                    @Override
                    public Object generate() {
                        StringBuilder result = new StringBuilder();

                        for (Feature feature : getFeatures()) {
                            if (feature.isEnabled()) {
                                result.append("\n\t\t").append(feature.getClass().getName());
                            }
                        }

                        return result.toString();
                    }
                });

        CrashReportManager.registerCrashContext(
                new CrashReportManager.ICrashContext() {
                    @Override
                    public String name() {
                        return "Loaded Overlays";
                    }

                    @Override
                    public Object generate() {
                        StringBuilder result = new StringBuilder();

                        for (Overlay overlay : getOverlays()) {
                            if (overlay.isEnabled()) {
                                result.append("\n\t\t").append(overlay.getClass().getName());
                            }
                        }

                        return result.toString();
                    }
                });
    }

    private static class OverlayListener {
        private final List<Overlay> overlays = new LinkedList<>();
        private final EnumMap<RenderEvent.ElementType, List<Overlay>> overlaysMap =
                new EnumMap<>(RenderEvent.ElementType.class);

        public void register(Overlay overlay) {
            overlays.add(overlay);

            for (RenderEvent.ElementType type : overlay.hookElements) {
                List<Overlay> overlayList =
                        overlaysMap.computeIfAbsent(type, k -> new ArrayList<>());
                overlayList.add(overlay);
            }
        }

        @SubscribeEvent
        public void onTick(ClientTickEvent e) {
            if (e.getTickPhase() == ClientTickEvent.Phase.END) {
                McUtils.mc().getProfiler().push("tickOverlay");
                for (Overlay overlay : overlays) {
                    if (!overlay.isEnabled()) continue;

                    overlay.tick();
                }
                McUtils.mc().getProfiler().pop();
            }
        }

        @SubscribeEvent
        public void onRenderPre(RenderEvent.Pre e) {
            if (!WynnUtils.onServer()) // || !McUtils.mc().playerController.isSpectator())
            return;

            McUtils.mc().getProfiler().push("preRenOverlay");

            List<Overlay> overlayList = overlaysMap.get(e.getType());

            if (overlayList != null) {
                for (Overlay overlay : overlayList) {
                    if (!overlay.visible) continue;
                    if (!overlay.isEnabled()) continue;

                    McUtils.mc().getProfiler().push(overlay.displayName);
                    overlay.render(e);
                    McUtils.mc().getProfiler().pop();
                }
            }

            McUtils.mc().getProfiler().pop();

            // McIf.mc().getTextureManager().bindTexture(ICONS);
        }

        @SubscribeEvent
        public void onRenderPost(RenderEvent.Post e) {
            if (!WynnUtils.onServer()) // || !McUtils.mc().playerController.isSpectator())
            return;

            McUtils.mc().getProfiler().push("postRenOverlay");

            List<Overlay> overlayList = overlaysMap.get(e.getType());

            if (overlayList != null) {
                for (Overlay overlay : overlayList) {
                    if (!overlay.visible) if (!overlay.isEnabled()) continue;

                    McUtils.mc().getProfiler().push(overlay.displayName);
                    overlay.render(e);
                    McUtils.mc().getProfiler().pop();
                }
            }

            McUtils.mc().getProfiler().pop();
        }
    }
}
