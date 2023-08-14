/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.models.containers.type.SearchableContainerType;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class ContainerQuickJumpModel extends Model {
    private static final int MAX_BANK_PAGES = 21;
    private static final int NEXT_PAGE_SLOT = 8;
    private static final int PREVIOUS_PAGE_SLOT = 17;
    private static final List<Integer> BUTTON_SLOTS = List.of(7, 16, 25, 34, 43, 52);
    private static final List<Integer> QUICK_JUMP_DESTINATIONS = List.of(1, 5, 9, 13, 17, 21);

    private boolean quickJumping = false;
    private int currentPage = 1;
    private int lastPage = MAX_BANK_PAGES;
    private int pageDestination = 1;
    private SearchableContainerType currentContainer;

    public ContainerQuickJumpModel(ContainerModel containerModel) {
        super(List.of(containerModel));
    }

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent e) {
        if (!(e.getScreen() instanceof AbstractContainerScreen<?> screen)) return;

        if (Models.Container.isBankScreen(screen)) {
            currentContainer = SearchableContainerType.BANK;
            lastPage = Models.Container.getFinalBankPage();
        } else if (Models.Container.isBlockBankScreen(screen)) {
            currentContainer = SearchableContainerType.BLOCK_BANK;
            lastPage = Models.Container.getFinalBlockBankPage();
        } else if (Models.Container.isBookshelfScreen(screen)) {
            currentContainer = SearchableContainerType.BOOKSHELF;
            lastPage = Models.Container.getFinalBookshelfPage();
        } else if (Models.Container.isMiscBucketScreen(screen)) {
            currentContainer = SearchableContainerType.MISC_BUCKET;
            lastPage = Models.Container.getFinalMiscBucketPage();
        } else {
            currentContainer = null;
            currentPage = 1;
            pageDestination = 1;
            return;
        }

        currentPage = Models.Container.getCurrentBankPage(screen);

        if (!quickJumping) return;

        if (pageDestination > lastPage) {
            quickJumping = false;
            pageDestination = currentPage;
        } else if (pageDestination != currentPage) {
            quickJumping = true;
            Models.ContainerQuickJump.jumpToDestination();
        } else {
            quickJumping = false;
        }
    }

    @SubscribeEvent
    public void onScreenClose(ScreenClosedEvent e) {
        currentContainer = null;
        currentPage = 1;
        pageDestination = 1;
    }

    @SubscribeEvent
    public void onContainerSetEvent(ContainerSetContentEvent.Post e) {
        if (currentContainer == null) return;

        if (Models.Container.isItemIndicatingLastBankPage(e.getItems().get(Models.Container.LAST_BANK_PAGE_SLOT))) {
            switch (currentContainer) {
                case BANK -> Models.Container.updateFinalBankPage(currentPage);
                case BLOCK_BANK -> Models.Container.updateFinalBlockBankPage(currentPage);
                case BOOKSHELF -> Models.Container.updateFinalBookshelfPage(currentPage);
                case MISC_BUCKET -> Models.Container.updateFinalMiscBucketPage(currentPage);
            }

            lastPage = currentPage;
        }
    }

    public SearchableContainerType getCurrentContainer() {
        return currentContainer;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setPageDestination(int destination) {
        this.pageDestination = destination;
    }

    public void jumpToDestination() {
        quickJumping = true;

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
                if (!tryUsingJumpButtons()) {
                    if (currentPage > pageDestination) {
                        clickPreviousPage();
                    } else if (currentPage != lastPage) {
                        clickNextPage();
                    }
                }
            }
        }
    }

    private boolean tryUsingJumpButtons() {
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
