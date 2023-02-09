/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.mod.CrashReportManager;
import com.wynntils.mc.event.DisplayResizeEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.TitleScreenInitEvent;
import com.wynntils.screens.overlays.placement.OverlayManagementScreen;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class OverlayManager extends Manager {
    private final MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(new BufferBuilder(256));

    private final Map<Overlay, OverlayInfo> overlayInfoMap = new HashMap<>();
    private final Map<Overlay, Feature> overlayParent = new HashMap<>();

    private final Set<Overlay> enabledOverlays = new HashSet<>();

    private final List<SectionCoordinates> sections = new ArrayList<>(9);
    private Map<Class<?>, Integer> profilingTimes = new HashMap<>();
    private Map<Class<?>, Integer> profilingCounts = new HashMap<>();

    public OverlayManager(CrashReportManager crashReportManager) {
        super(List.of(crashReportManager));
        addCrashCallbacks();
    }

    public void registerOverlay(Overlay overlay, OverlayInfo overlayInfo, Feature parent) {
        overlayInfoMap.put(overlay, overlayInfo);
        overlayParent.put(overlay, parent);
    }

    public void disableOverlays(List<Overlay> overlays) {
        enabledOverlays.removeIf(overlays::contains);
        overlays.forEach(
                overlay -> overlay.getConfigOptionFromString("userEnabled").ifPresent(overlay::onConfigUpdate));
    }

    public void enableOverlays(List<Overlay> overlays, boolean ignoreState) {
        List<Overlay> enabledOverlays = ignoreState
                ? overlays
                : overlays.stream().filter(Overlay::isEnabled).toList();
        this.enabledOverlays.addAll(enabledOverlays);
        enabledOverlays.forEach(
                overlay -> overlay.getConfigOptionFromString("userEnabled").ifPresent(overlay::onConfigUpdate));
    }

    @SubscribeEvent
    public void onRenderPre(RenderEvent.Pre event) {
        McUtils.mc().getProfiler().push("preRenOverlay");
        renderOverlays(event, OverlayInfo.RenderState.Pre);
        McUtils.mc().getProfiler().pop();
    }

    @SubscribeEvent
    public void onRenderPost(RenderEvent.Post event) {
        McUtils.mc().getProfiler().push("postRenOverlay");
        renderOverlays(event, OverlayInfo.RenderState.Post);
        McUtils.mc().getProfiler().pop();
    }

    private void renderOverlays(RenderEvent event, OverlayInfo.RenderState renderState) {
        boolean testMode = false;
        boolean shouldRender = true;

        if (McUtils.mc().screen instanceof OverlayManagementScreen screen) {
            testMode = screen.isTestMode();
            shouldRender = false;
        }

        List<Overlay> crashedOverlays = new LinkedList<>();
        for (Overlay overlay : enabledOverlays) {
            OverlayInfo annotation = overlayInfoMap.get(overlay);

            if (annotation.renderType() != event.getType()) {
                continue;
            }

            if (annotation.renderAt() == OverlayInfo.RenderState.Replace) {
                if (renderState != OverlayInfo.RenderState.Pre) {
                    continue;
                }
                event.setCanceled(true);
            } else {
                if (annotation.renderAt() != renderState) {
                    continue;
                }
            }

            try {
                if (testMode) {
                    overlay.renderPreview(
                            event.getPoseStack(), bufferSource, event.getPartialTicks(), event.getWindow());
                } else {
                    if (shouldRender) {
                        long startTime = System.currentTimeMillis();
                        overlay.render(event.getPoseStack(), bufferSource, event.getPartialTicks(), event.getWindow());
                        logProfilingData(startTime, overlay);
                    }
                }
            } catch (Throwable t) {
                WynntilsMod.error("Exception when rendering overlay " + overlay.getTranslatedName(), t);
                WynntilsMod.warn("This overlay will be disabled");
                McUtils.sendMessageToClient(Component.literal("Wynntils error: Overlay '" + overlay.getTranslatedName()
                                + "' has crashed and will be disabled")
                        .withStyle(ChatFormatting.RED));
                // We can't disable it right away since that will cause ConcurrentModificationException
                crashedOverlays.add(overlay);
            }
        }

        bufferSource.endBatch();

        // Hopefully we have none :)
        for (Overlay overlay : crashedOverlays) {
            overlay.getConfigOptionFromString("userEnabled").ifPresent(c -> c.setValue(Boolean.FALSE));
        }
    }

    private void logProfilingData(long startTime, Overlay overlay) {
        long endTime = System.currentTimeMillis();
        int timeSpent = (int) (endTime - startTime);
        int allTime = profilingTimes.getOrDefault(overlay.getClass(), 0);
        profilingTimes.put(overlay.getClass(), allTime + timeSpent);

        int allCount = profilingCounts.getOrDefault(overlay.getClass(), 0);
        profilingCounts.put(overlay.getClass(), allCount + 1);
    }

    public Map<Class<?>, Integer> getProfilingTimes() {
        return profilingTimes;
    }

    public Map<Class<?>, Integer> getProfilingCounts() {
        return profilingCounts;
    }

    public void resetProfiling() {
        profilingTimes.clear();
        profilingCounts.clear();
    }

    private void addCrashCallbacks() {
        Managers.CrashReport.registerCrashContext("Loaded Overlays", () -> {
            StringBuilder result = new StringBuilder();

            for (Overlay overlay : enabledOverlays) {
                result.append("\n\t\t").append(overlay.getTranslatedName());
            }

            return result.toString();
        });
    }

    @SubscribeEvent
    public void onResizeEvent(DisplayResizeEvent event) {
        calculateSections();
    }

    // Calculate the sections when loading is finished (this acts as a "game loaded" event)
    @SubscribeEvent
    public void gameInitEvent(TitleScreenInitEvent.Post event) {
        calculateSections();
    }

    private void calculateSections() {
        Window window = McUtils.window();
        int width = window.getGuiScaledWidth();
        int height = window.getGuiScaledHeight();

        int wT = width / 3;
        int hT = height / 3;

        sections.clear();
        for (int h = 0; h < 3; h++) {
            for (int w = 0; w < 3; w++) {
                sections.add(new SectionCoordinates(w * wT, h * hT, (w + 1) * wT, (h + 1) * hT));
            }
        }
    }

    public SectionCoordinates getSection(OverlayPosition.AnchorSection section) {
        return sections.get(section.getIndex());
    }

    public List<SectionCoordinates> getSections() {
        return sections;
    }

    public Set<Overlay> getOverlays() {
        return overlayInfoMap.keySet();
    }

    public OverlayInfo getOverlayInfo(Overlay overlay) {
        return overlayInfoMap.getOrDefault(overlay, null);
    }

    public Feature getOverlayParent(Overlay overlay) {
        return overlayParent.get(overlay);
    }

    public boolean isEnabled(Overlay overlay) {
        return enabledOverlays.contains(overlay);
    }
}
