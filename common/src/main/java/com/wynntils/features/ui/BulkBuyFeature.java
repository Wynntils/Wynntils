/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.WynntilsMod;
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
import com.wynntils.mc.event.TickEvent;
import com.wynntils.screens.bulkbuy.widgets.BulkBuyWidget;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.UI)
public class BulkBuyFeature extends Feature {
    @Persisted
    public final Config<Integer> bulkBuyAmount = new Config<>(4);

    @Persisted
    public final Config<BulkBuySpeed> bulkBuySpeed = new Config<>(BulkBuySpeed.BALANCED);

    private static final String SHOP_TITLE_SUFFIX = " Shop";
    // Test in BulkBuyFeature_PRICE_PATTERN
    private static final Pattern PRICE_PATTERN = Pattern.compile("§6 - §(?:c✖|a✔) §f(\\d+)§7²");
    private static final ChatFormatting BULK_BUY_ACTIVE_COLOR = ChatFormatting.GREEN;
    private static final StyledText PRICE_STR = StyledText.fromString("§6Price:");

    private BulkBuyWidget bulkBuyWidget;
    private int bulkBoughtSlotNumber = -1; // Slot number of the thing we're buying
    private AbstractContainerMenu bulkBoughtContainer = null; // Shop container we're buying from
    private int bulkBoughtAmount = 0; // Amount remaining that we need to buy
    private int bulkBoughtPrice = 0; // Price of a single item

    @SubscribeEvent
    public void onShopOpened(SetSlotEvent.Pre e) {
        // Warning - for some reason this is triggered randomly while the shop is open as well
        if (e.getSlot() != 4 || e.getContainer().getContainerSize() != 54 || bulkBoughtSlotNumber != -1) return;
        // Shop titles are in slot 4, eg. §aScroll Shop
        // Shops are all size 54 for double chest, sometimes size 41 is sent (no idea what it's for)

        // Now we can do all the screen checks, since this event doesn't give us a way to access a screen
        if (!(McUtils.mc().screen instanceof ContainerScreen screen)) return;
        if (!(screen.getMenu() instanceof AbstractContainerMenu)) return;

        String title = e.getItemStack().getHoverName().getString();
        if (!title.startsWith(ChatFormatting.GREEN.toString()) || !title.endsWith(SHOP_TITLE_SUFFIX)) return;

        bulkBuyWidget = new BulkBuyWidget(
                screen.leftPos - Texture.BULK_BUY_PANEL.width(),
                screen.topPos - 5,
                Texture.BULK_BUY_PANEL.width(),
                Texture.BULK_BUY_PANEL.height());
        screen.addRenderableWidget(bulkBuyWidget);
    }

    @SubscribeEvent
    public void onSlotClicked(ContainerClickEvent e) {
        if (!KeyboardUtils.isShiftDown() || e.getClickType() != ClickType.QUICK_MOVE) return;

        AbstractContainerMenu container = e.getContainerMenu();
        ItemStack itemStack = e.getItemStack();
        if (!isBulkBuyable(container, itemStack)) return;

        int itemPrice = findItemPrice(LoreUtils.getLore(itemStack));
        if (itemPrice * (bulkBoughtAmount + bulkBuyAmount.get()) > Models.Emerald.getAmountInInventory()) {
            McUtils.sendErrorToClient(I18n.get("feature.wynntils.bulkBuy.bulkBuyCannotAfford"));
            return;
        }

        if (bulkBoughtSlotNumber == -1) {
            // we're starting a new bulk buy
            bulkBoughtSlotNumber = e.getSlotNum();
            bulkBoughtContainer = container;
            bulkBoughtAmount = bulkBuyAmount.get();
            bulkBoughtPrice = itemPrice;

            bulkBuyWidget.setBulkBoughtPrice(bulkBoughtPrice);
            bulkBuyWidget.setBulkBoughtItemStack(itemStack);
        } else if (bulkBoughtSlotNumber != e.getSlotNum()) {
            // we're trying to buy a different item
            McUtils.sendErrorToClient(I18n.get("feature.wynntils.bulkBuy.bulkBuyDifferentItem"));
            return;
        } else {
            // we're buying more of the same item
            bulkBoughtAmount += bulkBuyAmount.get();
        }
        bulkBuyWidget.setBulkBoughtAmount(bulkBoughtAmount);

        e.setCanceled(true);
    }

    @SubscribeEvent
    public void onShopClosed(ContainerCloseEvent.Pre e) {
        resetBulkBuy();
        bulkBuyWidget = null;
    }

    @SubscribeEvent
    public void onTickPurchase(TickEvent e) {
        if (bulkBoughtSlotNumber == -1) return;
        if (McUtils.mc().level.getGameTime() % bulkBuySpeed.get().getTicksDelay() != 0) return;

        ContainerUtils.clickOnSlot(
                bulkBoughtSlotNumber,
                bulkBoughtContainer.containerId,
                GLFW.GLFW_MOUSE_BUTTON_RIGHT,
                bulkBoughtContainer.getItems());
        --bulkBoughtAmount;

        if (bulkBoughtAmount <= 0) {
            resetBulkBuy();
        }
        bulkBuyWidget.setBulkBoughtAmount(bulkBoughtAmount);
    }

    private void resetBulkBuy() {
        bulkBoughtSlotNumber = -1;
        bulkBoughtContainer = null;
        bulkBoughtAmount = 0;
        bulkBoughtPrice = 0;

        bulkBuyWidget.setBulkBoughtAmount(bulkBoughtAmount);
        bulkBuyWidget.setBulkBoughtPrice(bulkBoughtPrice);
        bulkBuyWidget.setBulkBoughtItemStack(null);
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

    private int findItemPrice(List<StyledText> lore) {
        // Go backwards since prices are usually at the bottoms of the tooltips
        for (int i = lore.size() - 1; i >= 0; i--) {
            Matcher priceMatcher = lore.get(i).getMatcher(PRICE_PATTERN);
            if (priceMatcher.find()) {
                return Integer.parseInt(priceMatcher.group(1));
            }
        }

        WynntilsMod.warn("Bulk Buy could not find price for " + lore.getFirst().getString());
        return -1;
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

    public enum BulkBuySpeed {
        FAST(4),
        BALANCED(5),
        SAFE(6),
        VERY_SAFE(8);

        private final int ticksDelay;

        BulkBuySpeed(int ticksDelay) {
            this.ticksDelay = ticksDelay;
        }

        private int getTicksDelay() {
            return ticksDelay;
        }
    }
}
