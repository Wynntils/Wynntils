/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.managers;

import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import com.wynntils.wynn.utils.InventoryUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import net.minecraft.network.chat.Component;
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
    private static final String QUESTS_TITLE = "§0[Pg. 1] §8mag_icus'§0 Quests";
    private static final String QUESTS_TITLE_2 = "§0[Pg. 2] §8mag_icus'§0 Quests";
    private static final String QUESTS_TITLE_3 = "§0[Pg. 3] §8mag_icus'§0 Quests";
    private static Component actualTitle;
    private static Component lastTitle;
    private static MenuType menuType;

    @SubscribeEvent
    public static void onWorldChange(WorldStateEvent e) {
        if (e.getNewState() == WorldStateManager.State.WORLD) {
            // trigger reading at login
            openInventory();
        }
    }

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
        int clickedSlot = getSlotToClick(e.getItems(), actualTitle, menuType);
        String newTitleToLookFor = getTitleToLookFor(e.getItems(), actualTitle, menuType);

        if (clickedSlot != -1) {
            // Click on a slot; expect a new title
            lookForTitle = newTitleToLookFor;

            Int2ObjectMap<ItemStack> changedSlots = new Int2ObjectOpenHashMap<>();
            changedSlots.put(clickedSlot, new ItemStack(Items.AIR));

            int mouseButtonNum = 0;
            McUtils.sendPacket(new ServerboundContainerClickPacket(
                    containerId,
                    transactionId,
                    clickedSlot,
                    mouseButtonNum,
                    ClickType.PICKUP,
                    e.getItems().get(clickedSlot),
                    changedSlots));
            transactionId++;
        } else {
            // Done
            McUtils.sendPacket(new ServerboundContainerClosePacket(containerId));
        }

        e.setCanceled(true);
    }

    private static int getSlotToClick(List<ItemStack> items, Component title, MenuType menuType) {
        if (title.getString().equals(QUESTS_TITLE)) {
            ItemStack nextPage = items.get(8);
            if (nextPage.is(Items.GOLDEN_SHOVEL)) {
                String dispName = nextPage.getDisplayName().getString();
                if (dispName.equals("[§f§lPage 2§a >§2>§a>§2>§a>]")) {
                    return 8;
                }
            }
        } else if (title.getString().equals(QUESTS_TITLE_2)) {
            ItemStack nextPage = items.get(8);
            if (nextPage.is(Items.GOLDEN_SHOVEL)) {
                String dispName = nextPage.getDisplayName().getString();
                if (dispName.equals("[§f§lPage 3§a >§2>§a>§2>§a>]")) {
                    return 8;
                }
            }
        }

        return -1;
    }

    private static String getTitleToLookFor(List<ItemStack> items, Component title, MenuType menuType) {
        if (title.getString().equals(QUESTS_TITLE)) {
            ItemStack nextPage = items.get(8);
            if (nextPage.is(Items.GOLDEN_SHOVEL)) {
                String dispName = nextPage.getDisplayName().getString();
                if (dispName.equals("[§f§lPage 2§a >§2>§a>§2>§a>]")) {
                    return QUESTS_TITLE_2;
                }
            }
        } else if (title.getString().equals(QUESTS_TITLE_2)) {
            ItemStack nextPage = items.get(8);
            if (nextPage.is(Items.GOLDEN_SHOVEL)) {
                String dispName = nextPage.getDisplayName().getString();
                if (dispName.equals("[§f§lPage 3§a >§2>§a>§2>§a>]")) {
                    return QUESTS_TITLE_3;
                }
            }
        }

        return "";
    }

    private static void openInventory() {
        int id = McUtils.player().containerMenu.containerId;
        if (id != 0) {
            // another inventory is already open, cannot do this
            return;
        }
        lookForTitle = QUESTS_TITLE;
        int prevItem = McUtils.inventory().selected;
        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(InventoryUtils.QUEST_BOOK_SLOT_NUM));
        McUtils.sendPacket(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND));
        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(prevItem));
    }
}
