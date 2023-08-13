/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
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
    public static final List<Integer> BANK_DESTINATIONS = List.of(1, 5, 9, 13, 17, 21);
    public static final List<Integer> BLOCK_BANK_DESTINATIONS = List.of(1, 3, 5, 8, 10, 12);
    public static final List<Integer> HOUSING_DEFAULT_DESTINATIONS = List.of(1, 3, 4, 6, 8, 10);

    public ContainerQuickJumpModel(ContainerModel containerModel) {
        super(List.of(containerModel));
    }

    private int pageDestination = -1;

    public List<Integer> getJumpDestinations(SearchableContainerType containerType) {
        return switch (containerType) {
            case BANK -> BANK_DESTINATIONS;
            case BLOCK_BANK -> BLOCK_BANK_DESTINATIONS;
            case BOOKSHELF, MISC_BUCKET -> HOUSING_DEFAULT_DESTINATIONS;
            default -> null;
        };
    }

    @SubscribeEvent
    public void onContainerInit(ScreenInitEvent event) {
        navigateCloser();
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
        List<Integer> jumpDestinations = getJumpDestinations(Models.Container.getContainerType());
        int closestDist = jumpDestinations.stream()
                .filter(this::validPage)
                .mapToInt(i -> Math.abs(i - pageDestination))
                .min()
                .orElse(-1);

        if (closestDist < Math.abs(pageDifference)) {
            int index = jumpDestinations.indexOf(closestDist + pageDestination);
            ContainerUtils.clickOnSlot(
                    BUTTON_SLOTS.get(index == -1 ? jumpDestinations.indexOf(-closestDist + pageDestination) : index),
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
