/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type.wynncontainers;

import com.wynntils.models.containers.type.AbstractWynncraftContainer;
import java.util.regex.Pattern;

public class CharacterInfoContainer extends AbstractWynncraftContainer {
    private static final Pattern TITLE_PATTERN = Pattern.compile("Character Info");

    public CharacterInfoContainer() {
        super(TITLE_PATTERN);
    }
}
