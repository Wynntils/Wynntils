/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import java.util.regex.Pattern;

public class EmotesContainer extends Container {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFF8\uE033\uDAFF\uDF80\uF016");

    public EmotesContainer() {
        super(TITLE_PATTERN);
    }
}
