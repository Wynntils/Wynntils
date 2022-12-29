/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.item;

import com.wynntils.core.components.Handler;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.SetSlotEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemHandler extends Handler {
    private final List<ItemAnnotator> annotators = new ArrayList<>();

    public static Optional<ItemAnnotation> getItemStackAnnotation(ItemStack item) {
        if (!(item instanceof AnnotatedItemStack annotatedItemStack)) return Optional.empty();

        return Optional.of(annotatedItemStack.getAnnotation());
    }

    public void registerAnnotator(ItemAnnotator annotator) {
        annotators.add(annotator);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSetSlot(SetSlotEvent.Pre event) {
        event.setItem(annotate(event.getItem()));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onContainerSetContent(ContainerSetContentEvent.Pre event) {
        event.getItems().replaceAll(this::annotate);
    }

    private ItemStack annotate(ItemStack item) {
        if (item instanceof AnnotatedItemStack) return item;

        Optional<ItemAnnotation> annotationOpt = annotators.stream()
                .map(annotator -> annotator.getAnnotation(item))
                .filter(Objects::nonNull)
                .findFirst();

        if (annotationOpt.isEmpty()) return item;

        return new AnnotatedItemStack(item, annotationOpt.get());
    }
}
