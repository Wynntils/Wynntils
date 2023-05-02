/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.emeralds;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.models.items.ItemModel;
import com.wynntils.models.items.items.game.EmeraldPouchItem;
import com.wynntils.models.items.properties.EmeraldValuedItemProperty;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class EmeraldModel extends Model {
    private static final Pattern STX_PATTERN = Pattern.compile("(\\.?\\d+\\.?\\d*)\\s*(s|stx|stacks)");
    private static final Pattern LE_PATTERN = Pattern.compile("(\\.?\\d+\\.?\\d*)\\s*(l|le)");
    private static final Pattern EB_PATTERN = Pattern.compile("(\\.?\\d+\\.?\\d*)\\s*(b|eb)");
    private static final Pattern K_PATTERN = Pattern.compile("(\\.?\\d+\\.?\\d*)\\s*(k|thousand)");
    private static final Pattern M_PATTERN = Pattern.compile("(\\.?\\d+\\.?\\d*)\\s*(m|million)");
    private static final Pattern E_PATTERN = Pattern.compile("(\\d+)($|\\s|\\s*e|\\s*em)(?![^\\d\\s-])");
    private static final Pattern RAW_PRICE_PATTERN = Pattern.compile("\\d+");
    private static final int STACK_SIZE = 64;
    private static final double TAX_AMOUNT = 1.05;

    private int inventoryEmeralds = 0;
    private int containerEmeralds = 0;
    private int pouchContainerId = -1;

    public EmeraldModel(ItemModel itemModel) {
        super(List.of(itemModel));
    }

    public String getFormattedString(int emeralds, boolean appendZeros) {
        StringBuilder builder = new StringBuilder();

        int[] emeraldAmounts = emeraldsPerUnit(emeralds);

        for (int i = emeraldAmounts.length - 1; i >= 0; i--) {
            if (emeraldAmounts[i] == 0 && !appendZeros) continue;

            builder.append(emeraldAmounts[i])
                    .append(EmeraldUnits.values()[i].getSymbol())
                    .append(" ");
        }

        return builder.toString().trim();
    }

    public int[] emeraldsPerUnit(int emeralds) {
        return new int[] {emeralds % 64, (emeralds / 64) % 64, emeralds / 4096};
    }

    public boolean isEmeraldPouch(ItemStack itemStack) {
        Optional<EmeraldPouchItem> itemOpt = Models.Item.asWynnItem(itemStack, EmeraldPouchItem.class);
        return itemOpt.isPresent();
    }

    public int getAmountInInventory() {
        return inventoryEmeralds;
    }

    public int getAmountInContainer() {
        return containerEmeralds;
    }

    public String convertEmeraldPrice(String inputStr) {
        Matcher rawMatcher = RAW_PRICE_PATTERN.matcher(inputStr);
        if (rawMatcher.matches()) return "";

        String input = inputStr.toLowerCase(Locale.ROOT);
        long emeralds = 0;

        try {
            // stx
            Matcher stxMatcher = STX_PATTERN.matcher(input);
            while (stxMatcher.find()) {
                emeralds += (long) (Double.parseDouble(stxMatcher.group(1)) * STACK_SIZE * STACK_SIZE * STACK_SIZE);
            }

            // le
            Matcher leMatcher = LE_PATTERN.matcher(input);
            while (leMatcher.find()) {
                emeralds += (long) (Double.parseDouble(leMatcher.group(1)) * STACK_SIZE * STACK_SIZE);
            }

            // eb
            Matcher ebMatcher = EB_PATTERN.matcher(input);
            while (ebMatcher.find()) {
                emeralds += (long) (Double.parseDouble(ebMatcher.group(1)) * STACK_SIZE);
            }
            // k
            Matcher kMatcher = K_PATTERN.matcher(input);
            while (kMatcher.find()) {
                emeralds += (long) (Double.parseDouble(kMatcher.group(1)) * 1000);
            }

            // m
            Matcher mMatcher = M_PATTERN.matcher(input);
            while (mMatcher.find()) {
                emeralds += (long) (Double.parseDouble(mMatcher.group(1)) * 1000000);
            }

            // standard numbers/emeralds
            Matcher eMatcher = E_PATTERN.matcher(input);
            while (eMatcher.find()) {
                emeralds += Long.parseLong(eMatcher.group(1));
            }

            // account for tax if flagged
            if (input.contains("-t")) {
                emeralds = Math.round(emeralds / TAX_AMOUNT);
            }
        } catch (NumberFormatException e) {
            return "";
        }

        return (emeralds > 0) ? String.valueOf(emeralds) : "";
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent e) {
        if (e.getNewState() != WorldState.WORLD) return;

        inventoryEmeralds = 0;
        containerEmeralds = 0;

        // Rescan inventory at login
        Inventory inventory = McUtils.player().getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            adjustBalance(inventory.getItem(i), 1, true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onSetSlot(SetSlotEvent.Pre event) {
        boolean isInventory = (event.getContainer() == McUtils.player().getInventory());
        if (pouchContainerId != -1 && !isInventory) return;

        // Subtract the outgoing object from our balance
        adjustBalance(event.getContainer().getItem(event.getSlot()), -1, isInventory);
        // And add the incoming value
        adjustBalance(event.getItemStack(), 1, isInventory);
    }

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent e) {
        String title = WynnUtils.normalizeBadString(e.getTitle().getString());
        if (title.equals("Emerald Pouch")) {
            pouchContainerId = e.getContainerId();
        } else {
            pouchContainerId = -1;
        }
    }

    @SubscribeEvent
    public void onMenuClosed(MenuEvent.MenuClosedEvent e) {
        containerEmeralds = 0;
        pouchContainerId = -1;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onContainerSetContent(ContainerSetContentEvent.Pre event) {
        int containerStop;

        inventoryEmeralds = 0;
        containerEmeralds = 0;

        List<ItemStack> items = event.getItems();
        if (event.getContainerId() == 0) {
            containerStop = 0;
        } else if (event.getContainerId() == McUtils.player().containerMenu.containerId) {
            containerStop = items.size() - 36;
        } else {
            return;
        }

        for (int i = 0; i < containerStop; i++) {
            adjustBalance(items.get(i), 1, false);
        }
        for (int i = containerStop; i < items.size(); i++) {
            adjustBalance(items.get(i), 1, true);
        }
        if (event.getContainerId() == pouchContainerId) {
            // Don't count the emeralds in the pouch container twice, it's actually still
            // just the inventory
            containerEmeralds = 0;
        }
    }

    private void adjustBalance(ItemStack itemStack, int multiplier, boolean isInventory) {
        Optional<EmeraldValuedItemProperty> valuedItemOpt =
                Models.Item.asWynnItemPropery(itemStack, EmeraldValuedItemProperty.class);
        if (valuedItemOpt.isEmpty()) return;

        int adjustValue = valuedItemOpt.get().getEmeraldValue() * multiplier;
        if (isInventory) {
            inventoryEmeralds += adjustValue;
        } else {
            containerEmeralds += adjustValue;
        }
    }
}
