/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.item;

import com.wynntils.core.components.Handler;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.SetSlotEvent;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemHandler extends Handler {
    public static Optional<ItemAnnotation> getItemStackAnnotation(ItemStack item) {
        if (!(item instanceof AnnotatedItemStack annotatedItemStack)) return Optional.empty();

        return Optional.of(annotatedItemStack.getAnnotation());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSetSlot(SetSlotEvent.Pre event) {
        event.setItem(annotate(event.getItem()));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onContainerSetContent(ContainerSetContentEvent.Pre event) {
        event.getItems().replaceAll(this::annotate);
    }

    private ItemStack annotate(ItemStack stack) {
        ItemAnnotation annotation = findAnnotation(stack);
        return new AnnotatedItemStack(stack, annotation);
    }

    private ItemAnnotation findAnnotation(ItemStack stack) {
        return null;
    }
}
