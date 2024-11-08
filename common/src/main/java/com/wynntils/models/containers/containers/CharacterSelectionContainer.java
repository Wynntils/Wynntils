/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.FullscreenContainerProperty;
import java.util.regex.Pattern;

public class CharacterSelectionContainer extends Container implements FullscreenContainerProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFD5\uE01F");

    public CharacterSelectionContainer() {
        super(TITLE_PATTERN);
    }
}
