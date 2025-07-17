/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.lootrun.type.LootrunLocation;
import java.util.regex.Pattern;

public class LootrunChestContainer extends Container {
    private static final String TITLE_PATTERN = "^%s$";

    private final LootrunLocation location;

    public LootrunChestContainer(LootrunLocation location) {
        super(Pattern.compile(String.format(TITLE_PATTERN, Pattern.quote(location.getContainerTitle()))));

        this.location = location;
    }
}
