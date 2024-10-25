/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.cosmetics;

import java.util.regex.Pattern;

public class PlayerEffectsMenuContainer extends CosmeticContainer {
    private static final Pattern TITLE_PATTERN = Pattern.compile("Player Effects Menu");

    public PlayerEffectsMenuContainer() {
        super(TITLE_PATTERN);
    }
}
