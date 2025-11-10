/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.models.containers.containers.personal.PersonalStorageContainer;
import com.wynntils.models.containers.event.BankPageSetEvent;
import com.wynntils.models.containers.type.BankPageCustomization;
import com.wynntils.models.containers.type.PersonalStorageType;
import com.wynntils.models.containers.type.QuickJumpButtonIcon;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class BankModel extends Model {
    @Persisted
    private final Storage<Integer> finalAccountBankPage = new Storage<>(21);

    @Persisted
    private final Storage<Integer> finalBlockBankPage = new Storage<>(12);

    @Persisted
    private final Storage<Integer> finalBookshelfPage = new Storage<>(10);

    @Persisted
    private final Storage<Integer> finalMiscBucketPage = new Storage<>(10);

    @Persisted
    private final Storage<Map<String, Integer>> finalCharacterBankPages = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<Integer, BankPageCustomization>> customAccountBankPageCustomizations =
            new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<Integer, BankPageCustomization>> customBlockBankPageCustomizations =
            new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<Integer, BankPageCustomization>> customBookshelfPageCustomizations =
            new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<Integer, BankPageCustomization>> customMiscBucketPageCustomizations =
            new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<String, Map<Integer, BankPageCustomization>>> customCharacterBankPagesCustomizations =
            new Storage<>(new TreeMap<>());

    public static final int QUICK_JUMP_SLOT = 7;
    private static final String FINAL_PAGE_NAME = "\uDB3F\uDFFF";

    private static final int MAX_CHARACTER_BANK_PAGES = 12;
    private static final StyledText LAST_BANK_PAGE_STRING = StyledText.fromString(">§4>§c>§4>§c>");

    private boolean editingMode;
    private boolean updatedPage;
    private int currentPage = 1;
    private PersonalStorageContainer personalStorageContainer = null;
    private PersonalStorageType storageContainerType = null;

    public BankModel() {
        super(List.of());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onScreenInit(ScreenInitEvent.Pre e) {
        if (!(Models.Container.getCurrentContainer() instanceof PersonalStorageContainer container)) {
            storageContainerType = null;
            return;
        }

        personalStorageContainer = container;

        storageContainerType = personalStorageContainer.getPersonalStorageType();

        editingMode = false;
        updatedPage = false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScreenClose(ScreenClosedEvent.Post e) {
        storageContainerType = null;
        currentPage = 1;
        editingMode = false;
        updatedPage = false;
    }

    // Swapping between account/character bank or personal/island storage does not
    // send the set slot packets for the slots we need to check so we have to use
    // the set content packet
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onContainerSetContent(ContainerSetContentEvent.Pre event) {
        if (storageContainerType == null) return;

        ItemStack previousPageItem = event.getItems().get(personalStorageContainer.getPreviousItemSlot());
        ItemStack nextPageItem = event.getItems().get(personalStorageContainer.getNextItemSlot());

        updateState(previousPageItem, nextPageItem);

        updatedPage = true;
    }

    // Right clicking the next/previous buttons or using quick jumps with a full inventory
    // does not send the set content packet, so we have to check the set slot packets
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onContainerSetSlot(ContainerSetSlotEvent.Pre event) {
        if (storageContainerType == null) return;
        if (!updatedPage) return;

        if (event.getSlot() == personalStorageContainer.getPreviousItemSlot()) {
            updateState(event.getItemStack(), ItemStack.EMPTY);
        }

        if (event.getSlot() == personalStorageContainer.getNextItemSlot()) {
            updateState(ItemStack.EMPTY, event.getItemStack());
        }
    }

    public BankPageCustomization getPageCustomization(int page) {
        Map<Integer, BankPageCustomization> pageNamesMap = getCurrentCustomizationMap();
        if (pageNamesMap == null) return new BankPageCustomization(page);

        return pageNamesMap.getOrDefault(page, new BankPageCustomization(page));
    }

    public void saveCurrentPageName(String nameToSet) {
        updatePageCustomization(currentPage, (customPageCustomization) -> customPageCustomization.setName(nameToSet));
    }

    public void savePageIcon(Integer pageIndex, QuickJumpButtonIcon iconToSet) {
        updatePageCustomization(pageIndex, (customPageCustomization) -> customPageCustomization.setIcon(iconToSet));
    }

    private void updatePageCustomization(Integer pageIndex, Consumer<BankPageCustomization> updater) {
        switch (storageContainerType) {
            case ACCOUNT_BANK -> updatePageCustomization(pageIndex, updater, customAccountBankPageCustomizations);
            case BLOCK_BANK -> updatePageCustomization(pageIndex, updater, customBlockBankPageCustomizations);
            case BOOKSHELF -> updatePageCustomization(pageIndex, updater, customBookshelfPageCustomizations);
            case CHARACTER_BANK -> {
                customCharacterBankPagesCustomizations.get().putIfAbsent(Models.Character.getId(), new TreeMap<>());

                Map<Integer, BankPageCustomization> nameMap =
                        customCharacterBankPagesCustomizations.get().get(Models.Character.getId());

                var customization = nameMap.getOrDefault(pageIndex, new BankPageCustomization(pageIndex));

                updater.accept(customization);

                if (customization.equals(new BankPageCustomization(pageIndex))) {
                    nameMap.remove(pageIndex);
                } else {
                    nameMap.put(pageIndex, customization);
                }

                customCharacterBankPagesCustomizations.get().put(Models.Character.getId(), nameMap);
                customCharacterBankPagesCustomizations.touched();
            }
            case MISC_BUCKET -> updatePageCustomization(pageIndex, updater, customMiscBucketPageCustomizations);
        }
    }

    private void updatePageCustomization(
            Integer pageIndex,
            Consumer<BankPageCustomization> updater,
            Storage<Map<Integer, BankPageCustomization>> storage) {
        var customization = storage.get().getOrDefault(pageIndex, new BankPageCustomization(pageIndex));

        updater.accept(customization);

        if (customization.equals(new BankPageCustomization(pageIndex))) {
            storage.get().remove(pageIndex);
        } else {
            storage.get().put(pageIndex, customization);
        }

        storage.touched();
    }

    public void resetCurrentPageName() {
        saveCurrentPageName(I18n.get("feature.wynntils.personalStorageUtilities.page", currentPage));
    }

    public int getFinalPage() {
        return switch (storageContainerType) {
            case ACCOUNT_BANK -> finalAccountBankPage.get();
            case BLOCK_BANK -> finalBlockBankPage.get();
            case BOOKSHELF -> finalBookshelfPage.get();
            case CHARACTER_BANK ->
                finalCharacterBankPages.get().getOrDefault(Models.Character.getId(), MAX_CHARACTER_BANK_PAGES);
            case MISC_BUCKET -> finalMiscBucketPage.get();
        };
    }

    public PersonalStorageType getStorageContainerType() {
        return storageContainerType;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public boolean isEditingMode() {
        return editingMode;
    }

    public void toggleEditingMode(boolean editingMode) {
        this.editingMode = editingMode;
    }

    private void updateState(ItemStack previousPageItem, ItemStack nextPageItem) {
        Matcher previousPageMatcher = StyledText.fromComponent(previousPageItem.getHoverName())
                .getMatcher(personalStorageContainer.getPreviousItemPattern());

        if (previousPageMatcher.matches()) {
            currentPage = Integer.parseInt(previousPageMatcher.group(1)) + 1;
        }

        Matcher nextPageMatcher = StyledText.fromComponent(nextPageItem.getHoverName())
                .getMatcher(personalStorageContainer.getNextItemPattern());

        if (nextPageMatcher.matches()) {
            currentPage = Integer.parseInt(nextPageMatcher.group(1)) - 1;
        }

        if (isItemIndicatingLastBankPage(nextPageItem)) {
            updateFinalPage();
        }

        WynntilsMod.postEvent(new BankPageSetEvent());
    }

    private boolean isItemIndicatingLastBankPage(ItemStack item) {
        return StyledText.fromComponent(item.getHoverName()).endsWith(LAST_BANK_PAGE_STRING)
                || item.getHoverName().getString().equals(FINAL_PAGE_NAME);
    }

    private void updateFinalPage() {
        switch (storageContainerType) {
            case ACCOUNT_BANK -> {
                finalAccountBankPage.store(currentPage);
            }
            case BLOCK_BANK -> {
                if (currentPage > finalBlockBankPage.get()) {
                    finalBlockBankPage.store(currentPage);
                }
            }
            case BOOKSHELF -> {
                finalBookshelfPage.store(currentPage);
            }
            case CHARACTER_BANK -> {
                finalCharacterBankPages.get().put(Models.Character.getId(), currentPage);
                finalCharacterBankPages.touched();
            }
            case MISC_BUCKET -> {
                finalMiscBucketPage.store(currentPage);
            }
        }
    }

    private Map<Integer, BankPageCustomization> getCurrentCustomizationMap() {
        return switch (storageContainerType) {
            case ACCOUNT_BANK -> customAccountBankPageCustomizations.get();
            case BLOCK_BANK -> customBlockBankPageCustomizations.get();
            case BOOKSHELF -> customBookshelfPageCustomizations.get();
            case CHARACTER_BANK ->
                customCharacterBankPagesCustomizations.get().getOrDefault(Models.Character.getId(), new TreeMap<>());
            case MISC_BUCKET -> customMiscBucketPageCustomizations.get();
        };
    }
}
