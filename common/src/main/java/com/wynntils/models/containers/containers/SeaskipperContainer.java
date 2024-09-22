/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import java.util.regex.Pattern;

public class SeaskipperContainer extends Container {
    private static final Pattern TITLE_PATTERN = Pattern.compile("V.S.S. Seaskipper");

    public SeaskipperContainer() {
        super(TITLE_PATTERN);
    }
}
