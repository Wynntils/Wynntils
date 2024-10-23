/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.cosmetics;

import java.util.regex.Pattern;

public class WeaponCosmeticsMenuContainer extends CosmeticContainer {
    private static final Pattern TITLE_PATTERN = Pattern.compile("Weapon Cosmetics");

    public WeaponCosmeticsMenuContainer() {
        super(TITLE_PATTERN);
    }
}
