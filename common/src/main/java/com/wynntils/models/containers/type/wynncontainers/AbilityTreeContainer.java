/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type.wynncontainers;

import com.wynntils.models.containers.type.FullscreenContainerProperty;
import com.wynntils.models.containers.type.ScrollableContainerProperty;
import com.wynntils.models.containers.type.WynncraftContainer;
import java.util.regex.Pattern;

public class AbilityTreeContainer extends WynncraftContainer
        implements ScrollableContainerProperty, FullscreenContainerProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile("(?:Warrior|Shaman|Mage|Assassin|Archer) Abilities");
    private static final Pattern NEXT_PAGE_PATTERN = Pattern.compile("§7Next Page");
    private static final Pattern PREVIOUS_PAGE_PATTERN = Pattern.compile("§7Previous Page");

    public AbilityTreeContainer() {
        super(TITLE_PATTERN);
    }

    @Override
    public Pattern getNextItemPattern() {
        return NEXT_PAGE_PATTERN;
    }

    @Override
    public Pattern getPreviousItemPattern() {
        return PREVIOUS_PAGE_PATTERN;
    }

    @Override
    public int getNextItemSlot() {
        return 59;
    }

    @Override
    public int getPreviousItemSlot() {
        return 57;
    }
}
