/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.cosmetics;

import java.util.regex.Pattern;

public class PetMenuContainer extends CosmeticContainer {
    private static final Pattern TITLE_PATTERN = Pattern.compile("Pet Menu");
    private static final Pattern NEXT_PAGE_PATTERN = Pattern.compile("§f§lPage \\d+§a >§2>§a>§2>§a>");
    private static final Pattern PREVIOUS_PAGE_PATTERN = Pattern.compile("§f§lPage \\d+§a <§2<§a<§2<§a<");

    public PetMenuContainer() {
        super(TITLE_PATTERN);
    }

    @Override
    public Pattern getNextItemPattern() {
        return NEXT_PAGE_PATTERN;
    }

    @Override
    public Pattern getPreviousItemPattern() {
        return PREVIOUS_PAGE_PATTERN;
    }
}
