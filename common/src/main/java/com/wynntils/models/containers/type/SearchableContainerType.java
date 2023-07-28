/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;

public enum SearchableContainerType {
    BANK(
            Pattern.compile("§0\\[Pg. (\\d+)\\] §8(.*)'s?§0 Bank"),
            Pattern.compile("§f§lPage \\d+§a >§2>§a>§2>§a>"),
            8,
            slotPos -> slotPos.x() < 7 && slotPos.y() < 6),
    BLOCK_BANK(
            Pattern.compile("§0\\[Pg. (\\d+)\\] §8(.*)'s?§0 Block Bank"),
            Pattern.compile("§f§lPage \\d+§a >§2>§a>§2>§a>"),
            8,
            slotPos -> slotPos.x() < 7 && slotPos.y() < 6),
    BOOKSHELF(
            Pattern.compile("§0\\[Pg. (\\d+)\\] §8(.*)'s?§0 Bookshelf"),
            Pattern.compile("§f§lPage \\d+§a >§2>§a>§2>§a>"),
            8,
            slotPos -> slotPos.x() < 7 && slotPos.y() < 6),
    MISC_BUCKET(
            Pattern.compile("§0\\[Pg. (\\d+)\\] §8(.*)'s?§0 Misc. Bucket"),
            Pattern.compile("§f§lPage \\d+§a >§2>§a>§2>§a>"),
            8,
            slotPos -> slotPos.x() < 7 && slotPos.y() < 6),
    GUILD_BANK(
            Pattern.compile(".+: Bank \\(.+\\)"),
            Pattern.compile("§a§lNext Page"),
            27,
            slotPos -> slotPos.x() > 1 && slotPos.y() < 6),
    MEMBER_LIST(
            Pattern.compile(".+: Members"),
            Pattern.compile("§a§lNext Page"),
            28,
            slotPos -> slotPos.x() > 1 && slotPos.y() < 6);

    private final Pattern titlePattern;
    private final Pattern nextItemPattern;
    private final int nextItemSlot;
    private final Predicate<ContainerUtils.SlotPos> isSearchableSlot;

    SearchableContainerType(
            Pattern titlePattern,
            Pattern nextItemPattern,
            int nextItemSlot,
            Predicate<ContainerUtils.SlotPos> isSearchableSlot) {
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

    public Stream<ItemStack> getSearchableItems(ChestMenu chestMenu) {
        return ContainerUtils.getSlots(chestMenu).filter(isSearchableSlot).map(ContainerUtils.SlotPos::itemStack);
    }
}
