/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item;

import com.wynntils.core.managers.Manager;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.wc.custom.item.properties.AmplifierTierProperty;
import com.wynntils.wc.custom.item.properties.ConsumableChargeProperty;
import com.wynntils.wc.custom.item.properties.CosmeticTierProperty;
import com.wynntils.wc.custom.item.properties.DailyRewardMultiplierProperty;
import com.wynntils.wc.custom.item.properties.DungeonKeyProperty;
import com.wynntils.wc.custom.item.properties.DurabilityProperty;
import com.wynntils.wc.custom.item.properties.IngredientProperty;
import com.wynntils.wc.custom.item.properties.ItemProperty;
import com.wynntils.wc.custom.item.properties.ItemTierProperty;
import com.wynntils.wc.custom.item.properties.MaterialProperty;
import com.wynntils.wc.custom.item.properties.PowderTierProperty;
import com.wynntils.wc.custom.item.properties.ProfessionLevelProperty;
import com.wynntils.wc.custom.item.properties.SkillIconProperty;
import com.wynntils.wc.custom.item.properties.SkillPointProperty;
import com.wynntils.wc.custom.item.properties.TeleportScrollProperty;
import com.wynntils.wc.utils.WynnItemMatchers;
import com.wynntils.wc.utils.WynnUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemStackTransformManager extends Manager {
    private static final Map<Predicate<ItemStack>, ItemStackTransformer> TRANSFORMERS = new HashMap<>();
    private static final Map<Predicate<ItemStack>, PropertyWriter> PROPERTIES = new HashMap<>();

    public static void registerTransformer(Predicate<ItemStack> pred, ItemStackTransformer cons) {
        TRANSFORMERS.put(pred, cons);
    }

    public void unregisterTransformer(Predicate<ItemStack> pred, ItemStackTransformer cons) {
        TRANSFORMERS.remove(pred, cons);
    }

    public static void registerProperty(Predicate<ItemStack> pred, PropertyWriter cons) {
        PROPERTIES.put(pred, cons);
    }

    public void unregisterProperty(Predicate<ItemStack> pred, PropertyWriter cons) {
        PROPERTIES.remove(pred, cons);
    }

    public static void init() {
        registerTransformer(WynnItemMatchers::isKnownGear, GearItemStack::new);
        registerTransformer(WynnItemMatchers::isUnidentified, UnidentifiedItemStack::new);
        registerTransformer(WynnItemMatchers::isSoulPoint, SoulPointItemStack::new);

        registerProperty(WynnItemMatchers::isDurabilityItem, DurabilityProperty::new);
        registerProperty(WynnItemMatchers::isTieredItem, ItemTierProperty::new);
        registerProperty(WynnItemMatchers::isCosmetic, CosmeticTierProperty::new);
        registerProperty(WynnItemMatchers::isDailyRewardsChest, DailyRewardMultiplierProperty::new);
        registerProperty(WynnItemMatchers::isPowder, PowderTierProperty::new);
        registerProperty(WynnItemMatchers::isProfessionLevel, ProfessionLevelProperty::new);
        registerProperty(WynnItemMatchers::isSkillTyped, SkillIconProperty::new);
        registerProperty(WynnItemMatchers::isSkillPoint, SkillPointProperty::new);
        registerProperty(WynnItemMatchers::isTeleportScroll, TeleportScrollProperty::new);
        registerProperty(WynnItemMatchers::isDungeonKey, DungeonKeyProperty::new);
        registerProperty(WynnItemMatchers::isAmplifier, AmplifierTierProperty::new);
        registerProperty(WynnItemMatchers::isConsumable, ConsumableChargeProperty::new);
        registerProperty(WynnItemMatchers::isIngredient, IngredientProperty::new);
        registerProperty(WynnItemMatchers::isMaterial, MaterialProperty::new);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSetSlot(SetSlotEvent event) {
        if (!WynnUtils.onServer()) return;

        ItemStack stack = event.getItem();

        // itemstack transformers
        for (Map.Entry<Predicate<ItemStack>, ItemStackTransformer> e : TRANSFORMERS.entrySet()) {
            if (e.getKey().test(stack)) {
                stack = e.getValue().transform(stack);
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

        event.setItem(stack);
    }

    @FunctionalInterface
    private interface ItemStackTransformer {
        WynnItemStack transform(ItemStack stack);
    }

    @FunctionalInterface
    private interface PropertyWriter {
        ItemProperty attach(WynnItemStack stack);
    }
}
