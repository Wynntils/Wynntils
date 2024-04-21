/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type.wynncontainers;

import com.wynntils.models.containers.type.AbstractWynncraftContainer;
import com.wynntils.models.containers.type.ContainerBounds;
import com.wynntils.models.containers.type.SearchableContainerProperty;
import java.util.regex.Pattern;

public class ScrapMenuContainer extends AbstractWynncraftContainer implements SearchableContainerProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile("Scrap Rewards");
    private static final Pattern NEXT_PAGE_PATTERN = Pattern.compile("§7Next Page");
    private static final Pattern PREVIOUS_PAGE_PATTERN = Pattern.compile("§7Previous Page");

    public ScrapMenuContainer() {
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
        return 8;
    }

    @Override
    public int getPreviousItemSlot() {
        return 0;
    }

    @Override
    public ContainerBounds getBounds() {
        return new ContainerBounds(1, 0, 5, 8);
    }

    @Override
    public boolean supportsAdvancedSearch() {
        return false;
    }
}
