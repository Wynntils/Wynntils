/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.ContainerBounds;
import com.wynntils.models.containers.type.FullscreenContainerProperty;
import com.wynntils.models.containers.type.ScrollableContainerProperty;
import java.util.List;
import java.util.regex.Pattern;

public class AspectsContainer extends Container implements ScrollableContainerProperty, FullscreenContainerProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFEA\uE002");
    private static final Pattern NEXT_PAGE_PATTERN = Pattern.compile("§7Next Page");
    private static final Pattern PREVIOUS_PAGE_PATTERN = Pattern.compile("§7Previous Page");

    // Ordered from first equippable to last equippable slot
    private static final List<Integer> EQUIPPED_SLOTS = List.of(18, 11, 4, 15, 26);

    public AspectsContainer() {
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

    public static List<Integer> getEquippedSlots() {
        return EQUIPPED_SLOTS;
    }

    public static ContainerBounds getAspectBounds() {
        return new ContainerBounds(4, 0, 5, 8);
    }
}
