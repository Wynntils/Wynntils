/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.core.components.Models;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.models.trademarket.type.TradeMarketPriceInfo;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;
import java.util.Optional;

public class TradeAmountStatProvider extends ItemStatProvider<Integer> {
    @Override
    public Optional<Integer> getValue(WynnItem wynnItem) {
        TradeMarketPriceInfo priceInfo = wynnItem.getData().getOrCalculate(WynnItemData.EMERALD_PRICE_KEY, () -> {
            TradeMarketPriceInfo calculatedInfo =
                    Models.TradeMarket.calculateItemPriceInfo(wynnItem.getData().get(WynnItemData.ITEMSTACK_KEY));
            return calculatedInfo;
        });

        if (priceInfo == TradeMarketPriceInfo.EMPTY) {
            return Optional.empty();
        }

        return Optional.of(priceInfo.amount());
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.VALUED);
    }
}
