/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.lootrun.type.LootrunLocation;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LootrunChestContainer extends Container {
    private static final Pattern TITLE_PATTERN = createTitlePattern();

    public LootrunChestContainer() {
        super(TITLE_PATTERN);
    }

    private static Pattern createTitlePattern() {
        String patternString = Arrays.stream(LootrunLocation.values())
                .filter(location -> location.getContainerTitle() != null)
                .map(location -> Pattern.quote(location.getContainerTitle()))
                .collect(Collectors.joining("|", "^(", ")$"));
        return Pattern.compile(patternString);
    }
}
