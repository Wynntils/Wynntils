/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.personal;

import com.wynntils.models.containers.type.PersonalStorageType;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import java.util.List;
import java.util.regex.Pattern;

public class BlockBankContainer extends PersonalStorageContainer {
    private static final Pattern TITLE_PATTERN = Pattern.compile("§0\\[Pg. (\\d+)\\] §8(.*)'s?§0 Block Bank");

    public BlockBankContainer() {
        super(TITLE_PATTERN, PersonalStorageType.BLOCK_BANK);
    }

    @Override
    public List<ItemProviderType> supportedProviderTypes() {
        return List.of();
    }
}
