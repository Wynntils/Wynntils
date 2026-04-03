/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import java.util.regex.Pattern;

public class EmeraldPouchContainer extends Container {
    private static final Pattern TITLE_PATTERN = Pattern.compile("EmeraldÀÀÀÀPouchÀ");

    public EmeraldPouchContainer() {
        super(TITLE_PATTERN);
    }
}
