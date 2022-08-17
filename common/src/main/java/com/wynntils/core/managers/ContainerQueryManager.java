/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.managers;

import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.utils.McUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ContainerQueryManager extends CoreManager {
    private static int containerId = -2;
    private static int transactionId = 0;

    private static String lookForTitle;
    private static Component actualTitle;
    private static Component lastTitle;
    private static MenuType menuType;

    public static ContainerAction nextAction;

    @SubscribeEvent
    public static void onMenuOpened(MenuEvent.MenuOpenedEvent e) {
        String title = e.getTitle().getString();
        if (lookForTitle == null) return;

        if (title.equals(lookForTitle)) {
            containerId = e.getContainerId();
            actualTitle = e.getTitle();
            menuType = e.getMenuType();
            transactionId = 0;
            lookForTitle = null;
            e.setCanceled(true);
        } else {
            // We got another container than the expected. Report failure.
        }
    }

    @SubscribeEvent
    public static void onMenuForcefullyClosed(MenuEvent.MenuClosedEvent e) {
        // Server closed our container window. This should not happen
        // but if it do, report failure
    }

    @SubscribeEvent
    public static void onContainerSetContent(ContainerSetContentEvent e) {
        int id = e.getContainerId();
        if (id != containerId) return;

        if (actualTitle.equals(lastTitle)) {
            e.setCanceled(true);
            return;
        }
        lastTitle = actualTitle;

        // FIXME: Better API here
        System.out.println("*** From " + actualTitle.getString() + " got " + e.getItems());
        String newTitleToLookFor = nextAction.processContainer(e.getItems(), actualTitle, menuType);

        if (newTitleToLookFor != null) {
            lookForTitle = newTitleToLookFor;
        } else {
            // Done
            McUtils.sendPacket(new ServerboundContainerClosePacket(containerId));
        }

        e.setCanceled(true);
    }

    public static void clickOnSlot(List<ItemStack> items, int clickedSlot) {
        Int2ObjectMap<ItemStack> changedSlots = new Int2ObjectOpenHashMap<>();
        changedSlots.put(clickedSlot, new ItemStack(Items.AIR));

        int mouseButtonNum = 0;
        McUtils.sendPacket(new ServerboundContainerClickPacket(
                containerId,
                transactionId,
                clickedSlot,
                mouseButtonNum,
                ClickType.PICKUP,
                items.get(clickedSlot),
                changedSlots));
        transactionId++;
    }

    public static void start(ContainerAction action) {
        String firstExpectedString = action.processContainer(List.of(), TextComponent.EMPTY, null);
        lookForTitle = firstExpectedString;
    }

    public static void openInventory(int slotNum) {
        int id = McUtils.player().containerMenu.containerId;
        if (id != 0) {
            // another inventory is already open, cannot do this
            return;
        }
        int prevItem = McUtils.inventory().selected;
        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(slotNum));
        McUtils.sendPacket(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND));
        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(prevItem));
    }

    @FunctionalInterface
    public interface ContainerAction {
        /** Return next title to expect, or null to quit */
        String processContainer(List<ItemStack> items, Component title, MenuType menuType);
    }

    @FunctionalInterface
    public interface ContainerAction2 {
        /** Return next title to expect, or null to quit */
        void processContainer(List<ItemStack> items, Component title, MenuType menuType);
    }

    public static class ContainerQueryBuilder {
        public static ContainerQueryBuilder start() {
            return new ContainerQueryBuilder();
        }

        public ContainerQueryBuilder expectTitle(String expectedTitle) {
            // FIXME: append to builder
            return this;
        }

        public ContainerQueryBuilder processContainer(ContainerAction2 action) {
            // FIXME: append to builder
            return this;
        }

        public ContainerQueryBuilder clickOnSlot(int clickedSlot) {
            // FIXME: append to builder
            return this;
        }

        public ContainerQueryBuilder useItemInHotbar(int slotNum) {
            // FIXME: append to builder
            return this;
        }

        public ContainerQueryBuilder openInventory(int slotNum) {
            // FIXME: append to builder
            return this;
        }

        public ContainerQuery build() {
            // FIXME: do build query
            return new ContainerQuery(this);
        }
    }

    public static class ContainerQuery {
        protected ContainerQuery(ContainerQueryBuilder builder) {}

        public void executeQuery() {
            // FIXME: do execute query
        }
    }
}
