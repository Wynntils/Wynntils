/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.functions.Function;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.gui.IngredientPouchItem;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class InventoryFunctions {
    public static class LiquidEmeraldFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            int ems = Models.Emerald.getAmountInInventory();
            return ems / 4096;
        }

        @Override
        public List<String> getAliases() {
            return List.of("le");
        }
    }

    public static class EmeraldBlockFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            int ems = Models.Emerald.getAmountInInventory();
            return (ems % 4096) / 64;
        }

        @Override
        public List<String> getAliases() {
            return List.of("eb");
        }
    }

    public static class EmeraldsFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Emerald.getAmountInInventory() % 64;
        }

        @Override
        public List<String> getAliases() {
            return List.of("em");
        }
    }

    public static class MoneyFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.Emerald.getAmountInInventory();
        }
    }

    public static class InventoryFreeFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.PlayerInventory.getOpenInvSlots();
        }

        @Override
        public List<String> getAliases() {
            return List.of("inv_free");
        }
    }

    public static class InventoryUsedFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.PlayerInventory.getUsedInvSlots();
        }

        @Override
        public List<String> getAliases() {
            return List.of("inv_used");
        }
    }

    public static class IngredientPouchOpenSlotsFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            ItemStack itemStack = McUtils.inventory().items.get(InventoryUtils.INGREDIENT_POUCH_SLOT_NUM);

            Optional<WynnItem> wynnItem = Models.Item.getWynnItem(itemStack);

            if (wynnItem.isPresent() && wynnItem.get() instanceof IngredientPouchItem pouchItem) {
                return 27 - pouchItem.getCount();
            }

            return -1;
        }

        @Override
        public List<String> getAliases() {
            return List.of("pouch_open");
        }
    }

    public static class IngredientPouchUsedSlotsFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            ItemStack itemStack = McUtils.inventory().items.get(InventoryUtils.INGREDIENT_POUCH_SLOT_NUM);

            Optional<WynnItem> wynnItem = Models.Item.getWynnItem(itemStack);

            if (wynnItem.isPresent() && wynnItem.get() instanceof IngredientPouchItem pouchItem) {
                return pouchItem.getCount();
            }

            return -1;
        }

        @Override
        public List<String> getAliases() {
            return List.of("pouch_used");
        }
    }

    public static class HeldItemCurrentDurabilityFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            ItemStack itemStack = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);

            Optional<WynnItem> wynnItem = Models.Item.getWynnItem(itemStack);

            if (wynnItem.isPresent() && wynnItem.get() instanceof CraftedGearItem craftedGearItem) {
                return craftedGearItem.getDurability().getCurrent();
            }

            return -1;
        }

        @Override
        public List<String> getAliases() {
            return List.of("held_durability");
        }
    }

    public static class HeldItemMaxDurabilityFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            ItemStack itemStack = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);

            Optional<WynnItem> wynnItem = Models.Item.getWynnItem(itemStack);

            if (wynnItem.isPresent() && wynnItem.get() instanceof CraftedGearItem craftedGearItem) {
                return craftedGearItem.getDurability().getMax();
            }

            return -1;
        }

        @Override
        public List<String> getAliases() {
            return List.of("held_durability");
        }
    }
}
