/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.container;

import com.wynntils.core.managers.CoreManager;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.utils.McUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.function.Consumer;
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
    private static final int NO_CONTAINER = -2;
    private static int containerId = NO_CONTAINER;
    private static int lastHandledContentId = NO_CONTAINER;
    private static int transactionId = 0;
    private static Component currentTitle;
    private static MenuType currentMenuType;

    private static ContainerQueryStep currentStep;

    private static Consumer<String> errorHandler;

    @SubscribeEvent
    public static void onMenuOpened(MenuEvent.MenuOpenedEvent e) {
        if (currentStep == null) return;

        boolean matches = currentStep.verifyContainer(e.getTitle(), e.getMenuType());
        if (matches) {
            containerId = e.getContainerId();
            currentTitle = e.getTitle();
            currentMenuType = e.getMenuType();
            transactionId = 0;
            e.setCanceled(true);
        } else {
            handleError("Unexpected container opened");
        }
    }

    @SubscribeEvent
    public static void onMenuForcefullyClosed(MenuEvent.MenuClosedEvent e) {
        if (currentStep == null) return;

        // Server closed our container window. This should not happen
        // but if it do, report failure
        handleError("Server closed container");
    }

    @SubscribeEvent
    public static void onContainerSetContent(ContainerSetContentEvent e) {
        if (currentStep == null) return;

        if (containerId == NO_CONTAINER) {
            // We have not registered a MenuOpenedEvent
            handleError("Container contents without associated container open");
            return;
        }

        int id = e.getContainerId();
        if (id != containerId) {
            handleError("Another container opened");
            return;
        }

        if (containerId == lastHandledContentId) {
            // Wynncraft sometimes sends contents twice; drop this
            e.setCanceled(true);
            return;
        }

        lastHandledContentId = containerId;
        // Callback to handle this container
        ContainerContent currentContainer = new ContainerContent(e.getItems(), currentTitle, currentMenuType);
        currentStep.handleContent(currentContainer);

        ContainerQueryStep nextStep = currentStep.getNextStep(currentContainer);

        if (nextStep != null) {
            currentStep = nextStep;
            currentStep.startStep(currentContainer);
        } else {
            // We're done
            currentStep = null;
            containerId = NO_CONTAINER;
            lastHandledContentId = NO_CONTAINER;
            McUtils.sendPacket(new ServerboundContainerClosePacket(id));
        }

        e.setCanceled(true);
    }

    private static void handleError(String errorMsg) {
        System.out.println("error: " + errorMsg);
        errorHandler.accept(errorMsg);
        containerId = NO_CONTAINER;
        lastHandledContentId = NO_CONTAINER;
        currentStep = null;
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

    public static void runQuery(ContainerQueryStep firstStep) {
        currentStep = firstStep;
        firstStep.startStep(null);
    }
}
