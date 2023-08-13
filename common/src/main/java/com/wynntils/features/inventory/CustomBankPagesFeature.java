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
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.models.containers.type.SearchableContainerType;
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

    public static final List<Integer> BLOCK_BANK_DESTINATIONS = List.of(1, 3, 5, 8, 10, 12);
    public static final List<Integer> HOUSING_DEFAULT_DESTINATIONS = List.of(1, 3, 4, 6, 8, 10);

    private List<Integer> customJumpDestinations;

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent event) {
        initCustomJumpDestinations();
    }

    private void initCustomJumpDestinations() {
        SearchableContainerType containerType = Models.Container.getContainerType();
        if (containerType == null) return;

        String configDestinations;

        switch (containerType) {
            case BANK -> configDestinations = bankDestinations.get();
            case BLOCK_BANK -> configDestinations = blockBankDestinations.get();
            case BOOKSHELF -> configDestinations = bookshelfDestinations.get();
            case MISC_BUCKET -> configDestinations = miscBucketDestinations.get();
            default -> {
                return;
            }
        }

        customJumpDestinations = parseStringToDestinations(configDestinations, containerType);

        if (customJumpDestinations == null) {
            customJumpDestinations = getJumpDestinations(containerType);
        }
    }

    @SubscribeEvent
    public void onSlotClicked(ContainerClickEvent e) {
        if (Models.Container.getContainerType() == null) return;

        int buttonIndex = Models.ContainerQuickJump.BUTTON_SLOTS.indexOf(e.getSlotNum());

        if (buttonIndex == -1) return;

        Models.ContainerQuickJump.jumpToPage(customJumpDestinations.get(buttonIndex));
        e.setCanceled(true);
    }

    @SubscribeEvent
    public void onSetSlot(SetSlotEvent.Pre e) {
        if (Models.Container.getContainerType() == null) return;
        if (e.getContainer() instanceof Inventory) return;

        if (Models.ContainerQuickJump.BUTTON_SLOTS.contains(e.getSlot())) {
            ItemStack jumpButton = new ItemStack(Items.DIAMOND_AXE);
            jumpButton.setDamageValue(92);

            CompoundTag jumpTag = jumpButton.getOrCreateTag();
            jumpTag.putInt("HideFlags", 6);
            jumpTag.putBoolean("Unbreakable", true);
            jumpButton.setTag(jumpTag);

            int buttonIndex = Models.ContainerQuickJump.BUTTON_SLOTS.indexOf(e.getSlot());
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
                List<Integer> defaultValues = getJumpDestinations(containerType);

                for (int i = startIndex; i < MAX_DESTINATIONS; i++) {
                    destinations.add(defaultValues.get(i));
                }
            }

            return destinations;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private List<Integer> getJumpDestinations(SearchableContainerType containerType) {
        return switch (containerType) {
            case BANK -> Models.ContainerQuickJump.QUICK_JUMP_DESTINATIONS;
            case BLOCK_BANK -> BLOCK_BANK_DESTINATIONS;
            default -> HOUSING_DEFAULT_DESTINATIONS;
        };
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
