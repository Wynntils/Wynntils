/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.ContainerBounds;
import com.wynntils.models.containers.type.SearchableContainerProperty;
import com.wynntils.models.store.type.CosmeticItemType;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import java.util.List;
import java.util.regex.Pattern;

public class CosmeticContainer extends Container implements SearchableContainerProperty {
    private static final String COSMETIC_PREVIEW_TITLE_CHARACTER = "\uF029";
    private static final String TITLE_START = "\uDAFF\uDFF8[\uE030-\uE033]\uDAFF\uDF80";
    private static final Pattern NEXT_PAGE_PATTERN = Pattern.compile("§7Next Page");
    private static final Pattern PREVIOUS_PAGE_PATTERN = Pattern.compile("§7Previous Page");

    private CosmeticContainer(String titleCharacter) {
        super(Pattern.compile(TITLE_START + titleCharacter));
    }

    public CosmeticContainer(CosmeticItemType cosmeticItemType) {
        this(cosmeticItemType.getTitleCharacter());
    }

    public CosmeticContainer() {
        this(COSMETIC_PREVIEW_TITLE_CHARACTER);
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

    @Override
    public int renderYOffset() {
        return 20;
    }
}
