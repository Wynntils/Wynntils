/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.models.containers.type.SearchableContainerType;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.List;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public final class ContainerQuickJumpModel extends Model {
    private static final int NEXT_PAGE_SLOT = 8;
    private static final int PREVIOUS_PAGE_SLOT = 17;
    public static final List<Integer> BUTTON_SLOTS = List.of(7, 16, 25, 34, 43, 52);
    public static final List<Integer> QUICK_JUMP_DESTINATIONS = List.of(1, 5, 9, 13, 17, 21);

    public ContainerQuickJumpModel(ContainerModel containerModel) {
        super(List.of(containerModel));
    }

    private int pageDestination = -1;

    @SubscribeEvent
    public void onContainerInit(ScreenInitEvent event) {
        navigateCloser();
    }

    @SubscribeEvent
    public void onContainerClose(ScreenClosedEvent event) {
        pageDestination = -1;
    }

    private void navigateCloser() {
        if (!validPage(pageDestination)) {
            pageDestination = -1;
            return;
        }

        int currentPage = Models.Container.getCurrentBankPage(McUtils.mc().screen);

        int pageDifference = pageDestination - currentPage;

        if (pageDifference == 0) {
            pageDestination = -1;
            return;
        }

        if (Math.abs(pageDifference) > 1 && jumpCloser(pageDifference)) return;

        ContainerUtils.clickOnSlot(
                pageDifference > 0 ? NEXT_PAGE_SLOT : PREVIOUS_PAGE_SLOT,
                McUtils.containerMenu().containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                McUtils.containerMenu().getItems());
    }

    private boolean jumpCloser(int pageDifference) {
        int closestDist = QUICK_JUMP_DESTINATIONS.stream()
                .filter(this::validPage)
                .mapToInt(i -> Math.abs(i - pageDestination))
                .min()
                .orElse(Integer.MAX_VALUE);

        if (closestDist < Math.abs(pageDifference)) {
            int index = QUICK_JUMP_DESTINATIONS.indexOf(closestDist + pageDestination);
            ContainerUtils.clickOnSlot(
                    BUTTON_SLOTS.get(
                            index == -1 ? QUICK_JUMP_DESTINATIONS.indexOf(-closestDist + pageDestination) : index),
                    McUtils.containerMenu().containerId,
                    GLFW.GLFW_MOUSE_BUTTON_LEFT,
                    McUtils.containerMenu().getItems());

            return true;
        }
        return false;
    }

    private boolean validPage(int page) {
        if (page <= 0) return false;
        SearchableContainerType containerType = Models.Container.getContainerType();
        if (containerType == null) return false;
        return page <= Models.Container.getFinalPage(containerType);
    }

    public void jumpToPage(int page) {
        if (!validPage(page)) return;
        pageDestination = page;
        navigateCloser();
    }
}
