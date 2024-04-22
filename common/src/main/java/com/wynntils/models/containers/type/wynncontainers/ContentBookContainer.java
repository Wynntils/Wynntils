/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type.wynncontainers;

import com.wynntils.models.containers.type.AbstractWynncraftContainer;
import com.wynntils.models.containers.type.ContainerBounds;
import com.wynntils.models.containers.type.FullscreenContainerProperty;
import com.wynntils.models.containers.type.SearchableContainerProperty;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import java.util.List;
import java.util.regex.Pattern;

public class ContentBookContainer extends AbstractWynncraftContainer
        implements SearchableContainerProperty, FullscreenContainerProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile("§f\uE000\uE072");
    private static final Pattern NEXT_PAGE_PATTERN = Pattern.compile("§7Scroll Down");
    private static final Pattern PREVIOUS_PAGE_PATTERN = Pattern.compile("§7Scroll Up");

    public ContentBookContainer() {
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
        return 69;
    }

    @Override
    public int getPreviousItemSlot() {
        return 65;
    }

    @Override
    public ContainerBounds getBounds() {
        return new ContainerBounds(0, 0, 5, 6);
    }

    @Override
    public List<ItemProviderType> supportedProviderTypes() {
        return List.of();
    }
}
