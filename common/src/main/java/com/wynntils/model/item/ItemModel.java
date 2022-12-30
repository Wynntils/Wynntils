/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.model.item.annotators.AmplifierAnnotator;
import com.wynntils.model.item.annotators.CraftedConsumableAnnotator;
import com.wynntils.model.item.annotators.CraftedGearAnnotator;
import com.wynntils.model.item.annotators.DungeonKeyAnnotator;
import com.wynntils.model.item.annotators.EmeraldPouchAnnotator;
import com.wynntils.model.item.annotators.GatheringToolAnnotator;
import com.wynntils.model.item.annotators.GearAnnotator;
import com.wynntils.model.item.annotators.GearBoxAnnotator;
import com.wynntils.model.item.annotators.HealthPotionAnnotator;
import com.wynntils.model.item.annotators.HorseAnnotator;
import com.wynntils.model.item.annotators.IngredientAnnotator;
import com.wynntils.model.item.annotators.ManaPotionAnnotator;
import com.wynntils.model.item.annotators.MaterialAnnotator;
import com.wynntils.model.item.annotators.PowderAnnotator;
import com.wynntils.model.item.annotators.SkillPotionAnnotator;
import com.wynntils.model.item.annotators.TeleportScrollAnnotator;
import com.wynntils.model.item.annotators.XpPotionAnnotator;
import com.wynntils.model.item.annotators.gui.CosmeticTierAnnotator;
import com.wynntils.model.item.annotators.gui.DailyRewardMultiplierAnnotator;
import com.wynntils.model.item.annotators.gui.ServerAnnotator;
import com.wynntils.model.item.annotators.gui.SkillPointAnnotator;
import com.wynntils.model.item.annotators.gui.SoulPointAnnotator;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.SearchOverlayProperty;
import com.wynntils.wynn.item.properties.SkillIconProperty;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

public class ItemModel extends Model {
    @Override
    public void init() {
        // FIXME: What is stealing our item stacks?!
        Handlers.Item.registerAnnotator(new SkillPointAnnotator());

        // For efficiency, register these annotator first
        Handlers.Item.registerAnnotator(new GearAnnotator());
        Handlers.Item.registerAnnotator(new GearBoxAnnotator());
        Handlers.Item.registerAnnotator(new IngredientAnnotator());
        Handlers.Item.registerAnnotator(new MaterialAnnotator());

        // Then alphabetically
        Handlers.Item.registerAnnotator(new AmplifierAnnotator());
        Handlers.Item.registerAnnotator(new CraftedConsumableAnnotator());
        Handlers.Item.registerAnnotator(new CraftedGearAnnotator());
        Handlers.Item.registerAnnotator(new DungeonKeyAnnotator());
        Handlers.Item.registerAnnotator(new EmeraldPouchAnnotator());
        Handlers.Item.registerAnnotator(new GatheringToolAnnotator());
        Handlers.Item.registerAnnotator(new HealthPotionAnnotator());
        Handlers.Item.registerAnnotator(new HorseAnnotator());
        Handlers.Item.registerAnnotator(new ManaPotionAnnotator());
        Handlers.Item.registerAnnotator(new PowderAnnotator());
        Handlers.Item.registerAnnotator(new SkillPotionAnnotator());
        Handlers.Item.registerAnnotator(new TeleportScrollAnnotator());
        Handlers.Item.registerAnnotator(new XpPotionAnnotator());

        // === gui
        Handlers.Item.registerAnnotator(new DailyRewardMultiplierAnnotator());
        Handlers.Item.registerAnnotator(new ServerAnnotator());
        Handlers.Item.registerAnnotator(new SoulPointAnnotator());
        Handlers.Item.registerAnnotator(new CosmeticTierAnnotator());

        Handlers.Item.registerAnnotator(new SearchOverlayAnnotator());
    }

    @Override
    public void disable() {
        // FIXME
    }

    /// ==== gui ====

    public static final class SearchOverlayAnnotator extends PropertyAnnotator {
        public SearchOverlayAnnotator() {
            super(itemstack -> true, SearchOverlayProperty::new);
        }
    }

    public abstract static class Annotator implements ItemAnnotator {
        private final Predicate<ItemStack> pred;
        private final Function<ItemStack, WynnItemStack> cons;

        protected Annotator(Predicate<ItemStack> pred, Function<ItemStack, WynnItemStack> cons) {
            this.pred = pred;
            this.cons = cons;
        }

        public ItemAnnotation getAnnotation(ItemStack itemStack) {
            if (!pred.test(itemStack)) return null;
            WynnItemStack converted = cons.apply(itemStack);

            return converted;
        }
    }

    public abstract static class PropertyAnnotator implements ItemAnnotator {
        private final Predicate<ItemStack> pred;
        private final Consumer<WynnItemStack> cons;

        protected PropertyAnnotator(Predicate<ItemStack> pred, Consumer<WynnItemStack> cons) {
            this.pred = pred;
            this.cons = cons;
        }

        @Override
        public ItemAnnotation getAnnotation(ItemStack itemStack) {
            if (!pred.test(itemStack)) return null;

            cons.accept(new WynnItemStack(itemStack));
            return null;
        }
    }
}
