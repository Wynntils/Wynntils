/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.managers.Manager;
import com.wynntils.core.managers.CrashReportManager;
import com.wynntils.gui.screens.overlays.OverlayManagementScreen;
import com.wynntils.mc.event.DisplayResizeEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.TitleScreenInitEvent;
import com.wynntils.mc.utils.McUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class OverlayManager extends Manager {
    private final Map<Overlay, OverlayInfo> overlayInfoMap = new HashMap<>();
    private final Map<Overlay, Feature> overlayParent = new HashMap<>();

    private final Set<Overlay> enabledOverlays = new HashSet<>();

    private final List<SectionCoordinates> sections = new ArrayList<>(9);

    public OverlayManager() {
        super(List.of());
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
        if (!ignoreState) {
            overlays = overlays.stream().filter(Overlay::isEnabled).toList();
        }

        enabledOverlays.addAll(overlays);
        overlays.forEach(
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
                    overlay.renderPreview(event.getPoseStack(), event.getPartialTicks(), event.getWindow());
                } else {
                    if (shouldRender) {
                        overlay.render(event.getPoseStack(), event.getPartialTicks(), event.getWindow());
                    }
                }
            } catch (Throwable t) {
                WynntilsMod.error("Exception when rendering overlay " + overlay.getTranslatedName(), t);
                WynntilsMod.warn("This overlay will be disabled");
                McUtils.sendMessageToClient(new TextComponent("Wynntils error: Overlay '" + overlay.getTranslatedName()
                                + "' has crashed and will be disabled")
                        .withStyle(ChatFormatting.RED));
                // We can't disable it right away since that will cause ConcurrentModificationException
                crashedOverlays.add(overlay);
            }
        }

        // Hopefully we have none :)
        for (Overlay overlay : crashedOverlays) {
            overlay.getConfigOptionFromString("userEnabled").ifPresent(c -> c.setValue(Boolean.FALSE));
        }
    }

    private void addCrashCallbacks() {
        CrashReportManager.registerCrashContext(new CrashReportManager.ICrashContext("Loaded Overlays") {

            @Override
            public Object generate() {
                StringBuilder result = new StringBuilder();

                for (Overlay overlay : enabledOverlays) {
                    result.append("\n\t\t").append(overlay.getTranslatedName());
                }

                return result.toString();
            }
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
