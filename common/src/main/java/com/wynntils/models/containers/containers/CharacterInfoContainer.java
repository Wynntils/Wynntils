/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import java.util.regex.Pattern;

public class CharacterInfoContainer extends Container {
    private static final Pattern TITLE_PATTERN = Pattern.compile("Character Info");

    public CharacterInfoContainer() {
        super(TITLE_PATTERN);
    }
}
