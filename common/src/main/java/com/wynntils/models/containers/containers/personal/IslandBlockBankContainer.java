/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.personal;

import com.wynntils.models.containers.type.PersonalStorageType;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import java.util.List;
import java.util.regex.Pattern;

public class IslandBlockBankContainer extends PersonalStorageContainer {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFF0\uE00F\uDAFF\uDF68\uF003");

    public IslandBlockBankContainer() {
        super(TITLE_PATTERN, PersonalStorageType.BLOCK_BANK, 12, List.of(1, 3, 5, 7, 9, 11));
    }

    @Override
    public List<ItemProviderType> supportedProviderTypes() {
        return List.of();
    }
}
