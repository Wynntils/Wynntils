/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.screens.bulkbuy.widgets.BulkBuyWidget;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ConfigCategory(Category.UI)
public class BulkBuyFeature extends Feature {
    @Persisted
    public final Config<Integer> bulkBuyAmount = new Config<>(4);

    public final boolean continuePurchasingWithoutFunds = false;

    // Test in BulkBuyFeature_PRICE_PATTERN
    private static final Pattern PRICE_PATTERN = Pattern.compile("§6 - §(?:c✖|a✔) §f(\\d+)§7²");
    private static final ChatFormatting BULK_BUY_ACTIVE_COLOR = ChatFormatting.GREEN;
    private static final StyledText PRICE_STR = StyledText.fromString("§6Price:");
    private static final int TICKS_DELAY = 4;

    private final LinkedHashMap<Integer, BulkBoughtItem> bulkBuyQueue = new LinkedHashMap<>();

    @SubscribeEvent
    public void onShopOpened(SetSlotEvent.Pre e) {
        // Warning - for some reason this is triggered randomly while the shop is open as well
        if (e.getSlot() != 4 || e.getContainer().getContainerSize() != 54) return;
        // Shop titles are in slot 4, eg. §aScroll Shop
        // Shops are all size 54 for double chest, sometimes size 41 is sent (no idea what it's for)

        // Now we can do all the screen checks, since this event doesn't give us a way to access a screen
        if (!(McUtils.mc().screen instanceof ContainerScreen screen)) return;
        if (!(screen.getMenu() instanceof AbstractContainerMenu)) return;

        String title = e.getItemStack().getHoverName().getString();
        if (!title.startsWith(ChatFormatting.GREEN.toString()) || !title.endsWith(" Shop")) return;

        screen.addRenderableWidget(new BulkBuyWidget(screen.leftPos - 98, screen.topPos, 100, 110, bulkBuyQueue));
    }

    @SubscribeEvent
    public void onSlotClicked(ContainerClickEvent e) {
        if (!KeyboardUtils.isShiftDown()) return;

        AbstractContainerMenu container = e.getContainerMenu();
        ItemStack itemStack = e.getItemStack();
        if (!isBulkBuyable(container, itemStack)) return;

        if (e.getClickType() == ClickType.QUICK_MOVE) {
            if (!bulkBuyQueue.containsKey(e.getSlotNum())) {
                // This event returns the ItemStack in the slot *after* the click happens, which is usually air
                // So we need this external storage so we can get accurate ItemStack icons/names
                bulkBuyQueue.put(e.getSlotNum(), new BulkBoughtItem(e.getSlotNum(), itemStack, bulkBuyAmount.get(), 0));
            } else {
                bulkBuyQueue.get(e.getSlotNum()).incrementAmount();
            }
            // TODO get rid of sthis
            McUtils.sendMessageToClient(Component.literal("Slot number " + e.getSlotNum() + " " +
                    bulkBuyQueue.get(e.getSlotNum()).getItemStack().getHoverName().getString() + " has " +
                    bulkBuyQueue.get(e.getSlotNum()).getAmount() + " items queued"));
        }
        e.setCanceled(true);
    }

    @SubscribeEvent
    public void onShopClosed(ContainerCloseEvent.Pre e) {
        bulkBuyQueue.clear();
    }

    // This needs to be low so it runs after weapon tooltips are generated (for weapon merchants)
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onTooltipPre(ItemTooltipRenderEvent.Pre event) {
        if (!isBulkBuyable(McUtils.containerMenu(), event.getItemStack())) return;

        List<Component> tooltips = List.of(
                Component.literal(""), // Empty line
                Component.translatable("feature.wynntils.bulkBuy.bulkBuyNormal", bulkBuyAmount.get())
                        .withStyle(BULK_BUY_ACTIVE_COLOR),
                Component.translatable("feature.wynntils.bulkBuy.bulkBuyActive", bulkBuyAmount.get())
                        .withStyle(BULK_BUY_ACTIVE_COLOR));

        event.setTooltips(LoreUtils.appendTooltip(event.getItemStack(), replacePrices(event.getTooltips()), tooltips));
    }

    /**
     * When shift is pressed:
     * Replaces the price in the lore with the bulk buy price.
     * Also replaces the "✔" with a "✖" with a if the user can't afford the bulk buy.
     * @param oldLore Lore of the item that user wants to bulk buy
     * @return New lore with the above replacements
     */
    private List<Component> replacePrices(List<Component> oldLore) {
        if (!KeyboardUtils.isShiftDown()) return oldLore;

        List<Component> returnable = new ArrayList<>(oldLore);

        // iterate through lore to find the price, then replace it with the bulk buy price
        // there is no better way to do this since we cannot tell which line is the price (user may or may not have nbt
        // lines enabled/disabled)
        for (Component line : oldLore) {
            StyledText oldLine = StyledText.fromComponent(line);
            Matcher priceMatcher = oldLine.getMatcher(PRICE_PATTERN);
            if (!priceMatcher.find()) continue;

            int newPrice = Integer.parseInt(priceMatcher.group(1)) * bulkBuyAmount.get();
            StyledText newLine = StyledText.fromString(oldLine.getString())
                    .replaceFirst(priceMatcher.group(1), BULK_BUY_ACTIVE_COLOR + Integer.toString(newPrice));
            if (newPrice > Models.Emerald.getAmountInInventory()) {
                newLine = StyledText.fromString(
                        newLine.getString().replace("a✔", "c✖")); // Replace green checkmark with red x
            }
            returnable.set(returnable.indexOf(line), newLine.getComponent());
            break;
        }
        if (returnable == oldLore) {
            WynntilsMod.warn("Could not find price for " + oldLore.getFirst().getString());
        }
        return returnable;
    }

    private boolean isBulkBuyable(AbstractContainerMenu menu, ItemStack toBuy) {
        String title = menu.getSlot(4).getItem().getHoverName().getString();

        return title.startsWith(ChatFormatting.GREEN.toString())
                && title.endsWith(" Shop")
                && LoreUtils.getStringLore(toBuy).contains(PRICE_STR);
    }

    public final class BulkBoughtItem {
        private final int slotNumber;
        private final ItemStack itemStack;
        private int amount;
        private int price;

        private BulkBoughtItem(int slotNumber, ItemStack itemStack, int amount, int price) {
            this.slotNumber = slotNumber;
            this.itemStack = itemStack;
            this.amount = amount;
            this.price = price;
        }

        public int getSlotNumber() {
            return slotNumber;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public int getAmount() {
            return amount;
        }

        public void incrementAmount() {
            this.amount += bulkBuyAmount.get();
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }
    }
}
