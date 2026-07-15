/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.models.inventory.type.InventoryAccessory;
import com.wynntils.models.inventory.type.InventoryArmor;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.DurableItemProperty;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.NamedValue;
import com.wynntils.utils.wynn.InventoryUtils;
import com.wynntils.utils.wynn.ItemUtils;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;
import com.wynntils.templates.annotations.TemplateFunction;

//Functions are accessed via reflection
@SuppressWarnings("unused")
public class InventoryFunctions {

    @TemplateFunction(name = "accessory_durability")
    public CappedValue accessoryDurabilityFunction(String accessory) {
        InventoryAccessory inventoryAccessory = InventoryAccessory.fromString(accessory);
        if (inventoryAccessory == null)
            return CappedValue.EMPTY;
        Optional<DurableItemProperty> durableItemOpt = Models.Item.asWynnItemProperty(McUtils.inventory().items.get(inventoryAccessory.getSlot()), DurableItemProperty.class);
        if (durableItemOpt.isEmpty())
            return CappedValue.EMPTY;
        return durableItemOpt.get().getDurability();
    }

    @TemplateFunction(name = "all_shiny_stats")
    public String allShinyStatsFunction() {
        List<ShinyStat> allShinyStats = Models.Shiny.getAllShinyStats();
        return allShinyStats.stream().map(s -> s.statType().displayName() + ": " + s.value()).collect(Collectors.joining("\n"));
    }

    @TemplateFunction(name = "armor_durability")
    public CappedValue armorDurabilityFunction(String armor) {
        InventoryArmor inventoryArmor = InventoryArmor.fromString(armor);
        if (inventoryArmor == null)
            return CappedValue.EMPTY;
        Optional<DurableItemProperty> durableItemOpt = Models.Item.asWynnItemProperty(McUtils.inventory().getItem(inventoryArmor.getInventorySlot()), DurableItemProperty.class);
        if (durableItemOpt.isEmpty())
            return CappedValue.EMPTY;
        return durableItemOpt.get().getDurability();
    }

    @TemplateFunction(name = "equipped_accessory_name")
    public String equippedAccessoryNameFunction(String accessory) {
        InventoryAccessory inventoryAccessory = InventoryAccessory.fromString(accessory);
        if (inventoryAccessory == null)
            return "NONE";
        ItemStack accessoryStack = McUtils.inventory().items.get(inventoryAccessory.getSlot());
        if (ItemUtils.isEmptyAccessorySlot(accessoryStack))
            return "NONE";
        StyledText hoverName = StyledText.fromComponent(accessoryStack.getHoverName());
        return WynnUtils.stripItemNameMarkers(hoverName.getString(StyleType.NONE));
    }

    @TemplateFunction(name = "equipped_armor_name")
    public String equippedArmorNameFunction(String armor) {
        InventoryArmor inventoryArmor = InventoryArmor.fromString(armor);
        if (inventoryArmor == null)
            return "NONE";
        ItemStack armorStack = McUtils.inventory().getItem(inventoryArmor.getInventorySlot());
        if (ItemUtils.isEmptyArmorSlot(armorStack))
            return "NONE";
        StyledText hoverName = StyledText.fromComponent(armorStack.getHoverName());
        return WynnUtils.stripItemNameMarkers(hoverName.getString(StyleType.NONE));
    }

    @TemplateFunction(name = "capped_inventory_slots")
    public CappedValue cappedInventorySlotsFunction() {
        return Models.Inventory.getInventorySlots();
    }

    @TemplateFunction(name = "capped_ingredient_pouch_slots")
    public CappedValue cappedIngredientPouchSlotsFunction() {
        return Models.IngredientPouch.getIngredientPouchSlots();
    }

    @TemplateFunction(name = "capped_held_item_durability")
    public CappedValue cappedHeldItemDurabilityFunction() {
        ItemStack itemStack = InventoryUtils.getItemInHand();
        Optional<DurableItemProperty> durableItemOpt = Models.Item.asWynnItemProperty(itemStack, DurableItemProperty.class);
        if (durableItemOpt.isEmpty())
            return CappedValue.EMPTY;
        return durableItemOpt.get().getDurability();
    }

    @TemplateFunction(name = "emerald_string", aliases = { "estr" })
    public String emeraldStringFunction(boolean zeros) {
        return Models.Emerald.getFormattedString(Models.Emerald.getAmountInInventory(), zeros);
    }

    @TemplateFunction(name = "liquid_emerald", aliases = { "le" })
    public int liquidEmeraldFunction() {
        int ems = Models.Emerald.getAmountInInventory();
        return ems / 4096;
    }

    @TemplateFunction(name = "emerald_block", aliases = { "eb" })
    public int emeraldBlockFunction() {
        int ems = Models.Emerald.getAmountInInventory();
        return (ems % 4096) / 64;
    }

    @TemplateFunction(name = "emeralds", aliases = { "em" })
    public int emeraldsFunction() {
        return Models.Emerald.getAmountInInventory() % 64;
    }

    @TemplateFunction(name = "money")
    public int moneyFunction() {
        return Models.Emerald.getAmountInInventory();
    }

    @TemplateFunction(name = "inventory_free", aliases = { "inv_free" })
    public int inventoryFreeFunction() {
        return Models.Inventory.getInventorySlots().getRemaining();
    }

    @TemplateFunction(name = "inventory_used", aliases = { "inv_used" })
    public int inventoryUsedFunction() {
        return Models.Inventory.getInventorySlots().current();
    }

    @TemplateFunction(name = "ingredient_pouch_open_slots", aliases = { "pouch_open", "pouch_free" })
    public int ingredientPouchOpenSlotsFunction() {
        return Models.IngredientPouch.getIngredientPouchSlots().getRemaining();
    }

    @TemplateFunction(name = "ingredient_pouch_used_slots", aliases = { "pouch_used" })
    public int ingredientPouchUsedSlotsFunction() {
        return Models.IngredientPouch.getIngredientPouchSlots().current();
    }

    @TemplateFunction(name = "held_item_current_durability", aliases = { "current_held_durability" })
    public int heldItemCurrentDurabilityFunction() {
        ItemStack itemStack = InventoryUtils.getItemInHand();
        Optional<DurableItemProperty> durableItemOpt = Models.Item.asWynnItemProperty(itemStack, DurableItemProperty.class);
        if (durableItemOpt.isEmpty())
            return -1;
        return durableItemOpt.get().getDurability().current();
    }

    @TemplateFunction(name = "held_item_max_durability", aliases = { "max_held_durability" })
    public int heldItemMaxDurabilityFunction() {
        ItemStack itemStack = InventoryUtils.getItemInHand();
        Optional<DurableItemProperty> durableItemOpt = Models.Item.asWynnItemProperty(itemStack, DurableItemProperty.class);
        if (durableItemOpt.isEmpty())
            return -1;
        return durableItemOpt.get().getDurability().max();
    }

    @TemplateFunction(name = "held_item_shiny_stat")
    public NamedValue heldItemShinyStatFunction() {
        ItemStack itemStack = InventoryUtils.getItemInHand();
        Optional<ShinyStat> shinyStatOpt = Models.Shiny.getShinyStat(itemStack);
        if (shinyStatOpt.isEmpty())
            return NamedValue.EMPTY;
        // FIXME: The function system can't handle longs, so we have to cast to int
        return new NamedValue(shinyStatOpt.get().statType().displayName(), (int) shinyStatOpt.get().value());
    }

    @TemplateFunction(name = "held_item_type", aliases = { "held_type" })
    public String heldItemTypeFunction() {
        ItemStack itemInHand = InventoryUtils.getItemInHand();
        if (itemInHand.isEmpty()) {
            return "NONE";
        }
        Optional<WynnItem> wynnItem = Models.Item.getWynnItem(itemInHand);
        if (wynnItem.isEmpty()) {
            return "NONE";
        }
        return wynnItem.get().getClass().getSimpleName();
    }

    @TemplateFunction(name = "held_item_name", aliases = { "held_item", "held_name" })
    public String heldItemNameFunction(boolean formatted) {
        ItemStack itemStack = InventoryUtils.getItemInHand();
        StyledText hoverName = StyledText.fromComponent(itemStack.getHoverName());
        String itemName = formatted ? hoverName.getString() : hoverName.getString(StyleType.NONE);
        return WynnUtils.stripItemNameMarkers(itemName);
    }

    @TemplateFunction(name = "held_item_cooldown", aliases = { "held_cooldown", "held_cd" })
    public CappedValue heldItemCooldownFunction() {
        return Models.CharacterStats.getItemCooldownTicks();
    }

    @TemplateFunction(name = "teleport_scroll_charges", aliases = { "tp_scroll_charges" })
    public int teleportScrollChargesFunction() {
        return Models.TeleportScroll.getTeleportScrollCharges();
    }

    @TemplateFunction(name = "teleport_scroll_recharge_timer", aliases = { "tp_scroll_timer" })
    public int teleportScrollRechargeTimerFunction() {
        return Models.TeleportScroll.getTeleportScrollRechargeTimerSeconds();
    }

    @TemplateFunction(name = "item_count", aliases = { "item_amount" })
    public int itemCountFunction(String name) {
        return Models.Inventory.getAmountInInventory(name);
    }

    @TemplateFunction(name = "inventory_ingredients")
    public int inventoryIngredientsFunction(String name) {
        return Models.Inventory.getIngredientAmountInInventory(name);
    }

    @TemplateFunction(name = "ingredient_pouch_ingredients")
    public int ingredientPouchIngredientsFunction(String name) {
        return Models.IngredientPouch.getIngredientAmountInPouch(name);
    }

    @TemplateFunction(name = "material_count")
    public int materialCountFunction(boolean exact, String name, int tier) {
        return Models.Inventory.getMaterialsAmountInInventory(name, tier, exact);
    }
}
