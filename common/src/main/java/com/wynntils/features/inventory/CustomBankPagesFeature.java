/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.wynn.ContainerUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ConfigCategory(Category.INVENTORY)
public class CustomBankPagesFeature extends Feature {
    // If possible, lock these to only allow between 1-21
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
    private static final Pattern BANK_PAGE_MATCHER = Pattern.compile("§0\\[Pg\\. (\\d{1,2})\\] §8\\w+'s§0 Bank");

    private boolean quickJumping = false;
    private int containerId;
    private int currentPage = 1;
    private int pageDestination = 1;
    private List<Integer> customJumpDestinations;

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent e) {
        if (!(e.getScreen() instanceof AbstractContainerScreen<?> screen)) return;

        StyledText title = StyledText.fromComponent(screen.getTitle());

        Matcher pageMatcher = title.getMatcher(BANK_PAGE_MATCHER);

        if (pageMatcher.matches()) {
            containerId = screen.getMenu().containerId;
            currentPage = Integer.parseInt(pageMatcher.group(1));

            customJumpDestinations = List.of(buttonOnePage.get(), buttonTwoPage.get(), buttonThreePage.get(), buttonFourPage.get(), buttonFivePage.get(), buttonSixPage.get());

            if (currentPage != pageDestination && quickJumping) {
                goToPage();
            } else if (quickJumping) {
                quickJumping = false;
            }
        } else {
            return;
        }
    }

    @SubscribeEvent
    public void onContainerClose(ContainerCloseEvent.Post e) {
        currentPage = 1;
        pageDestination = 1;
    }

    @SubscribeEvent
    public void onSlotClicked(ContainerClickEvent e) {
        if (e.getContainerMenu().containerId != containerId) {
            return;
        }

        int slotIndex = e.getSlotNum();

        if (BUTTON_SLOTS.contains(slotIndex)) {
            int buttonIndex = BUTTON_SLOTS.indexOf(slotIndex);
            pageDestination = Math.max(1, Math.min(MAX_BANK_PAGES, customJumpDestinations.get(buttonIndex)));
            e.setCanceled(true);
            goToPage();
        } else if (slotIndex == NEXT_PAGE_SLOT) {
            pageDestination++;
        } else if (slotIndex == PREVIOUS_PAGE_SLOT) {
            pageDestination--;
        }
    }

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.Pre e) {
        if (!(e.getScreen() instanceof AbstractContainerScreen<?> screen)) return;

        StyledText title = StyledText.fromComponent(screen.getTitle());

        Matcher pageMatcher = title.getMatcher(BANK_PAGE_MATCHER);

        if (pageMatcher.matches()) {
            if (BUTTON_SLOTS.contains(e.getSlot().index)) {
                renderQuickJumpButton(e.getPoseStack(), e.getSlot());
            }
        }

        // Prevent buying a new page
        if (e.getSlot().index == NEXT_PAGE_SLOT && e.getSlot().getItem().getHoverName().getString().contains("§c") && pageDestination > currentPage) {
            pageDestination = currentPage;
            quickJumping = false;
        }
    }

    private void renderQuickJumpButton(PoseStack poseStack, Slot buttonSlot) {
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.COLUMN_ARROW_RIGHT.resource(),
                buttonSlot.x - 8,
                buttonSlot.y - 8,
                300,
                32,
                32,
                Texture.COLUMN_ARROW_RIGHT.width(),
                Texture.COLUMN_ARROW_RIGHT.height());

        buttonSlot.set(new ItemStack(Items.SNOW));

        int buttonIndex = BUTTON_SLOTS.indexOf(buttonSlot.index);
        // When config limits 1-21, change to just customJumpDestinations.get(buttonIndex)
        int buttonDestination = Math.max(1, Math.min(MAX_BANK_PAGES, customJumpDestinations.get(buttonIndex)));

        buttonSlot.getItem().setHoverName(Component.literal("§7Jump to Page " + buttonDestination));
    }

    private void goToPage() {
        quickJumping = true;

        if (currentPage == pageDestination) {
            return;
        }

        if (currentPage + 1 == pageDestination) {
            ContainerUtils.clickOnSlot(
                    NEXT_PAGE_SLOT,
                    McUtils.containerMenu().containerId,
                    GLFW.GLFW_MOUSE_BUTTON_LEFT,
                    McUtils.containerMenu().getItems());
        } else if (currentPage - 1 == pageDestination) {
            ContainerUtils.clickOnSlot(
                    PREVIOUS_PAGE_SLOT,
                    McUtils.containerMenu().containerId,
                    GLFW.GLFW_MOUSE_BUTTON_LEFT,
                    McUtils.containerMenu().getItems());
        } else {
            int closest = QUICK_JUMP_DESTINATIONS.get(0);
            int closestDistance = Math.abs(closest - pageDestination);
            int currentDistance = Math.abs(currentPage - pageDestination);

            for (int jumpDestination : QUICK_JUMP_DESTINATIONS) {
                int jumpDistance = Math.abs(jumpDestination - pageDestination);

                if (jumpDistance < closestDistance) {
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
            } else if (closest > pageDestination) {
                ContainerUtils.clickOnSlot(
                        PREVIOUS_PAGE_SLOT,
                        McUtils.containerMenu().containerId,
                        GLFW.GLFW_MOUSE_BUTTON_LEFT,
                        McUtils.containerMenu().getItems());
            } else {
                ContainerUtils.clickOnSlot(
                        NEXT_PAGE_SLOT,
                        McUtils.containerMenu().containerId,
                        GLFW.GLFW_MOUSE_BUTTON_LEFT,
                        McUtils.containerMenu().getItems());
            }
        }
    }
}
