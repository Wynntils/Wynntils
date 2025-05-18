/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.trademarket;

import com.wynntils.models.containers.Container;
import java.util.regex.Pattern;

public class TradeMarketTradesContainer extends Container {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFE8\uE013");

    public TradeMarketTradesContainer() {
        super(TITLE_PATTERN);
    }
}
