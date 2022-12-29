/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.wynn.item.ServerItemStack;
import com.wynntils.wynn.item.SoulPointItemStack;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.CosmeticTierProperty;
import com.wynntils.wynn.item.properties.DailyRewardMultiplierProperty;
import com.wynntils.wynn.item.properties.SearchOverlayProperty;
import com.wynntils.wynn.item.properties.ServerCountProperty;
import com.wynntils.wynn.item.properties.SkillIconProperty;
import com.wynntils.wynn.item.properties.SkillPointProperty;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

public class ItemModel extends Model {
    @Override
    public void init() {
        // === game, done
        Handlers.Item.registerAnnotator(new GearAnnotator());
        Handlers.Item.registerAnnotator(new IngredientAnnotator());
        Handlers.Item.registerAnnotator(new PowderAnnotator());
        Handlers.Item.registerAnnotator(new GearBoxAnnotator());
        Handlers.Item.registerAnnotator(new EmeraldPouchAnnotator());

        // === game, left
        Handlers.Item.registerAnnotator(new AmplifierAnnotator());
        Handlers.Item.registerAnnotator(new ConsumableAnnotator());
        Handlers.Item.registerAnnotator(new DungeonKeyAnnotator());
        Handlers.Item.registerAnnotator(new GatheringToolAnnotator());
        Handlers.Item.registerAnnotator(new HorseAnnotator());
        Handlers.Item.registerAnnotator(new MaterialAnnotator());
        Handlers.Item.registerAnnotator(new TeleportScrollAnnotator());
        Handlers.Item.registerAnnotator(new CraftedGearAnnotator());

        // === gui
        Handlers.Item.registerAnnotator(new ServerAnnotator());
        Handlers.Item.registerAnnotator(new SoulPointAnnotator());

        Handlers.Item.registerAnnotator(new CosmeticTierAnnotator());
        Handlers.Item.registerAnnotator(new DailyRewardMultiplierAnnotator());
        Handlers.Item.registerAnnotator(new SearchOverlayAnnotator());
        Handlers.Item.registerAnnotator(new ServerCountAnnotator());
        Handlers.Item.registerAnnotator(new SkillIconAnnotator());
        Handlers.Item.registerAnnotator(new SkillPointAnnotator());
    }

    @Override
    public void disable() {
        // FIXME
    }

    /// ==== game ====

    // CraftedConsumables -- Scroll, Food, Potion
    // Potions

    /// ==== gui ====

    public static final class ServerAnnotator extends Annotator {
        public ServerAnnotator() {
            super(WynnItemMatchers::isServerItem, ServerItemStack::new);
        }
    }

    public static final class SoulPointAnnotator extends Annotator {
        public SoulPointAnnotator() {
            super(WynnItemMatchers::isSoulPoint, SoulPointItemStack::new);
        }
    }

    public static final class CosmeticTierAnnotator extends PropertyAnnotator {
        public CosmeticTierAnnotator() {
            super(WynnItemMatchers::isCosmetic, CosmeticTierProperty::new);
        }
    }

    public static final class DailyRewardMultiplierAnnotator extends PropertyAnnotator {
        public DailyRewardMultiplierAnnotator() {
            super(WynnItemMatchers::isDailyRewardsChest, DailyRewardMultiplierProperty::new);
        }
    }

    public static final class SearchOverlayAnnotator extends PropertyAnnotator {
        public SearchOverlayAnnotator() {
            super(itemstack -> true, SearchOverlayProperty::new);
        }
    }

    public static final class ServerCountAnnotator extends PropertyAnnotator {
        public ServerCountAnnotator() {
            super(WynnItemMatchers::isServerItem, ServerCountProperty::new);
        }
    }

    public static final class SkillIconAnnotator extends PropertyAnnotator {
        public SkillIconAnnotator() {
            super(WynnItemMatchers::isSkillTyped, SkillIconProperty::new);
        }
    }

    public static final class SkillPointAnnotator extends PropertyAnnotator {
        public SkillPointAnnotator() {
            super(WynnItemMatchers::isSkillPoint, SkillPointProperty::new);
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
