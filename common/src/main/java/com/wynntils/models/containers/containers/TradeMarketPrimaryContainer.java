/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.ContainerBounds;
import com.wynntils.models.containers.type.HighlightableProfessionProperty;
import com.wynntils.models.containers.type.ScrollableContainerProperty;
import java.util.regex.Pattern;

public class TradeMarketPrimaryContainer extends Container
        implements ScrollableContainerProperty, HighlightableProfessionProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile("Trade Market");
    private static final Pattern NEXT_PAGE_PATTERN = Pattern.compile("§f§lPage \\d+§a >§2>§a>§2>§a>");
    private static final Pattern PREVIOUS_PAGE_PATTERN = Pattern.compile("§f§lPage \\d+§a <§2<§a<§2<§a<");

    public TradeMarketPrimaryContainer() {
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
        return 26;
    }

    @Override
    public int getPreviousItemSlot() {
        return 17;
    }

    @Override
    public ContainerBounds getBounds() {
        return new ContainerBounds(0, 0, 5, 6);
    }
}
