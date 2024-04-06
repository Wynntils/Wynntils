/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.containers.type.InventoryAccessory;
import com.wynntils.models.containers.type.InventoryArmor;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.DurableItemProperty;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.NamedValue;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;

public class InventoryFunctions {
    public static class AccessoryDurabilityFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            InventoryAccessory inventoryAccessory = InventoryAccessory.fromString(
                    arguments.getArgument("accessory").getStringValue());
            if (inventoryAccessory == null) return CappedValue.EMPTY;

            Optional<DurableItemProperty> durableItemOpt = Models.Item.asWynnItemProperty(
                    McUtils.inventory().items.get(inventoryAccessory.getSlot()), DurableItemProperty.class);

            if (durableItemOpt.isEmpty()) return CappedValue.EMPTY;

            return durableItemOpt.get().getDurability();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("accessory", String.class, null)));
        }
    }

    public static class AllShinyStatsFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            List<ShinyStat> allShinyStats = Models.Shiny.getAllShinyStats();
            return allShinyStats.stream()
                    .map(s -> s.statType().displayName() + ": " + s.value())
                    .collect(Collectors.joining("\n"));
        }
    }

    public static class ArmorDurabilityFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            InventoryArmor inventoryArmor =
                    InventoryArmor.fromString(arguments.getArgument("armor").getStringValue());
            if (inventoryArmor == null) return CappedValue.EMPTY;

            Optional<DurableItemProperty> durableItemOpt = Models.Item.asWynnItemProperty(
                    McUtils.inventory().armor.get(inventoryArmor.getSlot()), DurableItemProperty.class);

            if (durableItemOpt.isEmpty()) return CappedValue.EMPTY;

            return durableItemOpt.get().getDurability();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("armor", String.class, null)));
        }
    }

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
            ItemStack itemStack = InventoryUtils.getItemInHand();
            Optional<DurableItemProperty> durableItemOpt =
                    Models.Item.asWynnItemProperty(itemStack, DurableItemProperty.class);
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
        protected List<String> getAliases() {
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
        protected List<String> getAliases() {
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
        protected List<String> getAliases() {
            return List.of("eb");
        }
    }

    public static class EmeraldsFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Emerald.getAmountInInventory() % 64;
        }

        @Override
        protected List<String> getAliases() {
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
        protected List<String> getAliases() {
            return List.of("inv_free");
        }
    }

    public static class InventoryUsedFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.PlayerInventory.getInventorySlots().current();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("inv_used");
        }
    }

    public static class IngredientPouchOpenSlotsFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.PlayerInventory.getIngredientPouchSlots().getRemaining();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("pouch_open", "pouch_free");
        }
    }

    public static class IngredientPouchUsedSlotsFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.PlayerInventory.getIngredientPouchSlots().current();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("pouch_used");
        }
    }

    public static class HeldItemCurrentDurabilityFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            ItemStack itemStack = InventoryUtils.getItemInHand();
            Optional<DurableItemProperty> durableItemOpt =
                    Models.Item.asWynnItemProperty(itemStack, DurableItemProperty.class);
            if (durableItemOpt.isEmpty()) return -1;

            return durableItemOpt.get().getDurability().current();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("current_held_durability");
        }
    }

    public static class HeldItemMaxDurabilityFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            ItemStack itemStack = InventoryUtils.getItemInHand();
            Optional<DurableItemProperty> durableItemOpt =
                    Models.Item.asWynnItemProperty(itemStack, DurableItemProperty.class);
            if (durableItemOpt.isEmpty()) return -1;

            return durableItemOpt.get().getDurability().max();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("max_held_durability");
        }
    }

    public static class HeldItemShinyStatFunction extends Function<NamedValue> {
        @Override
        public NamedValue getValue(FunctionArguments arguments) {
            ItemStack itemStack = InventoryUtils.getItemInHand();
            Optional<ShinyStat> shinyStatOpt = Models.Shiny.getShinyStat(itemStack);
            if (shinyStatOpt.isEmpty()) return NamedValue.EMPTY;

            // FIXME: The function system can't handle longs, so we have to cast to int
            return new NamedValue(shinyStatOpt.get().statType().displayName(), (int)
                    shinyStatOpt.get().value());
        }
    }

    public static class HeldItemTypeFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            ItemStack itemInHand = InventoryUtils.getItemInHand();

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
        protected List<String> getAliases() {
            return List.of("held_type");
        }
    }

    public static class HeldItemNameFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            ItemStack itemStack = InventoryUtils.getItemInHand();
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
        protected List<String> getAliases() {
            return List.of("held_item", "held_name");
        }
    }
}
