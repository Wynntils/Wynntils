/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type.wynncontainers;

import com.wynntils.models.containers.type.PersonalStorageContainer;
import com.wynntils.models.containers.type.PersonalStorageType;
import java.util.regex.Pattern;

public class BlockBankContainer extends PersonalStorageContainer {
    public BlockBankContainer() {
        super(Pattern.compile("§0\\[Pg. (\\d+)\\] §8(.*)'s?§0 Block Bank"), PersonalStorageType.BLOCK_BANK);
    }

    @Override
    public boolean supportsAdvancedSearch() {
        return false;
    }
}
