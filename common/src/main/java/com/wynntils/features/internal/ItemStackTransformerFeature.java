/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.internal;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.features.InternalFeature;
import com.wynntils.core.features.properties.EventListener;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.wc.custom.item.CosmeticItemStack;
import com.wynntils.wc.custom.item.GearItemStack;
import com.wynntils.wc.custom.item.UnidentifiedItemStack;
import com.wynntils.wc.custom.item.WynnItemStack;
import com.wynntils.wc.utils.WynnItemMatchers;
import com.wynntils.wc.utils.WynnUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@EventListener
public class ItemStackTransformerFeature extends InternalFeature {

    private final Map<Predicate<ItemStack>, Function<ItemStack, WynnItemStack>> TRANSFORMERS = new HashMap<>();

    public void registerTransformer(Predicate<ItemStack> pred, Function<ItemStack, WynnItemStack> cons) {
        TRANSFORMERS.put(pred, cons);
    }

    public void unregisterTransformer(Predicate<ItemStack> pred, Function<ItemStack, WynnItemStack> cons) {
        TRANSFORMERS.remove(pred, cons);
    }

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {
        registerTransformer(WynnItemMatchers::isGear, GearItemStack::new);
        registerTransformer(WynnItemMatchers::isUnidentified, UnidentifiedItemStack::new);
        registerTransformer(WynnItemMatchers::isCosmetic, CosmeticItemStack::new);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void transformItem(SetSlotEvent event) {
        if (!WynnUtils.onServer()) return;

        ItemStack stack = event.getItem();
        for (Map.Entry<Predicate<ItemStack>, Function<ItemStack, WynnItemStack>> e : TRANSFORMERS.entrySet()) {
            if (e.getKey().test(stack)) {
                ItemStack transformed = e.getValue().apply(stack);
                event.setItem(transformed);
                return;
            }
        }
    }
}
