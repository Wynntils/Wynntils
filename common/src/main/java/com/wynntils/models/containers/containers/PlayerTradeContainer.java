/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import java.util.regex.Pattern;

public class PlayerTradeContainer extends Container {
    // Test in PlayerTradeContainer_TITLE_PATTERN
    private static final Pattern TITLE_PATTERN = Pattern.compile("^\\w{3,16} +(?<=^.{21})\\w{1,16}$");

    public PlayerTradeContainer() {
        super(TITLE_PATTERN);
    }
}
