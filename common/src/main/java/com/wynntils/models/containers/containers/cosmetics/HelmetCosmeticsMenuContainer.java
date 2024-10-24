/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.cosmetics;

import java.util.regex.Pattern;

public class HelmetCosmeticsMenuContainer extends CosmeticContainer {
    private static final Pattern TITLE_PATTERN = Pattern.compile("Helmet Cosmetics");

    public HelmetCosmeticsMenuContainer() {
        super(TITLE_PATTERN);
    }
}
