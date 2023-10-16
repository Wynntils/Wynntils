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
import net.minecraft.world.item.ItemStack;

public class PriceStatProvider extends ItemStatProvider<Integer> {
    @Override
    public List<Integer> getValue(ItemStack itemStack, WynnItem wynnItem) {
        TradeMarketPriceInfo priceInfo = wynnItem.getData().getOrCalculate(WynnItemData.EMERALD_PRICE_KEY, () -> {
            TradeMarketPriceInfo calculatedInfo = Models.TradeMarket.calculateItemPriceInfo(itemStack);
            return calculatedInfo;
        });

        if (priceInfo == TradeMarketPriceInfo.EMPTY) {
            return List.of();
        }

        // Silverbull price is the normal price if the item is not discounted
        return List.of(priceInfo.silverbullPrice());
    }
}
