/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.ScrollableContainerProperty;
import com.wynntils.models.store.type.StoreItemType;
import java.util.regex.Pattern;

public class StoreContainer extends Container implements ScrollableContainerProperty {
    private static final String TITLE_START = "\uDAFF\uDFF4\uE02D\uDAFF\uDF7C";
    private static final Pattern NEXT_PAGE_PATTERN = Pattern.compile("§7Next Page");
    private static final Pattern PREVIOUS_PAGE_PATTERN = Pattern.compile("§7Previous Page");

    public StoreContainer(StoreItemType storeItemType) {
        super(Pattern.compile(TITLE_START + storeItemType.getTitleCharacter() + ".*"));
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
        return 53;
    }

    @Override
    public int getPreviousItemSlot() {
        return 51;
    }
}
