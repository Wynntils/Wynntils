/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.personal;

import com.wynntils.models.containers.type.PersonalStorageType;
import java.util.regex.Pattern;

public class BookshelfContainer extends PersonalStorageContainer {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFF0\uE00F\uDAFF\uDF68\uF005");

    public BookshelfContainer() {
        super(TITLE_PATTERN, PersonalStorageType.BOOKSHELF);
    }
}
