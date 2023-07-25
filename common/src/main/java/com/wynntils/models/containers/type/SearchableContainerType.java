/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type;

import com.wynntils.core.text.StyledText;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public enum SearchableContainerType {
    BANK(
            java.util.regex.Pattern.compile("§0\\[Pg. (\\d+)\\] §8(.*)'s?§0 Bank"),
            java.util.regex.Pattern.compile("§f§lPage \\d+§a >§2>§a>§2>§a>"),
            8,
            (x, y) -> x < 7 && y < 6),
    BLOCK_BANK(
            java.util.regex.Pattern.compile("§0\\[Pg. (\\d+)\\] §8(.*)'s?§0 Block Bank"),
            java.util.regex.Pattern.compile("§f§lPage \\d+§a >§2>§a>§2>§a>"),
            8,
            (x, y) -> x < 7 && y < 6),
    BOOKSHELF(
            java.util.regex.Pattern.compile("§0\\[Pg. (\\d+)\\] §8(.*)'s?§0 Bookshelf"),
            java.util.regex.Pattern.compile("§f§lPage \\d+§a >§2>§a>§2>§a>"),
            8,
            (x, y) -> x < 7 && y < 6),
    MISC_BUCKET(
            Pattern.compile("§0\\[Pg. (\\d+)\\] §8(.*)'s?§0 Misc. Bucket"),
            Pattern.compile("§f§lPage \\d+§a >§2>§a>§2>§a>"),
            8,
            (x, y) -> x < 7 && y < 6),
    GUILD_BANK(Pattern.compile(".+: Bank \\(.+\\)"), Pattern.compile("§a§lNext Page"), 27, (x, y) -> x > 1 && y < 6),
    MEMBER_LIST(Pattern.compile(".+: Members"), Pattern.compile("§a§lNext Page"), 28, (x, y) -> x > 1 && y < 6);

    private final Pattern titlePattern;
    private final Pattern nextItemPattern;
    private final int nextItemSlot;
    private final BiPredicate<Integer, Integer> isSearchableSlot;

    SearchableContainerType(
            Pattern titlePattern,
            Pattern nextItemPattern,
            int nextItemSlot,
            BiPredicate<Integer, Integer> isSearchableSlot) {
        this.titlePattern = titlePattern;
        this.nextItemPattern = nextItemPattern;
        this.nextItemSlot = nextItemSlot;
        this.isSearchableSlot = isSearchableSlot;
    }

    public int getNextItemSlot() {
        return nextItemSlot;
    }

    public Pattern getNextItemPattern() {
        return nextItemPattern;
    }

    public static SearchableContainerType getContainerType(StyledText title) {
        for (SearchableContainerType type : SearchableContainerType.values()) {
            if (title.getMatcher(type.titlePattern).matches()) {
                return type;
            }
        }

        return null;
    }

    public Stream<ItemStack> getSearchableItems(AbstractContainerScreen<?> screen) {
        return screen.getMenu().slots.stream()
                // see ChestMenu
                .filter(slot -> isSearchableSlot.test((slot.x - 8) / 18, (slot.y - 18) / 18))
                .map(Slot::getItem);
    }
}
