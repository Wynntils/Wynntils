/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.model.item.properties.AmplifierTierPropertyModel;
import com.wynntils.wynn.model.item.properties.ConsumableChargePropertyModel;
import com.wynntils.wynn.model.item.properties.CosmeticTierPropertyModel;
import com.wynntils.wynn.model.item.properties.DailyRewardMultiplierPropertyModel;
import com.wynntils.wynn.model.item.properties.DungeonKeyPropertyModel;
import com.wynntils.wynn.model.item.properties.EmeraldPouchTierPropertyModel;
import com.wynntils.wynn.model.item.properties.GatheringToolPropertyModel;
import com.wynntils.wynn.model.item.properties.IngredientPropertyModel;
import com.wynntils.wynn.model.item.properties.ItemTierPropertyModel;
import com.wynntils.wynn.model.item.properties.MaterialPropertyModel;
import com.wynntils.wynn.model.item.properties.PowderTierPropertyModel;
import com.wynntils.wynn.model.item.properties.ServerCountPropertyModel;
import com.wynntils.wynn.model.item.properties.SkillIconPropertyModel;
import com.wynntils.wynn.model.item.properties.SkillPointPropertyModel;
import com.wynntils.wynn.model.item.properties.TeleportScrollPropertyModel;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemStackTransformManager extends CoreManager {
    private static final Set<ItemStackTransformer> TRANSFORMERS = ConcurrentHashMap.newKeySet();
    private static final Set<ItemPropertyWriter> PROPERTIES = ConcurrentHashMap.newKeySet();

    public static final List<Class<? extends Model>> HIGHLIGHT_PROPERTIES = List.of(
            CosmeticTierPropertyModel.class,
            EmeraldPouchItemStackModel.class,
            MaterialPropertyModel.class,
            IngredientPropertyModel.class,
            ItemTierPropertyModel.class,
            PowderTierPropertyModel.class);
    public static final List<Class<? extends Model>> TEXT_OVERLAY_PROPERTIES = List.of(
            AmplifierTierPropertyModel.class,
            ConsumableChargePropertyModel.class,
            DailyRewardMultiplierPropertyModel.class,
            DungeonKeyPropertyModel.class,
            EmeraldPouchTierPropertyModel.class,
            GatheringToolPropertyModel.class,
            PowderTierPropertyModel.class,
            ServerCountPropertyModel.class,
            SkillIconPropertyModel.class,
            SkillPointPropertyModel.class,
            TeleportScrollPropertyModel.class);

    public ItemStackTransformManager() {
        super(List.of());
    }

    @Override
    public void init() {}

    public static void registerTransformer(ItemStackTransformer transformer) {
        TRANSFORMERS.add(transformer);
    }

    public static void unregisterTransformer(ItemStackTransformer transformer) {
        TRANSFORMERS.remove(transformer);
    }

    public static void registerProperty(ItemPropertyWriter writer) {
        PROPERTIES.add(writer);
    }

    public static void unregisterProperty(ItemPropertyWriter writer) {
        PROPERTIES.remove(writer);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSetSlot(SetSlotEvent.Pre event) {
        ItemStack stack = event.getItem();

        // itemstack transformers
        for (ItemStackTransformer t : TRANSFORMERS) {
            if (t.test(stack)) {
                stack = t.transform(stack);
                break;
            }
        }

        // itemstack properties
        for (ItemPropertyWriter w : PROPERTIES) {
            if (w.test(stack)) {
                if (!(stack instanceof WynnItemStack)) stack = new WynnItemStack(stack);

                w.attach((WynnItemStack) stack);
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
