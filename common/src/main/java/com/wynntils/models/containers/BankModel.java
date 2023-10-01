/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.models.containers.type.SearchableContainerType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BankModel extends Model {
    @Persisted
    private final Storage<Integer> finalBankPage = new Storage<>(21);

    @Persisted
    private final Storage<Integer> finalBlockBankPage = new Storage<>(12);

    @Persisted
    private final Storage<Integer> finalBookshelfPage = new Storage<>(10);

    @Persisted
    private final Storage<Integer> finalMiscBucketPage = new Storage<>(10);

    @Persisted
    private final Storage<Map<Integer, String>> customBankPageNames = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<Integer, String>> customBlockBankPageNames = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<Integer, String>> customBookshelfPageNames = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<Integer, String>> customMiscBucketPageNames = new Storage<>(new TreeMap<>());

    private boolean editingName;
    private int currentPage = 1;
    private SearchableContainerType currentContainer;

    public BankModel() {
        super(List.of());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScreenInit(ScreenInitEvent e) {
        if (!(e.getScreen() instanceof AbstractContainerScreen<?> screen)) return;

        if (Models.Container.isBankScreen(screen)) {
            currentContainer = SearchableContainerType.BANK;
        } else if (Models.Container.isBlockBankScreen(screen)) {
            currentContainer = SearchableContainerType.BLOCK_BANK;
        } else if (Models.Container.isBookshelfScreen(screen)) {
            currentContainer = SearchableContainerType.BOOKSHELF;
        } else if (Models.Container.isMiscBucketScreen(screen)) {
            currentContainer = SearchableContainerType.MISC_BUCKET;
        } else {
            currentContainer = null;
            currentPage = 1;
            return;
        }

        currentPage = Models.Container.getCurrentBankPage(screen);

        editingName = false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScreenClose(ScreenClosedEvent e) {
        currentContainer = null;
        currentPage = 1;
        editingName = false;
    }

    public Optional<String> getPageName(int page) {
        Storage<Map<Integer, String>> pageNamesMap = getCurrentNameMap();

        if (pageNamesMap == null) return Optional.empty();

        return Optional.ofNullable(pageNamesMap.get().get(page));
    }

    public void saveCurrentPageName(String nameToSet) {
        Storage<Map<Integer, String>> pageNamesMap = getCurrentNameMap();

        if (pageNamesMap == null) return;

        pageNamesMap.get().put(currentPage, nameToSet);
        pageNamesMap.touched();

        editingName = false;
    }

    public void resetCurrentPageName() {
        Storage<Map<Integer, String>> pageNamesMap = getCurrentNameMap();

        if (pageNamesMap == null) return;

        pageNamesMap.get().remove(currentPage);
        pageNamesMap.touched();

        editingName = false;
    }

    public int getFinalPage() {
        return switch (currentContainer) {
            case BANK -> finalBankPage.get();
            case BLOCK_BANK -> finalBlockBankPage.get();
            case BOOKSHELF -> finalBookshelfPage.get();
            case MISC_BUCKET -> finalMiscBucketPage.get();
            default -> 1;
        };
    }

    public void updateFinalPage() {
        switch (currentContainer) {
            case BANK -> {
                finalBankPage.store(currentPage);
            }
            case BLOCK_BANK -> {
                if (currentPage > finalBlockBankPage.get()) {
                    finalBlockBankPage.store(currentPage);
                }
            }
            case BOOKSHELF -> {
                finalBookshelfPage.store(currentPage);
            }
            case MISC_BUCKET -> {
                finalMiscBucketPage.store(currentPage);
            }
        }
    }

    public SearchableContainerType getCurrentContainer() {
        return currentContainer;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public boolean isEditingName() {
        return editingName;
    }

    public void toggleEditingName(boolean editingName) {
        this.editingName = editingName;
    }

    private Storage<Map<Integer, String>> getCurrentNameMap() {
        return switch (currentContainer) {
            case BANK -> customBankPageNames;
            case BLOCK_BANK -> customBlockBankPageNames;
            case BOOKSHELF -> customBookshelfPageNames;
            case MISC_BUCKET -> customMiscBucketPageNames;
            default -> null;
        };
    }
}
