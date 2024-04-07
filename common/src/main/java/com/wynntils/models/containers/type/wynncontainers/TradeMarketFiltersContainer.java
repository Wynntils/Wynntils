/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type.wynncontainers;

import com.wynntils.models.containers.type.ScrollableContainerProperty;
import com.wynntils.models.containers.type.WynncraftContainer;
import java.util.regex.Pattern;

public class TradeMarketFiltersContainer extends WynncraftContainer implements ScrollableContainerProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\\[Pg\\. \\d] Filter Items");
    private static final Pattern NEXT_PAGE_PATTERN = Pattern.compile("§7Forward to §fPage \\d+");
    private static final Pattern PREVIOUS_PAGE_PATTERN = Pattern.compile("§7Back to §fPage \\d+");

    public TradeMarketFiltersContainer() {
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
        return 35;
    }

    @Override
    public int getPreviousItemSlot() {
        return 26;
    }
}
