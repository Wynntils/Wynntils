/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.personal;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.ContainerBounds;
import com.wynntils.models.containers.type.PersonalStorageType;
import com.wynntils.models.containers.type.SearchableContainerProperty;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import java.util.List;
import java.util.regex.Pattern;

public abstract class PersonalStorageContainer extends Container implements SearchableContainerProperty {
    private static final Pattern NEXT_PAGE_PATTERN = Pattern.compile("§f§lPage \\d+§a >§2>§a>§2>§a>");
    private static final Pattern PREVIOUS_PAGE_PATTERN = Pattern.compile("§f§lPage \\d+§a <§2<§a<§2<§a<");

    private final PersonalStorageType personalStorageType;

    protected PersonalStorageContainer(Pattern titlePattern, PersonalStorageType storageType) {
        super(titlePattern);

        this.personalStorageType = storageType;
    }

    public PersonalStorageType getPersonalStorageType() {
        return personalStorageType;
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
        return 17;
    }

    @Override
    public ContainerBounds getBounds() {
        return new ContainerBounds(0, 0, 5, 6);
    }

    @Override
    public List<ItemProviderType> supportedProviderTypes() {
        return ItemProviderType.normalTypes();
    }
}
