/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.utils.StringUtils;

public class TradeMarketIdentificationFilterItem extends GuiItem {
    private final String statName;
    private final String initials;

    public TradeMarketIdentificationFilterItem(String statName) {
        this.statName = statName;

        this.initials = StringUtils.getAbbreviation(statName);
    }

    public String getStatName() {
        return statName;
    }

    public String getInitials() {
        return initials;
    }
}
