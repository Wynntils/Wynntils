/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type;

import com.wynntils.core.components.Model;
import com.wynntils.models.containers.ContainerModel;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.List;
import org.lwjgl.glfw.GLFW;

public class ContainerQuickJumpModel extends Model {
    private static final int NEXT_PAGE_SLOT = 8;
    private static final int PREVIOUS_PAGE_SLOT = 17;
    private static final List<Integer> BUTTON_SLOTS = List.of(7, 16, 25, 34, 43, 52);
    private static final List<Integer> QUICK_JUMP_DESTINATIONS = List.of(1, 5, 9, 13, 17, 21);

    public ContainerQuickJumpModel(ContainerModel containerModel) {
        super(List.of(containerModel));
    }

    public void jumpToDestination(int currentPage, int pageDestination, int lastPage) {
        if (currentPage == pageDestination || pageDestination > lastPage) return;

        int pageDifference = pageDestination - currentPage;

        switch (pageDifference) {
            case 1 -> {
                if (currentPage != lastPage) {
                    clickNextPage();
                }
            }
            case -1 -> clickPreviousPage();
            default -> {
                if (!tryUsingJumpButtons(currentPage, pageDestination, lastPage)) {
                    if (currentPage > pageDestination) {
                        clickPreviousPage();
                    } else if (currentPage != lastPage) {
                        clickNextPage();
                    }
                }
            }
        }
    }

    private boolean tryUsingJumpButtons(int currentPage, int pageDestination, int lastPage) {
        int closest = QUICK_JUMP_DESTINATIONS.get(0);
        int closestDistance = Math.abs(closest - pageDestination);
        int currentDistance = Math.abs(currentPage - pageDestination);

        for (int jumpDestination : QUICK_JUMP_DESTINATIONS) {
            int jumpDistance = Math.abs(jumpDestination - pageDestination);

            if (jumpDistance < closestDistance && jumpDestination <= lastPage) {
                closest = jumpDestination;
                closestDistance = jumpDistance;
            }
        }

        if (closestDistance < currentDistance) {
            ContainerUtils.clickOnSlot(
                    BUTTON_SLOTS.get(QUICK_JUMP_DESTINATIONS.indexOf(closest)),
                    McUtils.containerMenu().containerId,
                    GLFW.GLFW_MOUSE_BUTTON_LEFT,
                    McUtils.containerMenu().getItems());

            return true;
        }

        return false;
    }

    private void clickNextPage() {
        ContainerUtils.clickOnSlot(
                NEXT_PAGE_SLOT,
                McUtils.containerMenu().containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                McUtils.containerMenu().getItems());
    }

    private void clickPreviousPage() {
        ContainerUtils.clickOnSlot(
                PREVIOUS_PAGE_SLOT,
                McUtils.containerMenu().containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                McUtils.containerMenu().getItems());
    }
}
