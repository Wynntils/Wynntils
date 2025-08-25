/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.ContainerBounds;
import com.wynntils.models.containers.type.SearchableContainerProperty;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import java.util.List;
import java.util.regex.Pattern;

public class ScrapMenuContainer extends Container implements SearchableContainerProperty {
    private static final Pattern TITLE_PATTERN =
            Pattern.compile("\uDAFF\uDFE8\uE037\uDAFF\uDF42\uF024\uDAFF\uDF5B\uF030");
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
        return 53;
    }

    @Override
    public int getPreviousItemSlot() {
        return 51;
    }

    @Override
    public ContainerBounds getBounds() {
        return new ContainerBounds(2, 0, 4, 8);
    }

    @Override
    public List<ItemProviderType> supportedProviderTypes() {
        return List.of();
    }
}
