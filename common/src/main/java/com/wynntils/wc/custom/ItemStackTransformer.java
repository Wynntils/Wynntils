/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom;

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

public class ItemStackTransformer {

    private static final Map<Predicate<ItemStack>, Function<ItemStack, WynnItemStack>> TRANSFORMERS = new HashMap<>();

    public static void registerTransformer(Predicate<ItemStack> pred, Function<ItemStack, WynnItemStack> cons) {
        TRANSFORMERS.put(pred, cons);
    }

    public static void unregisterTransformer(Predicate<ItemStack> pred, Function<ItemStack, WynnItemStack> cons) {
        TRANSFORMERS.remove(pred, cons);
    }

    public static void init() {
        registerTransformer(WynnItemMatchers::isGear, GearItemStack::new);
        registerTransformer(WynnItemMatchers::isUnidentified, UnidentifiedItemStack::new);
        registerTransformer(WynnItemMatchers::isCosmetic, CosmeticItemStack::new);
    }

    public static ItemStack transform(ItemStack stack) {
        if (!WynnUtils.onServer()) return stack;

        for (Map.Entry<Predicate<ItemStack>, Function<ItemStack, WynnItemStack>> e : TRANSFORMERS.entrySet()) {
            if (e.getKey().test(stack)) return e.getValue().apply(stack);
        }

        // nothing to transform
        return stack;
    }
}
