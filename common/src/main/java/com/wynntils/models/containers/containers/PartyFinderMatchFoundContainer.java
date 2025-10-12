/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.FullscreenContainerProperty;
import java.util.regex.Pattern;

public class PartyFinderMatchFoundContainer extends Container implements FullscreenContainerProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFE0[\uE041-\uE051]");

    public PartyFinderMatchFoundContainer() {
        super(TITLE_PATTERN);
    }
}
