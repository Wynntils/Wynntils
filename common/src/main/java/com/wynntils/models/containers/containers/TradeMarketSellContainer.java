/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import java.util.regex.Pattern;

public class TradeMarketSellContainer extends Container {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFE8\uE014.+");

    public TradeMarketSellContainer() {
        super(TITLE_PATTERN);
    }
}
