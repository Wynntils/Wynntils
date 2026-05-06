/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import java.util.regex.Pattern;

public class StoreContainer extends Container {
    private static Pattern TITLE_PATTERN = Pattern.compile(
            "\uDAFF\uDFF4\uE02C\uDAFF\uDF7C\uF027\uDAFF\uDF52\uDB00\uDC3D.\uDAFF\uDF22\uDB00\uDC40.\uDAFF\uDF2F");

    public StoreContainer() {
        super(TITLE_PATTERN);
    }
}
