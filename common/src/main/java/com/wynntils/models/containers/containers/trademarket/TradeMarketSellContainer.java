/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.trademarket;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.BoundedContainerProperty;
import com.wynntils.models.containers.type.ContainerBounds;
import java.util.regex.Pattern;

public class TradeMarketSellContainer extends Container implements BoundedContainerProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFE8\uE014(.+)?");

    public TradeMarketSellContainer() {
        super(TITLE_PATTERN);
    }

    @Override
    public ContainerBounds getBounds() {
        // singular slot where the item to be sold is (slot 22)
        return new ContainerBounds(2, 4, 2, 4);
    }
}
