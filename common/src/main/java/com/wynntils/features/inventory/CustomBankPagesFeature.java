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
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.models.containers.type.SearchableContainerType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class CustomBankPagesFeature extends Feature {
    // Change to ranged integer/integer list when implemented
    @Persisted
    public final Config<String> bankDestinations = new Config<>("1,5,9,13,17,21");

    @Persisted
    public final Config<String> blockBankDestinations = new Config<>("1,3,5,8,10,12");

    @Persisted
    public final Config<String> bookshelfDestinations = new Config<>("1,3,4,6,8,10");

    @Persisted
    public final Config<String> miscBucketDestinations = new Config<>("1,3,4,6,8,10");

    private static final int MAX_BANK_PAGES = 21;
    private static final int MAX_BLOCK_BANK_PAGES = 12;
    private static final int MAX_HOUSING_CONTAINER_PAGES = 10;
    private static final int MAX_DESTINATIONS = 6;
    private static final List<Integer> BUTTON_SLOTS = List.of(7, 16, 25, 34, 43, 52);
    private static final List<Integer> BLOCK_BANK_DESTINATIONS = List.of(1, 3, 5, 8, 10, 12);
    private static final List<Integer> HOUSING_DEFAULT_DESTINATIONS = List.of(1, 3, 4, 6, 8, 10);
    private static final List<Integer> QUICK_JUMP_DESTINATIONS = List.of(1, 5, 9, 13, 17, 21);

    private List<Integer> customJumpDestinations;

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent e) {
        if (!(e.getScreen() instanceof AbstractContainerScreen<?> screen)) return;

        SearchableContainerType currentContainer;

        if (Models.Container.isBankScreen(screen)) {
            currentContainer = SearchableContainerType.BANK;
        } else if (Models.Container.isBlockBankScreen(screen)) {
            currentContainer = SearchableContainerType.BLOCK_BANK;
        } else if (Models.Container.isBookshelfScreen(screen)) {
            currentContainer = SearchableContainerType.BOOKSHELF;
        } else if (Models.Container.isMiscBucketScreen(screen)) {
            currentContainer = SearchableContainerType.MISC_BUCKET;
        } else {
            return;
        }

        getCustomJumpDestinations(currentContainer);
    }

    private void getCustomJumpDestinations(SearchableContainerType currentContainer) {
        String configDestinations;

        switch (currentContainer) {
            case BANK -> configDestinations = bankDestinations.get();
            case BLOCK_BANK -> configDestinations = blockBankDestinations.get();
            case BOOKSHELF -> configDestinations = bookshelfDestinations.get();
            case MISC_BUCKET -> configDestinations = miscBucketDestinations.get();
            default -> {
                return;
            }
        }

        customJumpDestinations = parseStringToDestinations(configDestinations, currentContainer);

        if (customJumpDestinations == null) {
            customJumpDestinations = getDefaultJumpDestinations();
        }
    }

    private List<Integer> getDefaultJumpDestinations() {
        return switch (Models.ContainerQuickJump.getCurrentContainer()) {
            case BANK -> QUICK_JUMP_DESTINATIONS;
            case BLOCK_BANK -> BLOCK_BANK_DESTINATIONS;
            default -> HOUSING_DEFAULT_DESTINATIONS; // this has the lowest values, so it's the safest default
        };
    }

    @SubscribeEvent
    public void onSlotClicked(ContainerClickEvent e) {
        if (Models.ContainerQuickJump.getCurrentContainer() == null) return;

        int slotIndex = e.getSlotNum();

        if (BUTTON_SLOTS.contains(slotIndex)) {
            int buttonIndex = BUTTON_SLOTS.indexOf(slotIndex);
            int pageDestination = customJumpDestinations.get(buttonIndex);
            int wynnDestination = QUICK_JUMP_DESTINATIONS.get(buttonIndex);

            if (pageDestination != wynnDestination) {
                e.setCanceled(true);
                Models.ContainerQuickJump.setPageDestination(pageDestination);
                Models.ContainerQuickJump.jumpToDestination();
            }
        }
    }

    @SubscribeEvent
    public void onSetSlot(SetSlotEvent.Pre e) {
        if (Models.ContainerQuickJump.getCurrentContainer() == null) return;
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

    @Override
    protected void onConfigUpdate(Config<?> unknownConfig) {
        String fieldName = unknownConfig.getFieldName();

        SearchableContainerType containerType;
        int maxValue;

        switch (fieldName) {
            case "bankDestinations" -> {
                containerType = SearchableContainerType.BANK;
                maxValue = MAX_BANK_PAGES;
            }
            case "blockBankDestinations" -> {
                containerType = SearchableContainerType.BLOCK_BANK;
                maxValue = MAX_BLOCK_BANK_PAGES;
            }
            case "bookshelfDestinations", "miscBucketDestinations" -> {
                containerType = SearchableContainerType.BOOKSHELF;
                maxValue = MAX_HOUSING_CONTAINER_PAGES;
            }
            default -> {
                return;
            }
        }

        // If we're still here, we have a string config
        Config<String> config = (Config<String>) unknownConfig;
        String valueString = config.getValue();

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
                    case BANK -> defaultValues = QUICK_JUMP_DESTINATIONS;
                    case BLOCK_BANK -> defaultValues = BLOCK_BANK_DESTINATIONS;
                    case BOOKSHELF, MISC_BUCKET -> defaultValues = HOUSING_DEFAULT_DESTINATIONS;
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
}
