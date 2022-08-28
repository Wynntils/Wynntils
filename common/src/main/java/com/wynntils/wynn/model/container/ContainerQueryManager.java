/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.container;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.mc.event.ClientTickEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.utils.McUtils;
import java.util.LinkedList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ContainerQueryManager extends CoreManager {
    private static final int NO_CONTAINER = -2;
    private static final LinkedList<ContainerQueryStep> queuedQueries = new LinkedList<>();
    public static final int OPERATION_TIMEOUT_TICKS = 30; // normal operation is ~10 ticks

    private static ContainerQueryStep currentStep;
    private static String firstStepName;
    private static ContainerQueryStep lastStep;

    private static Component currentTitle;
    private static MenuType currentMenuType;
    private static int containerId = NO_CONTAINER;
    private static int lastHandledContentId = NO_CONTAINER;
    private static int ticksRemaining;

    public static void init() {}

    public static void runQuery(ContainerQueryStep firstStep) {
        if (currentStep != null) {
            // Only add if it is not already enqueued
            if (queuedQueries.stream()
                    .filter(query -> query.getName().equals(firstStepName))
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

        if (McUtils.player().containerMenu.containerId != 0) {
            // For safety, check this way too
            firstStep.onError("Another container is already open");
            return;
        }

        currentStep = firstStep;
        firstStepName = firstStep.getName();
        lastStep = null;
        resetTimer();
        if (!firstStep.startStep(null)) {
            raiseError("Cannot execute first step");
        }
    }

    @SubscribeEvent
    public static void onMenuOpened(MenuEvent.MenuOpenedEvent e) {
        if (currentStep == null && lastStep == null) return;
        if (currentStep == null && lastStep != null) {
            // We're in a possibly bad state. We have failed a previous call, but
            // we might still get the menu opened (perhaps after a lag spike).
            boolean matches = lastStep.verifyContainer(e.getTitle(), e.getMenuType());
            if (matches) {
                WynntilsMod.warn("Closing container '" + e.getTitle().getString()
                        + "' due to previously aborted container query");
                lastStep = null;
                e.setCanceled(true);
            } else {
                lastStep = null;
            }
            return;
        }

        boolean matches = currentStep.verifyContainer(e.getTitle(), e.getMenuType());
        if (matches) {
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
    public static void onMenuForcefullyClosed(MenuEvent.MenuClosedEvent e) {
        if (currentStep == null) return;

        // Server closed our container window. This should not happen
        // but if it do, report failure
        raiseError("Server closed container");
    }

    @SubscribeEvent
    public static void onContainerSetContent(ContainerSetContentEvent e) {
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

        if (containerId == lastHandledContentId) {
            // Wynncraft sometimes sends contents twice; just drop this silently
            e.setCanceled(true);
            resetTimer();
            return;
        }

        lastHandledContentId = containerId;
        ContainerContent currentContainer =
                new ContainerContent(e.getItems(), currentTitle, currentMenuType, containerId);
        resetTimer();

        // Now actually process this container
        currentStep.handleContent(currentContainer);

        ContainerQueryStep nextStep = currentStep.getNextStep(currentContainer);
        if (nextStep != null) {
            // Go on and query another container
            currentStep = nextStep;
            if (!currentStep.startStep(currentContainer)) {
                raiseError("Cannot execute chained start step");
            }
        } else {
            // We're done
            endQuery();
            McUtils.sendPacket(new ServerboundContainerClosePacket(id));
            if (!queuedQueries.isEmpty()) {
                runQuery(queuedQueries.pop());
            }
        }

        e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Start event) {
        if (currentStep == null) return;

        ticksRemaining--;

        if (ticksRemaining <= 0) {
            raiseError("Container reply timed out");
        }
    }

    private static void raiseError(String errorMsg) {
        if (currentStep == null) {
            WynntilsMod.error("Internal error in ContainerQueryManager: handleError called with no currentStep");
            return;
        }
        currentStep.onError(errorMsg);
        lastStep = currentStep;
        endQuery();
    }

    private static void endQuery() {
        containerId = NO_CONTAINER;
        lastHandledContentId = NO_CONTAINER;
        currentStep = null;
    }

    private static void resetTimer() {
        ticksRemaining = OPERATION_TIMEOUT_TICKS;
    }
}
