/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type.wynncontainers;

import com.wynntils.models.containers.type.AbstractWynncraftContainer;
import com.wynntils.models.containers.type.ContainerBounds;
import com.wynntils.models.containers.type.SearchableContainerProperty;
import java.util.regex.Pattern;

public class GuildMemberListContainer extends AbstractWynncraftContainer implements SearchableContainerProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile(".+: Members");
    private static final Pattern NEXT_PAGE_PATTERN = Pattern.compile("§a§lNext Page");
    private static final Pattern PREVIOUS_PAGE_PATTERN = Pattern.compile("§a§lPrevious Page");

    public GuildMemberListContainer() {
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
        return 28;
    }

    @Override
    public int getPreviousItemSlot() {
        return 10;
    }

    @Override
    public ContainerBounds getBounds() {
        return new ContainerBounds(0, 2, 4, 8);
    }

    @Override
    public boolean supportsAdvancedSearch() {
        return false;
    }
}
