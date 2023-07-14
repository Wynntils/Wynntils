/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.INVENTORY)
public class CustomBankPagesFeature extends Feature {
    // Change to ranged integer when implemented
    @RegisterConfig
    public final Config<Integer> buttonOnePage = new Config<>(1);

    @RegisterConfig
    public final Config<Integer> buttonTwoPage = new Config<>(5);

    @RegisterConfig
    public final Config<Integer> buttonThreePage = new Config<>(9);

    @RegisterConfig
    public final Config<Integer> buttonFourPage = new Config<>(13);

    @RegisterConfig
    public final Config<Integer> buttonFivePage = new Config<>(17);

    @RegisterConfig
    public final Config<Integer> buttonSixPage = new Config<>(21);

    private static final int MAX_BANK_PAGES = 21;
    private static final int NEXT_PAGE_SLOT = 8;
    private static final int PREVIOUS_PAGE_SLOT = 17;
    private static final List<Integer> BUTTON_SLOTS = List.of(7, 16, 25, 34, 43, 52);
    private static final List<Integer> QUICK_JUMP_DESTINATIONS = List.of(1, 5, 9, 13, 17, 21);

    private boolean isBankScreen = false;
    private boolean quickJumping = false;
    private int currentPage = 1;
    private int lastPage = MAX_BANK_PAGES;
    private int pageDestination = 1;
    private List<Integer> customJumpDestinations;

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent e) {
        if (!(e.getScreen() instanceof AbstractContainerScreen<?> screen)) return;
        if (!Models.Container.isBankScreen(screen)) return;

        isBankScreen = true;

        currentPage = Models.Container.getCurrentBankPage(screen);

        lastPage = Models.Container.getFinalBankPage();

        customJumpDestinations = List.of(
                buttonOnePage.get(),
                buttonTwoPage.get(),
                buttonThreePage.get(),
                buttonFourPage.get(),
                buttonFivePage.get(),
                buttonSixPage.get());

        if (!quickJumping) return;

        if (pageDestination > lastPage) {
            quickJumping = false;
            pageDestination = currentPage;
        } else if (pageDestination != currentPage) {
            jumpToDestination();
        } else {
            quickJumping = false;
        }
    }

    @SubscribeEvent
    public void onContainerClose(ContainerCloseEvent.Post e) {
        isBankScreen = false;
        currentPage = 1;
        pageDestination = 1;
    }

    @SubscribeEvent
    public void onSlotClicked(ContainerClickEvent e) {
        if (!isBankScreen) return;

        int slotIndex = e.getSlotNum();

        if (BUTTON_SLOTS.contains(slotIndex)) {
            int buttonIndex = BUTTON_SLOTS.indexOf(slotIndex);
            pageDestination = customJumpDestinations.get(buttonIndex);
            e.setCanceled(true);
            jumpToDestination();
        }
    }

    @SubscribeEvent
    public void onSetSlot(SetSlotEvent.Pre e) {
        if (!isBankScreen) return;
        if (e.getContainer() instanceof Inventory) return;

        if (BUTTON_SLOTS.contains(e.getSlot())) {
            ItemStack jumpButton = new ItemStack(Items.DIAMOND_AXE);
            jumpButton.setDamageValue(92);

            CompoundTag jumpTag = jumpButton.getOrCreateTag();
            jumpTag.putInt("HideFlags", 6);
            jumpTag.putBoolean("Unbreakable", true);
            jumpButton.setTag(jumpTag);

            int buttonIndex = BUTTON_SLOTS.indexOf(e.getSlot());
            int buttonDestination = customJumpDestinations.get(buttonIndex);

            jumpButton.setCount(buttonDestination);

            jumpButton.setHoverName(Component.literal(ChatFormatting.GRAY + "Jump to Page " + buttonDestination));

            e.setItemStack(jumpButton);
        }
    }

    @SubscribeEvent
    public void onContainerSetEvent(ContainerSetContentEvent.Post e) {
        if (Models.Container.isItemIndicatingLastBankPage(e.getItems().get(Models.Container.LAST_BANK_PAGE_SLOT))) {
            Models.Container.updateFinalBankPage(currentPage);
            lastPage = currentPage;
        }
    }

    private void jumpToDestination() {
        quickJumping = true;

        if (currentPage == pageDestination) return;

        int pageDifference = pageDestination - currentPage;

        switch (pageDifference) {
            case 1 -> {
                if (lastPage != currentPage) {
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

            if (jumpDistance < closestDistance && jumpDestination < lastPage) {
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

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        switch (configHolder.getFieldName()) {
            case "buttonOnePage",
                    "buttonFourPage",
                    "buttonFivePage",
                    "buttonSixPage",
                    "buttonThreePage",
                    "buttonTwoPage" -> {
                if ((int) configHolder.getValue() < 1) {
                    configHolder.setValue(1);
                } else if ((int) configHolder.getValue() > MAX_BANK_PAGES) {
                    configHolder.setValue(MAX_BANK_PAGES);
                }
            }
        }
    }
}
