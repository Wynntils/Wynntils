/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.BoundedContainerProperty;
import com.wynntils.models.containers.type.ContainerBounds;
import com.wynntils.models.containers.type.ScrollableContainerProperty;
import java.util.regex.Pattern;

public class BlacksmithRepairContainer extends Container
        implements BoundedContainerProperty, ScrollableContainerProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFF8\uE017");
    private static final Pattern NEXT_PAGE_PATTERN = Pattern.compile("§7Next Page");
    private static final Pattern PREVIOUS_PAGE_PATTERN = Pattern.compile("§7Previous Page");

    public BlacksmithRepairContainer() {
        super(TITLE_PATTERN);
    }

    @Override
    public ContainerBounds getBounds() {
        return new ContainerBounds(1, 2, 1, 6);
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
        return 23;
    }

    @Override
    public int getPreviousItemSlot() {
        return 21;
    }
}
