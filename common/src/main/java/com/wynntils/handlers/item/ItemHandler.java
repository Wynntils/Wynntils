/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.item;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemHandler extends Handler {
    private final List<ItemAnnotator> annotators = new ArrayList<>();
    private Map<Class<? extends ItemAnnotation>, Integer> profilingTimes = new HashMap<>();
    private Map<Class<? extends ItemAnnotation>, Integer> profilingCounts = new HashMap<>();

    public static Optional<ItemAnnotation> getItemStackAnnotation(ItemStack item) {
        ItemAnnotation annotation = ((AnnotatedItemStack) item).getAnnotation();
        return Optional.ofNullable(annotation);
    }

    public void registerAnnotator(ItemAnnotator annotator) {
        annotators.add(annotator);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSetSlot(SetSlotEvent.Pre event) {
        annotate(event.getItem());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onContainerSetContent(ContainerSetContentEvent.Pre event) {
        event.getItems().forEach(this::annotate);
    }

    private void annotate(ItemStack item) {
        ItemAnnotation annotation = ((AnnotatedItemStack) item).getAnnotation();
        // Don't redo if we already have an annotation
        if (annotation != null) return;

        long startTime = System.currentTimeMillis();
        List<ItemAnnotator> crashedAnnotators = new LinkedList<>();
        String name = WynnUtils.normalizeBadString(ComponentUtils.getCoded(item.getHoverName()));

        for (ItemAnnotator annotator : annotators) {
            try {
                annotation = annotator.getAnnotation(item, name);
                if (annotation != null) {
                    break;
                }
            } catch (Throwable t) {
                String annotatorName = annotator.getClass().getSimpleName();
                WynntilsMod.error("Exception when processing item annotator " + annotatorName, t);
                WynntilsMod.warn("This annotator will be disabled");
                WynntilsMod.warn("Problematic item:" + item);
                WynntilsMod.warn("Problematic item name:" + ComponentUtils.getCoded(item.getHoverName()));
                WynntilsMod.warn("Problematic item tags:" + item.getTag());
                McUtils.sendMessageToClient(Component.literal("Wynntils error: Item Annotator '" + annotatorName
                                + "' has crashed and will be disabled. Not all items will be properly parsed.")
                        .withStyle(ChatFormatting.RED));
                // We can't disable it right away since that will cause ConcurrentModificationException
                crashedAnnotators.add(annotator);
            }
        }

        // Hopefully we have none :)
        for (ItemAnnotator annotator : crashedAnnotators) {
            annotators.remove(annotator);
        }

        if (annotation == null) return;

        // Measure performance
        logProfilingData(startTime, annotation);

        ((AnnotatedItemStack) item).setAnnotation(annotation);
    }

    private void logProfilingData(long startTime, ItemAnnotation annotation) {
        long endTime = System.currentTimeMillis();
        int timeSpent = (int) (endTime - startTime);
        Integer allTime = profilingTimes.getOrDefault(annotation.getClass(), Integer.valueOf(0));
        profilingTimes.put(annotation.getClass(), allTime + timeSpent);

        Integer allCount = profilingCounts.getOrDefault(annotation.getClass(), Integer.valueOf(0));
        profilingCounts.put(annotation.getClass(), allCount + 1);
    }

    public Map<Class<? extends ItemAnnotation>, Integer> getProfilingTimes() {
        return profilingTimes;
    }

    public Map<Class<? extends ItemAnnotation>, Integer> getProfilingCounts() {
        return profilingCounts;
    }

    public void resetProfiling() {
        profilingTimes.clear();
        profilingCounts.clear();
    }
}
