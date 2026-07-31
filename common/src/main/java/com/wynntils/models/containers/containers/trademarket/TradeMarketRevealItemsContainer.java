/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.trademarket;

import com.wynntils.models.containers.Container;
import java.util.regex.Pattern;

public class TradeMarketRevealItemsContainer extends Container {
    private static final Pattern TITLE_PATTERN = Pattern.compile("What would you like to reveal\\?");

    public TradeMarketRevealItemsContainer() {
        super(TITLE_PATTERN);
    }
}
