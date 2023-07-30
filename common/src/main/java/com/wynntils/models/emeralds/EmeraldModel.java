/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.emeralds;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.ContainerCloseEvent;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class EmeraldModel extends Model {
    public static final int EMERALD_BLOCK_VALUE = 64;
    public static final int LIQUID_EMERALD_VALUE = 4096;
    public static final int LIQUID_EMERALD_STACK_VALUE = 262144;

    private static final Pattern STX_PATTERN = Pattern.compile("(\\.?\\d+\\.?\\d*)\\s*(s|stx|stacks)");
    private static final Pattern LE_PATTERN = Pattern.compile("(\\.?\\d+\\.?\\d*)\\s*(l|le)");
    private static final Pattern EB_PATTERN = Pattern.compile("(\\.?\\d+\\.?\\d*)\\s*(b|eb)");
    private static final Pattern K_PATTERN = Pattern.compile("(\\.?\\d+\\.?\\d*)\\s*(k|thousand)");
    private static final Pattern M_PATTERN = Pattern.compile("(\\.?\\d+\\.?\\d*)\\s*(m|million)");
    private static final Pattern E_PATTERN = Pattern.compile("(\\d+)($|\\s|\\s*e|\\s*em)(?![^\\d\\s-])");
    private static final Pattern RAW_PRICE_PATTERN = Pattern.compile("\\d+");
    public static final double TAX_AMOUNT = 1.05;

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
        return new int[] {
            emeralds % EMERALD_BLOCK_VALUE,
            (emeralds / EMERALD_BLOCK_VALUE) % 64,
            (emeralds / LIQUID_EMERALD_VALUE) % 64,
            emeralds / LIQUID_EMERALD_STACK_VALUE
        };
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
                emeralds += (long) (Double.parseDouble(stxMatcher.group(1)) * LIQUID_EMERALD_STACK_VALUE);
            }

            // le
            Matcher leMatcher = LE_PATTERN.matcher(input);
            while (leMatcher.find()) {
                emeralds += (long) (Double.parseDouble(leMatcher.group(1)) * LIQUID_EMERALD_VALUE);
            }

            // eb
            Matcher ebMatcher = EB_PATTERN.matcher(input);
            while (ebMatcher.find()) {
                emeralds += (long) (Double.parseDouble(ebMatcher.group(1)) * EMERALD_BLOCK_VALUE);
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
        Inventory inventory = McUtils.inventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            adjustBalance(null, inventory.getItem(i), true);
        }
    }

    @SubscribeEvent
    public void onSetSlot(SetSlotEvent.Post event) {
        boolean isInventory = event.getContainer() == McUtils.inventory();
        if (pouchContainerId != -1 && !isInventory) return;

        // FIXME: This is a hack to always have up-to-date emerald counts
        //        When Wynncraft fixes emerald stacking,
        //        this can be simplified greatly (by using old and new stacks)
        //        However, this is really fast so maybe we can keep it (pending profiling)
        if (isInventory) {
            inventoryEmeralds = 0;

            // Rescan inventory after merging items
            List<ItemStack> items = McUtils.inventoryMenu().getItems();
            for (ItemStack item : items) {
                adjustBalance(null, item, true);
            }
        } else if (event.getContainer() == McUtils.containerMenu()) {
            containerEmeralds = 0;

            // Rescan container after merging items
            List<ItemStack> items = McUtils.containerMenu().getItems();
            for (ItemStack item : items) {
                adjustBalance(null, item, false);
            }
        }
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

    @SubscribeEvent
    public void onContainerClose(ContainerCloseEvent.Post event) {
        containerEmeralds = 0;
    }

    private void adjustBalance(ItemStack oldItemStack, ItemStack newItemStack, boolean isInventory) {
        int adjustValue = 0;
        Optional<EmeraldValuedItemProperty> oldItemValueOpt =
                Models.Item.asWynnItemPropery(oldItemStack, EmeraldValuedItemProperty.class);
        if (oldItemValueOpt.isPresent()) {
            adjustValue -= oldItemValueOpt.get().getEmeraldValue();
        }

        Optional<EmeraldValuedItemProperty> newItemValueOpt =
                Models.Item.asWynnItemPropery(newItemStack, EmeraldValuedItemProperty.class);
        if (newItemValueOpt.isPresent()) {
            adjustValue += newItemValueOpt.get().getEmeraldValue();
        }

        // We most likely replaced the same item, so we don't need to adjust
        if (adjustValue == 0) return;

        if (isInventory) {
            inventoryEmeralds = Math.max(0, inventoryEmeralds + adjustValue);
        } else {
            containerEmeralds = Math.max(0, containerEmeralds + adjustValue);
        }
    }
}
