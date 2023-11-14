/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.containers.type.SearchableContainerType;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class ContainerModel extends Model {
    // Test suite: https://regexr.com/7jh15
    public static final Pattern ABILITY_TREE_PATTERN =
            Pattern.compile("(?:Warrior|Shaman|Mage|Assassin|Archer) Abilities");

    // Test suite: https://regexr.com/7b4lf
    private static final Pattern GUILD_BANK_PATTERN =
            Pattern.compile("[a-zA-Z ]+: Bank \\((?:Everyone|High Ranked)\\)");

    // Test suite: https://regexr.com/7jh1e
    private static final Pattern LOOT_CHEST_PATTERN = Pattern.compile("Loot Chest (§.)\\[.+\\]");

    // Test suite: https://regexr.com/7hcl7
    private static final Pattern PERSONAL_STORAGE_PATTERN =
            Pattern.compile("^§0\\[Pg\\. (\\d+)\\] §8[a-zA-Z0-9_ ]+'s?§0 (.*)$");

    // Test suite: https://regexr.com/7jh0s
    private static final Pattern TRADE_MARKET_FILTER_TITLE = Pattern.compile("\\[Pg\\. \\d] Filter Items");

    private static final String ACCOUNT_BANK_NAME = "Account Bank";
    private static final String BLOCK_BANK_NAME = "Block Bank";
    private static final String BOOKSHELF_NAME = "Bookshelf";
    private static final String CHARACTER_BANK_NAME = "Character Bank";
    private static final String MISC_BUCKET_NAME = "Misc. Bucket";
    public static final String CHARACTER_INFO_NAME = "Character Info";
    public static final String COSMETICS_MENU_NAME = "Crates, Bombs & Cosmetics";

    private static final Pair<Integer, Integer> ABILITY_TREE_PREVIOUS_NEXT_SLOTS = new Pair<>(57, 59);
    private static final Pair<Integer, Integer> BANK_PREVIOUS_NEXT_SLOTS = new Pair<>(17, 8);
    private static final Pair<Integer, Integer> CONTENT_BOOK_PREVIOUS_NEXT_SLOTS = new Pair<>(65, 69);
    private static final Pair<Integer, Integer> GUILD_BANK_PREVIOUS_NEXT_SLOTS = new Pair<>(9, 27);
    private static final Pair<Integer, Integer> GUILD_MEMBER_LIST_PREVIOUS_NEXT_SLOTS = new Pair<>(10, 28);
    private static final Pair<Integer, Integer> JUKEBOX_PREVIOUS_NEXT_SLOTS = new Pair<>(17, 8);
    private static final Pair<Integer, Integer> LOBBY_PREVIOUS_NEXT_SLOTS = new Pair<>(36, 44);
    private static final Pair<Integer, Integer> SCRAP_MENU_PREVIOUS_NEXT_SLOTS = new Pair<>(0, 8);
    private static final Pair<Integer, Integer> TRADE_MARKET_PREVIOUS_NEXT_SLOTS = new Pair<>(17, 26);
    private static final Pair<Integer, Integer> TRADE_MARKET_SECONDARY_PREVIOUS_NEXT_SLOTS = new Pair<>(26, 35);
    private static final StyledText CONTENT_BOOK_TITLE = StyledText.fromString("§f\uE000\uE072");
    private static final StyledText FIRST_TRADE_MARKET_PAGE_STRING = StyledText.fromString("§bReveal Item Names");
    private static final StyledText JUKEBOX_NAME = StyledText.fromString("Player's Jukebox");
    private static final StyledText LAST_BANK_PAGE_STRING = StyledText.fromString(">§4>§c>§4>§c>");
    private static final StyledText LOBBY_TITLE = StyledText.fromString("Wynncraft Servers");
    private static final StyledText SCRAP_MENU_TITLE = StyledText.fromString("Scrap Rewards");
    private static final StyledText SEASKIPPER_TITLE = StyledText.fromString("V.S.S. Seaskipper");
    private static final StyledText TRADE_MARKET_SEARCH_TITLE = StyledText.fromString("Search Results");
    private static final StyledText TRADE_MARKET_TITLE = StyledText.fromString("Trade Market");

    public static final int LAST_BANK_PAGE_SLOT = 8;

    public ContainerModel() {
        super(List.of());
    }

    public boolean isAbilityTreeScreen(Screen screen) {
        return ABILITY_TREE_PATTERN.matcher(screen.getTitle().getString()).matches();
    }

    public boolean isAccountBankScreen(Screen screen) {
        Matcher matcher = StyledText.fromComponent(screen.getTitle()).getMatcher(PERSONAL_STORAGE_PATTERN);
        if (!matcher.matches()) return false;

        String type = matcher.group(2);
        return type.equals(ACCOUNT_BANK_NAME);
    }

    public boolean isCharacterBankScreen(Screen screen) {
        Matcher matcher = StyledText.fromComponent(screen.getTitle()).getMatcher(PERSONAL_STORAGE_PATTERN);
        if (!matcher.matches()) return false;

        String type = matcher.group(2);
        return type.equals(CHARACTER_BANK_NAME);
    }

    public int getCurrentBankPage(Screen screen) {
        Matcher matcher = StyledText.fromComponent(screen.getTitle()).getMatcher(PERSONAL_STORAGE_PATTERN);
        if (!matcher.matches()) return 0;

        return Integer.parseInt(matcher.group(1));
    }

    /**
     * @return True if the page is the last page in a Bank, Block Bank, or Misc Bucket
     */
    public boolean isLastBankPage(Screen screen) {
        return (isAccountBankScreen(screen)
                        || isBlockBankScreen(screen)
                        || isBlockBankScreen(screen)
                        || isBookshelfScreen(screen)
                        || isCharacterBankScreen(screen)
                        || isMiscBucketScreen(screen))
                && screen instanceof ContainerScreen cs
                && isItemIndicatingLastBankPage(
                        cs.getMenu().getSlot(LAST_BANK_PAGE_SLOT).getItem());
    }

    public boolean isItemIndicatingLastBankPage(ItemStack item) {
        return StyledText.fromComponent(item.getHoverName()).endsWith(LAST_BANK_PAGE_STRING)
                || item.getHoverName().getString().equals(" ");
    }

    public boolean isGuildBankScreen(Screen screen) {
        return StyledText.fromComponent(screen.getTitle()).matches(GUILD_BANK_PATTERN);
    }

    public boolean isGuildMemberListScreen(Screen screen) {
        return StyledText.fromComponent(screen.getTitle())
                .matches(SearchableContainerType.MEMBER_LIST.getTitlePattern());
    }

    public boolean isJukeboxScreen(Screen screen) {
        if (!(screen instanceof ContainerScreen cs)) return false;
        if (cs.getMenu().getRowCount() != 6) return false;

        return StyledText.fromComponent(cs.getTitle()).equals(JUKEBOX_NAME);
    }

    public boolean isTradeMarketScreen(Screen screen) {
        if (!(screen instanceof ContainerScreen cs)) return false;
        if (cs.getMenu().getRowCount() != 6) return false;

        return StyledText.fromComponent(cs.getTitle()).equals(TRADE_MARKET_TITLE);
    }

    public boolean isSecondaryTradeMarketScreen(Screen screen) {
        if (!(screen instanceof ContainerScreen cs)) return false;
        if (cs.getMenu().getRowCount() != 6) return false;

        return StyledText.fromComponent(cs.getTitle()).matches(TRADE_MARKET_FILTER_TITLE)
                || StyledText.fromComponent(cs.getTitle()).equals(TRADE_MARKET_SEARCH_TITLE);
    }

    public boolean isFirstTradeMarketPage(Screen screen) {
        return isTradeMarketScreen(screen)
                && screen instanceof ContainerScreen cs
                && StyledText.fromComponent(cs.getMenu().getSlot(17).getItem().getHoverName())
                        .equals(FIRST_TRADE_MARKET_PAGE_STRING);
    }

    public boolean isBlockBankScreen(Screen screen) {
        Matcher matcher = StyledText.fromComponent(screen.getTitle()).getMatcher(PERSONAL_STORAGE_PATTERN);
        if (!matcher.matches()) return false;

        String type = matcher.group(2);
        return type.equals(BLOCK_BANK_NAME);
    }

    public boolean isBookshelfScreen(Screen screen) {
        Matcher matcher = StyledText.fromComponent(screen.getTitle()).getMatcher(PERSONAL_STORAGE_PATTERN);
        if (!matcher.matches()) return false;

        String type = matcher.group(2);
        return type.equals(BOOKSHELF_NAME);
    }

    public boolean isMiscBucketScreen(Screen screen) {
        Matcher matcher = StyledText.fromComponent(screen.getTitle()).getMatcher(PERSONAL_STORAGE_PATTERN);
        if (!matcher.matches()) return false;

        String type = matcher.group(2);
        return type.equals(MISC_BUCKET_NAME);
    }

    public boolean isScrapMenuScreen(Screen screen) {
        if (!(screen instanceof ContainerScreen cs)) return false;
        return cs.getMenu().getRowCount() == 6
                && StyledText.fromComponent(screen.getTitle()).equals(SCRAP_MENU_TITLE);
    }

    public boolean isContentBook(Screen screen) {
        if (!(screen instanceof ContainerScreen cs)) return false;
        return StyledText.fromComponent(cs.getTitle()).equals(CONTENT_BOOK_TITLE);
    }

    public boolean isLobbyScreen(Screen screen) {
        if (!(screen instanceof ContainerScreen cs)) return false;
        return StyledText.fromComponent(cs.getTitle()).equals(LOBBY_TITLE);
    }

    public boolean isLootChest(Screen screen) {
        return screen instanceof ContainerScreen && lootChestMatcher(screen).matches();
    }

    public boolean isLootChest(String title) {
        return title.startsWith("Loot Chest");
    }

    public boolean isRewardChest(String title) {
        return title.startsWith("Daily Rewards")
                || title.contains("Objective Rewards")
                || title.contains("Challenge Rewards")
                || title.contains("Flying Chest");
    }

    public boolean isLootOrRewardChest(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?>)) return false;

        String title = screen.getTitle().getString();
        return isLootOrRewardChest(title);
    }

    public boolean isLootOrRewardChest(String title) {
        return isLootChest(title) || isRewardChest(title);
    }

    public boolean isSeaskipper(Component component) {
        return StyledText.fromComponent(component).equals(SEASKIPPER_TITLE);
    }

    public Matcher lootChestMatcher(Screen screen) {
        return StyledText.fromComponent(screen.getTitle()).getNormalized().getMatcher(LOOT_CHEST_PATTERN);
    }

    public Optional<Integer> getScrollSlot(AbstractContainerScreen<?> gui, boolean scrollUp) {
        Pair<Integer, Integer> slots = getScrollSlots(gui, scrollUp);
        if (slots == null) return Optional.empty();

        return Optional.of(scrollUp ? slots.a() : slots.b());
    }

    private Pair<Integer, Integer> getScrollSlots(AbstractContainerScreen<?> gui, boolean scrollUp) {
        if (isAbilityTreeScreen(gui)) {
            return ABILITY_TREE_PREVIOUS_NEXT_SLOTS;
        }

        if (isAccountBankScreen(gui)
                || isBlockBankScreen(gui)
                || isBookshelfScreen(gui)
                || isCharacterBankScreen(gui)
                || isMiscBucketScreen(gui)) {
            if (!scrollUp && isLastBankPage(gui)) return null;

            return BANK_PREVIOUS_NEXT_SLOTS;
        }

        if (isGuildBankScreen(gui)) {
            return GUILD_BANK_PREVIOUS_NEXT_SLOTS;
        }

        if (isGuildMemberListScreen(gui)) {
            return GUILD_MEMBER_LIST_PREVIOUS_NEXT_SLOTS;
        }

        if (isTradeMarketScreen(gui)) {
            if (scrollUp && isFirstTradeMarketPage(gui)) return null;

            return TRADE_MARKET_PREVIOUS_NEXT_SLOTS;
        }

        if (isSecondaryTradeMarketScreen(gui)) {
            return TRADE_MARKET_SECONDARY_PREVIOUS_NEXT_SLOTS;
        }

        if (isScrapMenuScreen(gui)) {
            return SCRAP_MENU_PREVIOUS_NEXT_SLOTS;
        }

        if (isContentBook(gui)) {
            return CONTENT_BOOK_PREVIOUS_NEXT_SLOTS;
        }

        if (isLobbyScreen(gui)) {
            return LOBBY_PREVIOUS_NEXT_SLOTS;
        }

        return null;
    }
}
