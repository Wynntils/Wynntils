/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type.wynncontainers;

import com.wynntils.models.containers.type.PersonalStorageContainer;
import com.wynntils.models.containers.type.PersonalStorageType;
import java.util.regex.Pattern;

public class AccountBankContainer extends PersonalStorageContainer {
    public AccountBankContainer() {
        super(Pattern.compile("§0\\[Pg. (\\d+)\\] §8(.*)'s?§0 Account Bank"), PersonalStorageType.ACCOUNT_BANK);
    }
}
