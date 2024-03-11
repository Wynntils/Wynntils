/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.models.containers.type.InteractiveContainerType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BankModel extends Model {
    // When storage supports upfixing, change to finalAccountBankPage
    @Persisted
    private final Storage<Integer> finalBankPage = new Storage<>(21);

    @Persisted
    private final Storage<Integer> finalBlockBankPage = new Storage<>(12);

    @Persisted
    private final Storage<Integer> finalBookshelfPage = new Storage<>(10);

    @Persisted
    private final Storage<Integer> finalMiscBucketPage = new Storage<>(10);

    @Persisted
    private final Storage<Map<String, Integer>> finalCharacterBankPages = new Storage<>(new TreeMap<>());

    // When storage supports upfixing, change to customAccountBankPageNames
    @Persisted
    private final Storage<Map<Integer, String>> customBankPageNames = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<Integer, String>> customBlockBankPageNames = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<Integer, String>> customBookshelfPageNames = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<Integer, String>> customMiscBucketPageNames = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<String, Map<Integer, String>>> customCharacterBankPagesNames =
            new Storage<>(new TreeMap<>());

    public static final int LAST_BANK_PAGE_SLOT = 8;

    // Test in BankModel_PERSONAL_STORAGE_PATTERN
    private static final Pattern PERSONAL_STORAGE_PATTERN =
            Pattern.compile("^§0\\[Pg\\. (\\d+)\\] §8[a-zA-Z0-9_ ]+'s?§0 (.*)$");
    private static final int MAX_CHARACTER_BANK_PAGES = 10;
    private static final StyledText LAST_BANK_PAGE_STRING = StyledText.fromString(">§4>§c>§4>§c>");

    private boolean editingName;
    private int currentPage = 1;
    private InteractiveContainerType currentContainer;

    public BankModel() {
        super(List.of());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScreenInit(ScreenInitEvent e) {
        if (!(e.getScreen() instanceof AbstractContainerScreen<?> screen)) return;

        for (InteractiveContainerType type : InteractiveContainerType.values()) {
            if (type.isBank() && type.isScreen(screen)) {
                currentContainer = type;
                break;
            }
        }

        if (currentContainer == null) return;

        currentPage = getCurrentBankPage(screen);

        editingName = false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScreenClose(ScreenClosedEvent e) {
        currentContainer = null;
        currentPage = 1;
        editingName = false;
    }

    public int getCurrentBankPage(Screen screen) {
        Matcher matcher = StyledText.fromComponent(screen.getTitle()).getMatcher(PERSONAL_STORAGE_PATTERN);
        if (!matcher.matches()) return 0;

        return Integer.parseInt(matcher.group(1));
    }

    public boolean isItemIndicatingLastBankPage(ItemStack item) {
        return StyledText.fromComponent(item.getHoverName()).endsWith(LAST_BANK_PAGE_STRING)
                || item.getHoverName().getString().equals(" ");
    }

    public Optional<String> getPageName(int page) {
        Map<Integer, String> pageNamesMap = getCurrentNameMap();

        if (pageNamesMap == null) return Optional.empty();

        return Optional.ofNullable(pageNamesMap.get(page));
    }

    public void saveCurrentPageName(String nameToSet) {
        switch (currentContainer) {
            case ACCOUNT_BANK -> {
                customBankPageNames.get().put(currentPage, nameToSet);
                customBankPageNames.touched();
            }
            case BLOCK_BANK -> {
                customBlockBankPageNames.get().put(currentPage, nameToSet);
                customBlockBankPageNames.touched();
            }
            case BOOKSHELF -> {
                customBookshelfPageNames.get().put(currentPage, nameToSet);
                customBookshelfPageNames.touched();
            }
            case CHARACTER_BANK -> {
                customCharacterBankPagesNames.get().putIfAbsent(Models.Character.getId(), new TreeMap<>());

                Map<Integer, String> nameMap =
                        customCharacterBankPagesNames.get().get(Models.Character.getId());

                nameMap.put(currentPage, nameToSet);

                customCharacterBankPagesNames.get().put(Models.Character.getId(), nameMap);
                customCharacterBankPagesNames.touched();
            }
            case MISC_BUCKET -> {
                customMiscBucketPageNames.get().put(currentPage, nameToSet);
                customMiscBucketPageNames.touched();
            }
        }

        editingName = false;
    }

    public void resetCurrentPageName() {
        switch (currentContainer) {
            case ACCOUNT_BANK -> {
                customBankPageNames.get().remove(currentPage);
                customBankPageNames.touched();
            }
            case BLOCK_BANK -> {
                customBlockBankPageNames.get().remove(currentPage);
                customBlockBankPageNames.touched();
            }
            case BOOKSHELF -> {
                customBookshelfPageNames.get().remove(currentPage);
                customBookshelfPageNames.touched();
            }
            case CHARACTER_BANK -> {
                customCharacterBankPagesNames
                        .get()
                        .getOrDefault(Models.Character.getId(), new TreeMap<>())
                        .remove(currentPage);
                customCharacterBankPagesNames.touched();
            }
            case MISC_BUCKET -> {
                customMiscBucketPageNames.get().remove(currentPage);
                customMiscBucketPageNames.touched();
            }
        }

        editingName = false;
    }

    public int getFinalPage() {
        return switch (currentContainer) {
            case ACCOUNT_BANK -> finalBankPage.get();
            case BLOCK_BANK -> finalBlockBankPage.get();
            case BOOKSHELF -> finalBookshelfPage.get();
            case CHARACTER_BANK -> finalCharacterBankPages
                    .get()
                    .getOrDefault(Models.Character.getId(), MAX_CHARACTER_BANK_PAGES);
            case MISC_BUCKET -> finalMiscBucketPage.get();
            default -> 1;
        };
    }

    public void updateFinalPage() {
        switch (currentContainer) {
            case ACCOUNT_BANK -> {
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
            case CHARACTER_BANK -> {
                finalCharacterBankPages.get().put(Models.Character.getId(), currentPage);
                finalCharacterBankPages.touched();
            }
            case MISC_BUCKET -> {
                finalMiscBucketPage.store(currentPage);
            }
        }
    }

    public InteractiveContainerType getCurrentContainer() {
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

    private Map<Integer, String> getCurrentNameMap() {
        return switch (currentContainer) {
            case ACCOUNT_BANK -> customBankPageNames.get();
            case BLOCK_BANK -> customBlockBankPageNames.get();
            case BOOKSHELF -> customBookshelfPageNames.get();
            case CHARACTER_BANK -> customCharacterBankPagesNames
                    .get()
                    .getOrDefault(Models.Character.getId(), new TreeMap<>());
            case MISC_BUCKET -> customMiscBucketPageNames.get();
            default -> null;
        };
    }
}
