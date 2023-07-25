/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.DurableItemProperty;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class InventoryFunctions {
    public static class CappedInventorySlotsFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.PlayerInventory.getInventorySlots();
        }
    }

    public static class CappedIngredientPouchSlotsFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.PlayerInventory.getIngredientPouchSlots();
        }
    }

    public static class CappedHeldItemDurabilityFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            ItemStack itemStack = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);
            Optional<DurableItemProperty> durableItemOpt =
                    Models.Item.asWynnItemPropery(itemStack, DurableItemProperty.class);
            if (durableItemOpt.isEmpty()) return CappedValue.EMPTY;

            return durableItemOpt.get().getDurability();
        }
    }

    public static class EmeraldStringFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return Models.Emerald.getFormattedString(
                    Models.Emerald.getAmountInInventory(),
                    arguments.getArgument("zeros").getBooleanValue());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("zeros", Boolean.class, false)));
        }

        @Override
        public List<String> getAliases() {
            return List.of("estr");
        }
    }

    public static class LiquidEmeraldFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
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
        public Integer getValue(FunctionArguments arguments) {
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
        public Integer getValue(FunctionArguments arguments) {
            return Models.Emerald.getAmountInInventory() % 64;
        }

        @Override
        public List<String> getAliases() {
            return List.of("em");
        }
    }

    public static class MoneyFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Emerald.getAmountInInventory();
        }
    }

    public static class InventoryFreeFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.PlayerInventory.getInventorySlots().getRemaining();
        }

        @Override
        public List<String> getAliases() {
            return List.of("inv_free");
        }
    }

    public static class InventoryUsedFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.PlayerInventory.getInventorySlots().current();
        }

        @Override
        public List<String> getAliases() {
            return List.of("inv_used");
        }
    }

    public static class IngredientPouchOpenSlotsFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.PlayerInventory.getIngredientPouchSlots().getRemaining();
        }

        @Override
        public List<String> getAliases() {
            return List.of("pouch_open", "pouch_free");
        }
    }

    public static class IngredientPouchUsedSlotsFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.PlayerInventory.getIngredientPouchSlots().current();
        }

        @Override
        public List<String> getAliases() {
            return List.of("pouch_used");
        }
    }

    public static class HeldItemCurrentDurabilityFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            ItemStack itemStack = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);
            Optional<DurableItemProperty> durableItemOpt =
                    Models.Item.asWynnItemPropery(itemStack, DurableItemProperty.class);
            if (durableItemOpt.isEmpty()) return -1;

            return durableItemOpt.get().getDurability().current();
        }

        @Override
        public List<String> getAliases() {
            return List.of("current_held_durability");
        }
    }

    public static class HeldItemMaxDurabilityFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            ItemStack itemStack = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);
            Optional<DurableItemProperty> durableItemOpt =
                    Models.Item.asWynnItemPropery(itemStack, DurableItemProperty.class);
            if (durableItemOpt.isEmpty()) return -1;

            return durableItemOpt.get().getDurability().max();
        }

        @Override
        public List<String> getAliases() {
            return List.of("max_held_durability");
        }
    }

    public static class HeldItemTypeFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            ItemStack itemInHand = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);

            if (itemInHand == null) {
                return "NONE";
            }

            Optional<WynnItem> wynnItem = Models.Item.getWynnItem(itemInHand);

            if (wynnItem.isEmpty()) {
                return "NONE";
            }

            return wynnItem.get().getClass().getSimpleName();
        }

        @Override
        public List<String> getAliases() {
            return List.of("held_type");
        }
    }

    public static class HeldItemNameFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            ItemStack itemStack = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);
            StyledText hoverName = StyledText.fromComponent(itemStack.getHoverName());
            if (!arguments.getArgument("formatted").getBooleanValue()) {
                return hoverName.getString(PartStyle.StyleType.NONE);
            }
            return hoverName.getString();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("formatted", Boolean.class, false)));
        }

        @Override
        public List<String> getAliases() {
            return List.of("held_item", "held_name");
        }
    }
}
