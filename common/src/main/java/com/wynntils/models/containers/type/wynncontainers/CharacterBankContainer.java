/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type.wynncontainers;

import com.wynntils.models.containers.type.PersonalStorageContainer;
import com.wynntils.models.containers.type.PersonalStorageType;
import java.util.regex.Pattern;

public class CharacterBankContainer extends PersonalStorageContainer {
    private static final Pattern TITLE_PATTERN = Pattern.compile("§0\\[Pg. (\\d+)\\] §8(.*)'s?§0 Character Bank");

    public CharacterBankContainer() {
        super(TITLE_PATTERN, PersonalStorageType.CHARACTER_BANK);
    }
}
