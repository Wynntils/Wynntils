/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.trademarket;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import java.util.List;
import java.util.regex.Pattern;

public class TradeMarketModel extends Model {
    private static final Pattern[] ITEM_NAME_PATTERNS = {
        // Item on the create buy order menu or create sell offer menu
        Pattern.compile("^§6(?:Buying|Selling) [^ ]+ (.+?)(?:§6)? for .+ Each$"),
        // Items on the trade overview menu
        Pattern.compile("^§6(?:Buying|Selling) [^ ]+ (.+)$"),
        // Item on the view existing sell offer menu (on the right side)
        Pattern.compile("^§7§l[^ ]+x (.+)$")
    };

    public TradeMarketModel() {
        super(List.of());

        Handlers.Item.addSimplifiablePatterns(ITEM_NAME_PATTERNS);
    }
}
