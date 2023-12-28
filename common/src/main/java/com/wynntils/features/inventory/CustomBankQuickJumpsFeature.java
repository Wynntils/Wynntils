/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.models.containers.type.SearchableContainerType;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.INVENTORY)
public class CustomBankQuickJumpsFeature extends Feature {
    // Change to ranged integer/integer list when implemented
    @Persisted
    public final Config<String> accountBankDestinations = new Config<>("1,5,9,13,17,21");

    @Persisted
    public final Config<String> blockBankDestinations = new Config<>("1,3,5,8,10,12");

    @Persisted
    public final Config<String> bookshelfDestinations = new Config<>("1,3,4,6,8,10");

    @Persisted
    public final Config<String> characterBankDestinations = new Config<>("1,3,4,6,8,10");

    @Persisted
    public final Config<String> miscBucketDestinations = new Config<>("1,3,4,6,8,10");

    private static final int MAX_ACCOUNT_BANK_PAGES = 21;
    private static final int MAX_BLOCK_BANK_PAGES = 12;
    private static final int MAX_DEFAULT_PAGES = 10;
    private static final int MAX_DESTINATIONS = 6;
    private static final int NEXT_PAGE_SLOT = 8;
    private static final int PREVIOUS_PAGE_SLOT = 17;
    private static final List<Integer> BUTTON_SLOTS = List.of(7, 16, 25, 34, 43, 52);
    private static final List<Integer> BLOCK_BANK_DESTINATIONS = List.of(1, 3, 5, 8, 10, 12);
    private static final List<Integer> DEFAULT_DESTINATIONS = List.of(1, 3, 4, 6, 8, 10);
    private static final List<Integer> QUICK_JUMP_DESTINATIONS = List.of(1, 5, 9, 13, 17, 21);

    private boolean quickJumping = false;
    private int currentPage = 1;
    private int lastPage = MAX_ACCOUNT_BANK_PAGES;
    private int pageDestination = 1;
    private List<Integer> customJumpDestinations;

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent e) {
        if (Models.Bank.getCurrentContainer() == null) return;

        lastPage = Models.Bank.getFinalPage();

        getCustomJumpDestinations();

        currentPage = Models.Bank.getCurrentPage();
    }

    @SubscribeEvent
    public void onScreenClose(ScreenClosedEvent e) {
        pageDestination = 1;
        quickJumping = false;
    }

    @SubscribeEvent
    public void onSetSlot(SetSlotEvent.Pre e) {
        if (Models.Bank.getCurrentContainer() == null) return;
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

            String hoverName;

            if (Models.Bank.getPageName(buttonDestination).isPresent()) {
                hoverName = ChatFormatting.GRAY + "Jump to Page " + buttonDestination + "\n - "
                        + Models.Bank.getPageName(buttonDestination).get();
            } else {
                hoverName = ChatFormatting.GRAY + "Jump to Page " + buttonDestination;
            }

            jumpButton.setHoverName(Component.literal(hoverName));

            e.setItemStack(jumpButton);
        }
    }

    @SubscribeEvent
    public void onSetSlotPost(SetSlotEvent.Post e) {
        if (!quickJumping) return;

        if (pageDestination > lastPage) {
            quickJumping = false;
            pageDestination = currentPage;
        } else if (pageDestination != currentPage
                && ((e.getSlot() == NEXT_PAGE_SLOT && pageDestination > currentPage)
                        || (e.getSlot() == PREVIOUS_PAGE_SLOT && pageDestination < currentPage))) {
            jumpToDestination();
        } else if (pageDestination == currentPage) {
            quickJumping = false;
        }
    }

    @SubscribeEvent
    public void onContainerSetEvent(ContainerSetContentEvent.Post e) {
        SearchableContainerType currentContainer = Models.Bank.getCurrentContainer();
        if (currentContainer == null) return;

        if (Models.Container.isItemIndicatingLastBankPage(e.getItems().get(Models.Container.LAST_BANK_PAGE_SLOT))) {
            Models.Bank.updateFinalPage();

            lastPage = currentPage;
        }
    }

    @SubscribeEvent
    public void onSlotClicked(ContainerClickEvent e) {
        if (Models.Bank.getCurrentContainer() == null) return;

        int slotIndex = e.getSlotNum();

        if (BUTTON_SLOTS.contains(slotIndex)) {
            int buttonIndex = BUTTON_SLOTS.indexOf(slotIndex);
            pageDestination = customJumpDestinations.get(buttonIndex);
            int wynnDestination = QUICK_JUMP_DESTINATIONS.get(buttonIndex);
            if (pageDestination != wynnDestination) {
                e.setCanceled(true);
                jumpToDestination();
            }
        }
    }

    @Override
    protected void onConfigUpdate(Config<?> unknownConfig) {
        String fieldName = unknownConfig.getFieldName();

        SearchableContainerType containerType;
        int maxValue;

        switch (fieldName) {
            case "accountBankDestinations" -> {
                containerType = SearchableContainerType.ACCOUNT_BANK;
                maxValue = MAX_ACCOUNT_BANK_PAGES;
            }
            case "blockBankDestinations" -> {
                containerType = SearchableContainerType.BLOCK_BANK;
                maxValue = MAX_BLOCK_BANK_PAGES;
            }
            case "bookshelfDestinations", "miscBucketDestinations", "characterBankDestinations" -> {
                containerType = SearchableContainerType.BOOKSHELF;
                maxValue = MAX_DEFAULT_PAGES;
            }
            default -> {
                return;
            }
        }

        // If we're still here, we have a string config
        Config<String> config = (Config<String>) unknownConfig;
        String valueString = config.get();

        List<Integer> originalValues = parseStringToDestinations(valueString, containerType);

        if (originalValues == null) {
            config.setValue(config.getDefaultValue());
            return;
        }

        List<Integer> newValues = modifyJumpValues(originalValues, maxValue);

        String formattedConfig =
                newValues.stream().limit(MAX_DESTINATIONS).map(Object::toString).collect(Collectors.joining(","));

        if (!formattedConfig.equals(valueString)) {
            config.setValue(formattedConfig);
        }
    }

    private List<Integer> parseStringToDestinations(String destinationsStr, SearchableContainerType containerType) {
        String[] destinationStrings = destinationsStr.split(",");

        try {
            List<Integer> destinations = Arrays.stream(destinationStrings)
                    .limit(MAX_DESTINATIONS)
                    .map(Integer::parseInt)
                    .collect(Collectors.toCollection(ArrayList::new));

            if (destinations.size() < MAX_DESTINATIONS) {
                int startIndex = destinations.size();
                List<Integer> defaultValues;

                switch (containerType) {
                    case ACCOUNT_BANK -> defaultValues = QUICK_JUMP_DESTINATIONS;
                    case BLOCK_BANK -> defaultValues = BLOCK_BANK_DESTINATIONS;
                    case BOOKSHELF, MISC_BUCKET, CHARACTER_BANK -> defaultValues = DEFAULT_DESTINATIONS;
                    default -> {
                        return null;
                    }
                }

                for (int i = startIndex; i < MAX_DESTINATIONS; i++) {
                    destinations.add(defaultValues.get(i));
                }
            }

            return destinations;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private List<Integer> modifyJumpValues(List<Integer> originalValues, int maxValue) {
        List<Integer> newValues = new ArrayList<>(originalValues);

        for (int i = 0; i < newValues.size(); i++) {
            int value = newValues.get(i);

            if (value <= 0) {
                newValues.set(i, 1);
            } else if (value > maxValue) {
                newValues.set(i, maxValue);
            }
        }

        return newValues;
    }

    private void jumpToDestination() {
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

    private void getCustomJumpDestinations() {
        String configDestinations;

        switch (Models.Bank.getCurrentContainer()) {
            case ACCOUNT_BANK -> configDestinations = accountBankDestinations.get();
            case BLOCK_BANK -> configDestinations = blockBankDestinations.get();
            case BOOKSHELF -> configDestinations = bookshelfDestinations.get();
            case CHARACTER_BANK -> configDestinations = characterBankDestinations.get();
            case MISC_BUCKET -> configDestinations = miscBucketDestinations.get();
            default -> {
                return;
            }
        }

        customJumpDestinations = parseStringToDestinations(configDestinations, Models.Bank.getCurrentContainer());

        if (customJumpDestinations == null) {
            customJumpDestinations = getDefaultJumpDestinations();
        }
    }

    private List<Integer> getDefaultJumpDestinations() {
        return switch (Models.Bank.getCurrentContainer()) {
            case ACCOUNT_BANK -> QUICK_JUMP_DESTINATIONS;
            case BLOCK_BANK -> BLOCK_BANK_DESTINATIONS;
            default -> DEFAULT_DESTINATIONS; // this has the lowest values, so it's the safest default
        };
    }
}
