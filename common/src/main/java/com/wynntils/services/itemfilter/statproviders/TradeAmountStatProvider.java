/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.core.components.Models;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.models.trademarket.type.TradeMarketPriceInfo;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;

public class TradeAmountStatProvider extends ItemStatProvider<Integer> {
    @Override
    public List<Integer> getValue(WynnItem wynnItem) {
        TradeMarketPriceInfo priceInfo = wynnItem.getData().getOrCalculate(WynnItemData.EMERALD_PRICE_KEY, () -> {
            TradeMarketPriceInfo calculatedInfo =
                    Models.TradeMarket.calculateItemPriceInfo(wynnItem.getData().get(WynnItemData.ITEMSTACK_KEY));
            return calculatedInfo;
        });

        if (priceInfo == TradeMarketPriceInfo.EMPTY) {
            return List.of();
        }

        return List.of(priceInfo.amount());
    }
}
