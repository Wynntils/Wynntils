/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.components.Manager;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.wynn.item.WynnItemStack;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ItemStackTransformManager extends Manager {
    private final Set<ItemStackTransformer> transformers = ConcurrentHashMap.newKeySet();
    private final Set<ItemPropertyWriter> properties = ConcurrentHashMap.newKeySet();

    public ItemStackTransformManager() {
        super(List.of());
    }

    public void registerTransformer(ItemStackTransformer transformer) {
        transformers.add(transformer);
    }

    public void unregisterTransformer(ItemStackTransformer transformer) {
        transformers.remove(transformer);
    }

    public void registerProperty(ItemPropertyWriter writer) {
        properties.add(writer);
    }

    public void unregisterProperty(ItemPropertyWriter writer) {
        properties.remove(writer);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSetSlot(SetSlotEvent.Pre event) {
        //   event.setItem(transformItem(event.getItem()));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onContainerSetContent(ContainerSetContentEvent.Pre event) {
        //    event.getItems().replaceAll(this::transformItem);
    }

    private ItemStack transformItem(ItemStack stack) {
        // itemstack transformers
        for (ItemStackTransformer t : transformers) {
            if (t.test(stack)) {
                stack = t.transform(stack);
                break;
            }
        }

        // itemstack properties
        for (ItemPropertyWriter w : properties) {
            if (w.test(stack)) {
                if (!(stack instanceof WynnItemStack)) stack = new WynnItemStack(stack);

                w.attach((WynnItemStack) stack);
            }
        }

        if (stack instanceof WynnItemStack wynnItemStack) {
            wynnItemStack.init();
        }

        return stack;
    }

    public static class ItemStackTransformer {
        private final Predicate<ItemStack> predicate;
        private final Function<ItemStack, WynnItemStack> transformer;

        public ItemStackTransformer(Predicate<ItemStack> predicate, Function<ItemStack, WynnItemStack> transformer) {
            this.predicate = predicate;
            this.transformer = transformer;
        }

        public boolean test(ItemStack item) {
            return predicate.test(item);
        }

        public WynnItemStack transform(ItemStack item) {
            return transformer.apply(item);
        }
    }

    public static class ItemPropertyWriter {
        private final Predicate<ItemStack> predicate;
        private final Consumer<WynnItemStack> writer;

        public ItemPropertyWriter(Predicate<ItemStack> predicate, Consumer<WynnItemStack> writer) {
            this.predicate = predicate;
            this.writer = writer;
        }

        public boolean test(ItemStack item) {
            return predicate.test(item);
        }

        public void attach(WynnItemStack item) {
            writer.accept(item);
        }
    }
}
