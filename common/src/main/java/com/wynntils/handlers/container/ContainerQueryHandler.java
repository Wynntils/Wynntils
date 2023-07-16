/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.container;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.LocalSoundEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ContainerQueryHandler extends Handler {
    private static final int NO_CONTAINER = -2;
    private static final int OPERATION_TIMEOUT_TICKS = 60; // normal operation is ~10 ticks
    private static final String MENU_CLICK_SOUND = "minecraft.block.wooden_pressure_plate.click_on";

    private final LinkedList<ContainerQueryStep> queuedQueries = new LinkedList<>();

    private ContainerQueryStep currentStep;
    private String firstStepName;

    private Component currentTitle;
    private MenuType<?> currentMenuType;
    private int containerId = NO_CONTAINER;
    private List<ItemStack> containerItems = new ArrayList<>();
    private ContainerQueryStep lastVerifiedStep;
    private ContainerQueryStep lastStepWithContentSet;

    private int lastHandledContentId = NO_CONTAINER;
    private List<ItemStack> lastHandledItems = List.of();

    private int ticksRemaining;

    public void runQuery(ContainerQueryStep firstStep) {
        if (currentStep != null) {
            // Only add if it is not already enqueued
            if (queuedQueries.stream()
                    .filter(query -> query.getName().equals(firstStep.getName()))
                    .findAny()
                    .isEmpty()) {
                queuedQueries.add(firstStep);
            }
            return;
        }

        Screen screen = McUtils.mc().screen;
        if (screen instanceof AbstractContainerScreen) {
            // Another inventory screen is already open, cannot do this
            firstStep.onError("Another container screen is already open");
            return;
        }

        if (McUtils.containerMenu().containerId != 0) {
            // For safety, check this way too
            firstStep.onError("Another container is already open");
            return;
        }

        currentStep = firstStep;
        firstStepName = firstStep.getName();
        resetTimer();
        try {
            if (!firstStep.startStep(null)) {
                endQuery();
            }
        } catch (ContainerQueryException e) {
            raiseError("Cannot execute first step: " + e.getMessage());
        }
    }

    @SubscribeEvent
    public void onSound(LocalSoundEvent.Client e) {
        // Silence the menu click sound when we are processing query
        if (currentStep == null) return;
        if (e.getSource() != SoundSource.BLOCKS) return;
        if (!e.getSound().getLocation().toLanguageKey().equals(MENU_CLICK_SOUND)) return;

        e.setCanceled(true);
        return;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (currentStep == null) return;

        ticksRemaining--;

        if (ticksRemaining <= 0) {
            raiseError("Container reply timed out");
        }
    }

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent e) {
        // Are we processing a query?
        if (currentStep == null) return;

        if (currentStep.verifyContainer(e.getTitle(), e.getMenuType())) {
            containerId = e.getContainerId();
            currentTitle = e.getTitle();
            currentMenuType = e.getMenuType();
            resetTimer();
            e.setCanceled(true);
        } else {
            raiseError("Unexpected container opened: '" + e.getTitle().getString() + "'");
        }
    }

    @SubscribeEvent
    public void onMenuForcefullyClosed(MenuEvent.MenuClosedEvent e) {
        if (currentStep == null) return;

        // Server closed our container window. This should not happen
        // but if it do, report failure
        if (e.getContainerId() == containerId) {
            raiseError("Server closed container");
        } else {
            WynntilsMod.warn("Server closed container " + e.getContainerId() + " but we are querying " + containerId);
        }
    }

    @SubscribeEvent
    public void onContainerSetContent(ContainerSetContentEvent.Pre e) {
        if (currentStep == null) return;
        // We got an inventory update, can happen all the time
        if (e.getContainerId() == 0) return;

        if (containerId == NO_CONTAINER) {
            // We have not registered a MenuOpenedEvent. Assume this means that this is the
            // content of another container, so just pass it on
            return;
        }

        int id = e.getContainerId();
        if (id != containerId) {
            raiseError("Another container opened");
            return;
        }

        if (containerId == lastHandledContentId && InventoryUtils.isItemListsEqual(e.getItems(), lastHandledItems)) {
            // After opening a new container, Wynncraft sometimes sends contents twice. Ignore this.
            e.setCanceled(true);
            resetTimer();
            return;
        }

        if (lastStepWithContentSet == currentStep) {
            // This is a bad edge case. If this happens, it means the server sent 2 set content packets, but with
            // different content.
            WynntilsMod.warn("Container " + containerId + " sent set content after being verified, ignoring.");
            resetTimer();
            return;
        }

        lastHandledContentId = containerId;
        lastHandledItems = e.getItems();
        containerItems = e.getItems();
        lastStepWithContentSet = currentStep;

        ContainerContent currentContainer =
                new ContainerContent(containerItems, currentTitle, currentMenuType, containerId);
        resetTimer();

        try {
            // Verify that the content update is what we expect
            Int2ObjectArrayMap<ItemStack> changes = new Int2ObjectArrayMap<>();
            for (ItemStack itemStack : e.getItems()) {
                changes.put(changes.size(), itemStack);
            }

            if (lastVerifiedStep == currentStep || currentStep.verifyContentUpdate(currentContainer, changes)) {
                // Now we can process the content
                processContainer(currentContainer);
            }
            // or else, we just wait for the next updates using set slot

            e.setCanceled(true);
        } catch (ContainerQueryException ex) {
            raiseError("Error while processing content for " + firstStepName + ": " + ex.getMessage());
        }
    }

    @SubscribeEvent
    public void onContainerSetSlot(ContainerSetSlotEvent.Pre event) {
        if (currentStep == null) return;
        // We got an inventory update, can happen all the time
        if (event.getContainerId() == 0 || event.getContainerId() == -1) return;

        // We already verified this step, ignore
        if (lastVerifiedStep == currentStep) {
            return;
        }

        if (containerId == NO_CONTAINER) {
            // We have not registered a MenuOpenedEvent. Assume this means that this is the
            // content of another container, so just pass it on
            return;
        }

        int id = event.getContainerId();
        if (id != containerId) {
            raiseError("Another container opened in set slot");
            return;
        }

        resetTimer();

        event.setCanceled(true);

        if (containerItems.size() < event.getSlot()) {
            // This couldn't possibly happen, we don't set contentUpdateVerified until we have the full content set
            // packet at least once
            WynntilsMod.warn("Container sent set slot for slot " + event.getSlot() + " but we only have "
                    + containerItems.size() + " slots");
            return;
        }

        // Note: We purposefully don't update the containerItems here, because we just want to verify the content update
        //        containerItems.set(event.getSlot(), event.getItemStack());

        ContainerContent currentContainer =
                new ContainerContent(containerItems, currentTitle, currentMenuType, containerId);

        try {
            // Verify that the content update is what we expect
            Int2ObjectArrayMap<ItemStack> changes = new Int2ObjectArrayMap<>();
            changes.put(event.getSlot(), event.getItemStack());
            if (currentStep.verifyContentUpdate(currentContainer, changes)) {
                // Now we can process the content
                lastVerifiedStep = currentStep;

                if (lastStepWithContentSet == currentStep) {
                    processContainer(currentContainer);
                }
            }
        } catch (ContainerQueryException ex) {
            raiseError("Error while processing set slot for " + firstStepName + ": " + ex.getMessage());
        }
    }

    private void processContainer(ContainerContent currentContainer) throws ContainerQueryException {
        currentStep.handleContent(currentContainer);

        ContainerQueryStep nextStep = currentStep.getNextStep(currentContainer);

        if (nextStep != null) {
            // Go on and query another container
            currentStep = nextStep;
            // Return true iff taking the next step succeeded
            lastVerifiedStep = null;
            lastStepWithContentSet = null;
            if (currentStep.startStep(currentContainer)) return;
        }

        // We're done
        endQuery();
        McUtils.sendPacket(new ServerboundContainerClosePacket(containerId));
        // Start next query in queue, if any
        if (!queuedQueries.isEmpty()) {
            runQuery(queuedQueries.pop());
        }
    }

    private void raiseError(String errorMsg) {
        if (currentStep == null) {
            WynntilsMod.error("Internal error in ContainerQueryManager: handleError called with no currentStep");
            return;
        }
        currentStep.onError(errorMsg);
        endQuery();
    }

    private void endQuery() {
        containerId = NO_CONTAINER;
        lastHandledContentId = NO_CONTAINER;
        containerItems = List.of();
        lastVerifiedStep = null;
        lastStepWithContentSet = null;
        lastHandledItems = List.of();
        currentStep = null;
    }

    private void resetTimer() {
        ticksRemaining = OPERATION_TIMEOUT_TICKS;
    }
}
