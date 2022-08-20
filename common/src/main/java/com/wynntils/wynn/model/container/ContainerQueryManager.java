/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.container;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ContainerQueryManager extends CoreManager {
    private static final int NO_CONTAINER = -2;

    private static ContainerQueryStep currentStep;

    private static Component currentTitle;
    private static MenuType currentMenuType;
    private static int containerId = NO_CONTAINER;
    private static int lastHandledContentId = NO_CONTAINER;

    public static void runQuery(ContainerQueryStep firstStep) {
        currentStep = firstStep;
        if (!firstStep.startStep(null)) {
            raiseError("Cannot execute first step");
        }
    }

    @SubscribeEvent
    public static void onMenuOpened(MenuEvent.MenuOpenedEvent e) {
        if (currentStep == null) return;

        boolean matches = currentStep.verifyContainer(e.getTitle(), e.getMenuType());
        if (matches) {
            containerId = e.getContainerId();
            currentTitle = e.getTitle();
            currentMenuType = e.getMenuType();
            e.setCanceled(true);
        } else {
            raiseError("Unexpected container opened");
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

        if (containerId == NO_CONTAINER) {
            // We have not registered a MenuOpenedEvent
            raiseError("Container contents without associated container open");
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
            return;
        }

        lastHandledContentId = containerId;
        ContainerContent currentContainer =
                new ContainerContent(e.getItems(), currentTitle, currentMenuType, containerId);

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
        }

        e.setCanceled(true);
    }

    private static void raiseError(String errorMsg) {
        if (currentStep != null) {
            WynntilsMod.error("Internal error in ContainerQueryManager: handleError called with no currentStep");
            return;
        }
        currentStep.onError(errorMsg);
        endQuery();
    }

    private static void endQuery() {
        containerId = NO_CONTAINER;
        lastHandledContentId = NO_CONTAINER;
        currentStep = null;
    }
}
