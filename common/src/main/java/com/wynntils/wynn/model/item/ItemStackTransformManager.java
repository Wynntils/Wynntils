/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.managers.CoreManager;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.AmplifierTierProperty;
import com.wynntils.wynn.item.properties.ConsumableChargeProperty;
import com.wynntils.wynn.item.properties.CosmeticTierProperty;
import com.wynntils.wynn.item.properties.DailyRewardMultiplierProperty;
import com.wynntils.wynn.item.properties.DungeonKeyProperty;
import com.wynntils.wynn.item.properties.DurabilityProperty;
import com.wynntils.wynn.item.properties.EmeraldPouchTierProperty;
import com.wynntils.wynn.item.properties.GatheringToolProperty;
import com.wynntils.wynn.item.properties.HorseProperty;
import com.wynntils.wynn.item.properties.IngredientProperty;
import com.wynntils.wynn.item.properties.ItemTierProperty;
import com.wynntils.wynn.item.properties.MaterialProperty;
import com.wynntils.wynn.item.properties.PowderTierProperty;
import com.wynntils.wynn.item.properties.SearchOverlayProperty;
import com.wynntils.wynn.item.properties.ServerCountProperty;
import com.wynntils.wynn.item.properties.SkillIconProperty;
import com.wynntils.wynn.item.properties.SkillPointProperty;
import com.wynntils.wynn.item.properties.TeleportScrollProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemStackTransformManager extends CoreManager {
    private static final Set<ItemStackTransformer> TRANSFORMERS = ConcurrentHashMap.newKeySet();
    private static final Map<Predicate<ItemStack>, PropertyWriter> PROPERTIES = new HashMap<>();

    public static void registerTransformer(ItemStackTransformer transformer) {
        TRANSFORMERS.add(transformer);
    }

    public static void unregisterTransformer(ItemStackTransformer transformer) {
        TRANSFORMERS.remove(transformer);
    }

    public static void registerProperty(Predicate<ItemStack> pred, PropertyWriter cons) {
        PROPERTIES.put(pred, cons);
    }

    public static void unregisterProperty(Predicate<ItemStack> pred, PropertyWriter cons) {
        PROPERTIES.remove(pred, cons);
    }

    public static void init() {
        registerProperty(WynnItemMatchers::isDurabilityItem, DurabilityProperty::new);
        registerProperty(WynnItemMatchers::isTieredItem, ItemTierProperty::new);
        registerProperty(WynnItemMatchers::isCosmetic, CosmeticTierProperty::new);
        registerProperty(WynnItemMatchers::isDailyRewardsChest, DailyRewardMultiplierProperty::new);
        registerProperty(WynnItemMatchers::isPowder, PowderTierProperty::new);
        registerProperty(WynnItemMatchers::isEmeraldPouch, EmeraldPouchTierProperty::new);
        registerProperty(WynnItemMatchers::isSkillTyped, SkillIconProperty::new);
        registerProperty(WynnItemMatchers::isSkillPoint, SkillPointProperty::new);
        registerProperty(WynnItemMatchers::isTeleportScroll, TeleportScrollProperty::new);
        registerProperty(WynnItemMatchers::isDungeonKey, DungeonKeyProperty::new);
        registerProperty(WynnItemMatchers::isAmplifier, AmplifierTierProperty::new);
        registerProperty(WynnItemMatchers::isConsumable, ConsumableChargeProperty::new);
        registerProperty(WynnItemMatchers::isIngredient, IngredientProperty::new);
        registerProperty(WynnItemMatchers::isMaterial, MaterialProperty::new);
        registerProperty(WynnItemMatchers::isHorse, HorseProperty::new);
        registerProperty(WynnItemMatchers::isServerItem, ServerCountProperty::new);
        registerProperty(WynnItemMatchers::isGatheringTool, GatheringToolProperty::new);
        registerProperty(itemStack -> true, SearchOverlayProperty::new);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSetSlot(SetSlotEvent event) {
        ItemStack stack = event.getItem();

        // itemstack transformers
        for (ItemStackTransformer t : TRANSFORMERS) {
            if (t.test(stack)) {
                stack = t.transform(stack);
                break;
            }
        }

        // itemstack properties
        for (Map.Entry<Predicate<ItemStack>, PropertyWriter> e : PROPERTIES.entrySet()) {
            if (e.getKey().test(stack)) {
                if (!(stack instanceof WynnItemStack))
                    stack = new WynnItemStack(stack); // create WynnItemStack wrapper to hold properties if necessary

                e.getValue().attach((WynnItemStack) stack);
            }
        }

        if (stack instanceof WynnItemStack wynnItemStack) {
            wynnItemStack.init();
        }
        event.setItem(stack);
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

    @FunctionalInterface
    protected interface PropertyWriter {
        void attach(WynnItemStack stack);
    }
}
