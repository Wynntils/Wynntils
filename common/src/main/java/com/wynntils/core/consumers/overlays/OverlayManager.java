/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.overlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.overlays.annotations.OverlayGroup;
import com.wynntils.core.consumers.overlays.annotations.RegisterOverlay;
import com.wynntils.core.mod.CrashReportManager;
import com.wynntils.core.mod.type.CrashType;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.OverlayGroupHolder;
import com.wynntils.mc.event.DisplayResizeEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.mc.event.TitleScreenInitEvent;
import com.wynntils.screens.overlays.placement.OverlayManagementScreen;
import com.wynntils.screens.overlays.selection.OverlaySelectionScreen;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.type.RenderElementType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.util.profiling.Profiler;
import net.neoforged.bus.api.SubscribeEvent;
import org.apache.commons.lang3.reflect.FieldUtils;

public final class OverlayManager extends Manager {
    private final Map<Feature, List<Overlay>> overlayParentMap = new HashMap<>();
    private final Map<Overlay, OverlayInfoContainer> overlayInfoMap = new HashMap<>();
    private final Map<Feature, List<OverlayGroupHolder>> overlayGroupMap = new HashMap<>();

    private final Set<Overlay> enabledOverlays = new HashSet<>();
    private Map<RenderElementType, List<Overlay>> renderMap = new HashMap<>();

    private final List<SectionCoordinates> sections = new ArrayList<>(9);
    private final Map<Class<?>, Integer> profilingTimes = new HashMap<>();
    private final Map<Class<?>, Integer> profilingCounts = new HashMap<>();

    public OverlayManager(CrashReportManager crashReportManager) {
        super(List.of(crashReportManager));

        addCrashCallbacks();
    }

    // region Initialization and Registration

    private void registerOverlay(
            Overlay overlay, Feature parent, RenderElementType elementType, boolean enabledByDefault) {
        overlayParentMap.putIfAbsent(parent, new LinkedList<>());
        overlayParentMap.get(parent).add(overlay);

        overlay.renderElement.store(elementType);
        overlayInfoMap.put(overlay, new OverlayInfoContainer(parent, enabledByDefault));
    }

    private void unregisterOverlay(Overlay overlay) {
        overlayParentMap.get(overlayInfoMap.get(overlay).parent()).remove(overlay);

        WynntilsMod.unregisterEventListener(overlay);

        overlayInfoMap.remove(overlay);
        enabledOverlays.remove(overlay);
    }

    public void disableOverlays(Feature parent) {
        overlayParentMap.getOrDefault(parent, List.of()).forEach(this::disableOverlay);
    }

    public void disableOverlay(Overlay disabledOverlay) {
        if (!isEnabled(disabledOverlay)) return;

        enabledOverlays.remove(disabledOverlay);
        WynntilsMod.unregisterEventListener(disabledOverlay);

        disabledOverlay.getConfigOptionFromString("userEnabled").ifPresent(disabledOverlay::callOnConfigUpdate);
    }

    public void enableOverlays(Feature parent) {
        overlayParentMap.getOrDefault(parent, List.of()).forEach(this::enableOverlay);
    }

    public void enableOverlay(Overlay enabledOverlay) {
        if (!enabledOverlay.shouldBeEnabled() || isEnabled(enabledOverlay)) return;

        enabledOverlays.add(enabledOverlay);
        WynntilsMod.registerEventListener(enabledOverlay);

        enabledOverlay.getConfigOptionFromString("userEnabled").ifPresent(enabledOverlay::callOnConfigUpdate);

        rebuildRenderOrder();
    }

    public void discoverOverlays(Feature feature) {
        Field[] overlayFields = FieldUtils.getFieldsWithAnnotation(feature.getClass(), RegisterOverlay.class);
        for (Field overlayField : overlayFields) {
            try {
                Object fieldValue = FieldUtils.readField(overlayField, feature, true);

                if (!(fieldValue instanceof Overlay overlay)) {
                    throw new RuntimeException("A non-Overlay class was marked with OverlayInfo annotation.");
                }

                RegisterOverlay annotation = overlayField.getAnnotation(RegisterOverlay.class);
                assert annotation.renderType().isRootRender() : "Overlay render type must use a root renderer";
                assert annotation.renderType() != RenderElementType.GUI_PRE : "Overlay render type cannot be GUI_PRE";
                Managers.Overlay.registerOverlay(overlay, feature, annotation.renderType(), overlay.isUserEnabled());

                assert !overlay.getTranslatedName().startsWith("feature.wynntils.")
                        : "Fix i18n for " + overlay.getTranslatedName();
            } catch (IllegalAccessException e) {
                WynntilsMod.error("Unable to get field " + overlayField, e);
            }
        }
    }

    public void discoverOverlayGroups(Feature feature) {
        List<OverlayGroupHolder> holders = Stream.of(feature.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(OverlayGroup.class))
                .map(field -> {
                    OverlayGroup annotation = field.getAnnotation(OverlayGroup.class);
                    return new OverlayGroupHolder(field, feature, annotation.renderType(), annotation.instances());
                })
                .toList();

        holders.forEach(this::createOverlayGroupWithDefaults);

        overlayGroupMap.put(feature, holders);
    }

    public void rebuildRenderOrder() {
        Map<RenderElementType, List<Overlay>> newRenderMap = new HashMap<>();

        for (RenderElementType elementType : RenderElementType.values()) {
            if (elementType == RenderElementType.GUI_PRE) continue;
            if (!elementType.isRootRender()) continue;

            // Get all the overlays that do not have a valid render order, this will be all overlays
            // on first run, then afterwards only new overlays
            List<Overlay> newOverlays = enabledOverlays.stream()
                    .filter(overlay -> overlay.getRenderElementType() == elementType)
                    .filter(overlay -> overlay.getRenderOrder() == -1)
                    .sorted(Comparator.comparing(Overlay::getShortName))
                    .toList();

            // Then we get the overlays that already have a valid order so that we can
            // add the new overlays to the end of the list and set a render order based on the size
            // of this and the new list
            List<Overlay> orderedOverlays = enabledOverlays.stream()
                    .filter(overlay -> overlay.getRenderElementType() == elementType)
                    .filter(overlay -> overlay.getRenderOrder() != -1)
                    .sorted(Comparator.comparing(overlay -> overlay.getRenderOrder()))
                    .collect(Collectors.toCollection(ArrayList::new));

            int newOrder = orderedOverlays.size();

            // Set and save the render order for each of the new overlays
            for (int i = 0; i < newOverlays.size(); i++) {
                Overlay overlay = newOverlays.get(i);
                overlay.setRenderOrder(newOrder + i);
                orderedOverlays.add(overlay);
            }

            newRenderMap.put(elementType, orderedOverlays);
        }

        renderMap = newRenderMap;
    }

    public void updateOverlayPosition(Overlay overlay, int direction) {
        Map<RenderElementType, List<Overlay>> newRenderMap = new HashMap<>(this.renderMap);

        RenderElementType currentElementType = overlay.getRenderElementType();
        List<Overlay> currentElementOrder = newRenderMap.get(currentElementType);
        int currentIndex = currentElementOrder.indexOf(overlay);

        if (direction == -1) {
            if (currentIndex == 0 && currentElementType != RenderElementType.overlayValues()[0]) {
                int elementIndex = List.of(RenderElementType.overlayValues()).indexOf(currentElementType);

                RenderElementType previousElement = RenderElementType.overlayValues()[elementIndex - 1];

                overlay.setRenderElementType(previousElement);
                overlay.setRenderOrder(newRenderMap.get(previousElement).size());
            } else if (currentIndex > 0) {
                Overlay aboveOverlay = currentElementOrder.get(currentIndex - 1);

                aboveOverlay.setRenderOrder(currentIndex);
                overlay.setRenderOrder(currentIndex - 1);
            }
        } else {
            if (currentIndex == currentElementOrder.size() - 1 && currentElementType != RenderElementType.GUI_POST) {
                int elementIndex = List.of(RenderElementType.overlayValues()).indexOf(currentElementType);

                RenderElementType nextElement = RenderElementType.overlayValues()[elementIndex + 1];
                List<Overlay> nextElementOrder = newRenderMap.get(nextElement);

                overlay.setRenderElementType(nextElement);
                overlay.setRenderOrder(0);

                for (Overlay other : nextElementOrder) {
                    other.setRenderOrder(other.getRenderOrder() + 1);
                }
            } else if (currentIndex < currentElementOrder.size() - 1) {
                Overlay belowOverlay = currentElementOrder.get(currentIndex + 1);

                belowOverlay.setRenderOrder(currentIndex);
                overlay.setRenderOrder(currentIndex + 1);
            }
        }

        renderMap = newRenderMap;

        rebuildRenderOrder();

        // Check that render orders start at 0 and increment by 1, no duplicates or gaps
        // We do this here and not in rebuildRenderOrder since we want to make sure all overlays
        // are present in the list and rebuild is called before all overlays have been initialized
        normalizeRenderOrders();
    }

    private void normalizeRenderOrders() {
        for (RenderElementType elementType : RenderElementType.values()) {
            if (elementType == RenderElementType.GUI_PRE) continue;
            if (!elementType.isRootRender()) continue;

            List<Overlay> currentElementMap = renderMap.get(elementType);

            int expectedRenderOrder = 0;

            for (Overlay overlay : currentElementMap) {
                if (overlay.getRenderOrder() != expectedRenderOrder) {
                    overlay.setRenderOrder(expectedRenderOrder);
                }

                expectedRenderOrder++;
            }
        }
    }

    // endregion

    // region Overlay Groups

    public void createOverlayGroupWithDefaults(OverlayGroupHolder holder) {
        recreateGroupOverlaysWithIds(
                holder,
                IntStream.rangeClosed(1, holder.getDefaultCount()).boxed().toList());
    }

    public void createOverlayGroupWithIds(OverlayGroupHolder holder, List<Integer> ids) {
        recreateGroupOverlaysWithIds(holder, ids);
    }

    public int extendOverlayGroup(OverlayGroupHolder holder) {
        List<Integer> ids = holder.getOverlays().stream()
                .map(overlay -> ((DynamicOverlay) overlay).getId())
                .collect(Collectors.toList());

        int newId = 1;
        while (ids.contains(newId)) {
            newId++;
        }

        ids.add(newId);

        recreateGroupOverlaysWithIds(holder, ids);

        return newId;
    }

    public void removeIdFromOverlayGroup(OverlayGroupHolder holder, int id) {
        List<Integer> ids = holder.getOverlays().stream()
                .map(overlay -> ((DynamicOverlay) overlay).getId())
                .collect(Collectors.toList());

        ids.remove((Integer) id);

        recreateGroupOverlaysWithIds(holder, ids);
    }

    private void recreateGroupOverlaysWithIds(OverlayGroupHolder holder, List<Integer> ids) {
        holder.getOverlays().forEach(this::unregisterOverlay);

        holder.initGroup(ids);

        holder.getOverlays()
                .forEach(overlay -> registerOverlay(overlay, holder.getParent(), holder.getElementType(), true));
    }

    // endregion

    // region Ticking
    @SubscribeEvent
    public void onTick(TickEvent event) {
        enabledOverlays.forEach(overlay -> {
            overlay.tick();
            overlay.updateEnabledCache();
        });
    }

    // endregion

    // region Rendering

    // We need to receive canceled events as some features cancel certain element types if they are replacing it
    @SubscribeEvent(receiveCanceled = true)
    public void onRenderPre(RenderEvent.Pre event) {
        // Overlays can only use root render events
        if (!event.getType().isRootRender()) return;
        // We don't use GUI_PRE for rendering overlays
        if (event.getType() == RenderElementType.GUI_PRE) return;

        Profiler.get().push("preRenOverlay" + event.getType().name());
        renderOverlays(event);
        Profiler.get().pop();
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onRenderPost(RenderEvent.Post event) {
        // Only GUI_POST is used for rendering overlays
        if (event.getType() != RenderElementType.GUI_POST) return;

        Profiler.get().push("postRenOverlay");
        renderOverlays(event);
        Profiler.get().pop();
    }

    private void renderOverlays(RenderEvent event) {
        boolean showPreview = false;
        boolean renderNonSelected = true;
        boolean shouldRender = true;
        Overlay selectedOverlay = null;

        if (McUtils.screen() instanceof OverlayManagementScreen screen) {
            shouldRender = false;
            showPreview = screen.showPreview();
            renderNonSelected = screen.shouldRenderAllOverlays();
            selectedOverlay = screen.getSelectedOverlay();
        } else if (McUtils.screen() instanceof OverlaySelectionScreen screen) {
            if (screen.renderingPreview()) {
                showPreview = true;
                renderNonSelected = screen.shouldShowOverlays();
                selectedOverlay = screen.getSelectedOverlay();
            }
        }

        List<Overlay> crashedOverlays = new LinkedList<>();
        for (Overlay overlay : renderMap.getOrDefault(event.getType(), new ArrayList<>())) {
            try {
                if (showPreview) {
                    if (selectedOverlay != null && overlay != selectedOverlay && !renderNonSelected) continue;

                    overlay.renderPreview(event.getGuiGraphics(), event.getDeltaTracker(), event.getWindow());
                } else if (shouldRender && overlay.isRendered()) {
                    long startTime = System.currentTimeMillis();
                    overlay.renderOrErrorMessage(event.getGuiGraphics(), event.getDeltaTracker(), event.getWindow());
                    logProfilingData(startTime, overlay);
                }
            } catch (Throwable t) {
                RenderUtils.disableScissor(event.getGuiGraphics());

                // We can't disable it right away since that will cause ConcurrentModificationException
                crashedOverlays.add(overlay);

                WynntilsMod.reportCrash(
                        CrashType.OVERLAY,
                        overlay.getTranslatedName(),
                        overlay.getClass().getName(),
                        "render",
                        t);
            }
        }

        // Hopefully we have none :)
        for (Overlay overlay : crashedOverlays) {
            overlay.getConfigOptionFromString("userEnabled")
                    .ifPresent(config -> ((Config<Boolean>) config).setValue(false));
        }
    }

    // endregion

    // region Profiling

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

    // endregion

    // region Sections
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

    // endregion

    private void addCrashCallbacks() {
        Managers.CrashReport.registerCrashContext("Loaded Overlays", () -> {
            StringBuilder result = new StringBuilder();

            for (Overlay overlay : enabledOverlays) {
                result.append("\n\t\t").append(overlay.getTranslatedName());
            }

            return result.toString();
        });
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

    public <T extends Overlay> T getOverlay(Class<T> overlayClass) {
        return (T) overlayInfoMap.keySet().stream()
                .filter(overlayClass::isInstance)
                .findFirst()
                .orElse(null);
    }

    public Feature getOverlayParent(Overlay overlay) {
        return overlayInfoMap.get(overlay).parent();
    }

    public Map<RenderElementType, List<Overlay>> getRenderMap() {
        return Collections.unmodifiableMap(renderMap);
    }

    public boolean isEnabled(Overlay overlay) {
        return enabledOverlays.contains(overlay);
    }

    public boolean isEnabledByDefault(Overlay overlay) {
        return overlayInfoMap.get(overlay).enabledByDefault();
    }

    public List<OverlayGroupHolder> getOverlayGroups() {
        return overlayGroupMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    public List<Overlay> getFeatureOverlays(Feature feature) {
        return overlayParentMap.getOrDefault(feature, List.of());
    }

    public List<OverlayGroupHolder> getFeatureOverlayGroups(Feature feature) {
        return overlayGroupMap.getOrDefault(feature, List.of());
    }

    private record OverlayInfoContainer(Feature parent, boolean enabledByDefault) {}
}
