/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type;

import com.wynntils.core.text.StyledText;
import java.util.regex.Pattern;

public enum SearchableContainerType {
    BANK(
            Pattern.compile("§0\\[Pg. (\\d+)\\] §8(.*)'s?§0 Bank"),
            Pattern.compile("§f§lPage \\d+§a >§2>§a>§2>§a>"),
            8,
            new ContainerBounds(0, 0, 5, 6)),
    BLOCK_BANK(
            Pattern.compile("§0\\[Pg. (\\d+)\\] §8(.*)'s?§0 Block Bank"),
            Pattern.compile("§f§lPage \\d+§a >§2>§a>§2>§a>"),
            8,
            new ContainerBounds(0, 0, 5, 6)),
    BOOKSHELF(
            Pattern.compile("§0\\[Pg. (\\d+)\\] §8(.*)'s?§0 Bookshelf"),
            Pattern.compile("§f§lPage \\d+§a >§2>§a>§2>§a>"),
            8,
            new ContainerBounds(0, 0, 5, 6)),
    MISC_BUCKET(
            Pattern.compile("§0\\[Pg. (\\d+)\\] §8(.*)'s?§0 Misc. Bucket"),
            Pattern.compile("§f§lPage \\d+§a >§2>§a>§2>§a>"),
            8,
            new ContainerBounds(0, 0, 5, 6)),
    GUILD_BANK(
            Pattern.compile(".+: Bank \\(.+\\)"),
            Pattern.compile("§a§lNext Page"),
            27,
            new ContainerBounds(0, 2, 4, 8)),
    MEMBER_LIST(Pattern.compile(".+: Members"), Pattern.compile("§a§lNext Page"), 28, new ContainerBounds(0, 2, 4, 8)),
    SCRAP_MENU(Pattern.compile("Scrap Rewards"), Pattern.compile("§7Next Page"), 8, new ContainerBounds(1, 0, 5, 8));

    private final Pattern titlePattern;
    private final Pattern nextItemPattern;
    private final int nextItemSlot;
    private final ContainerBounds bounds;

    SearchableContainerType(Pattern titlePattern, Pattern nextItemPattern, int nextItemSlot, ContainerBounds bounds) {
        this.titlePattern = titlePattern;
        this.nextItemPattern = nextItemPattern;
        this.nextItemSlot = nextItemSlot;
        this.bounds = bounds;
    }

    public int getNextItemSlot() {
        return nextItemSlot;
    }

    public Pattern getNextItemPattern() {
        return nextItemPattern;
    }

    public ContainerBounds getBounds() {
        return bounds;
    }

    public static SearchableContainerType getContainerType(StyledText title) {
        for (SearchableContainerType type : SearchableContainerType.values()) {
            if (title.getMatcher(type.titlePattern).matches()) {
                return type;
            }
        }

        return null;
    }
}
