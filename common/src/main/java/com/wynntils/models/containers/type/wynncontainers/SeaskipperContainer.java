/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type.wynncontainers;

import com.wynntils.models.containers.type.WynncraftContainer;
import java.util.regex.Pattern;

public class SeaskipperContainer extends WynncraftContainer {
    private static final Pattern TITLE_PATTERN = Pattern.compile("V.S.S. Seaskipper");

    public SeaskipperContainer() {
        super(TITLE_PATTERN);
    }
}
