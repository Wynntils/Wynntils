package com.wynntils.features.ui;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.utils.mc.ComponentUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class TradeMarketBulkSellFeature extends Feature {

    @RegisterConfig
    public Config<Boolean> enableSellAll = new Config<>(true);

    @RegisterConfig
    public Config<Integer> bulkSell1Amount = new Config<>(64);

    @RegisterConfig
    public Config<Integer> bulkSell2Amount = new Config<>(1728);

    @RegisterConfig
    public Config<Integer> bulkSell3Amount = new Config<>(6399);

    @SubscribeEvent
    public void onSellDialogueOpened(ScreenOpenedEvent.Pre e) {
        if (!ComponentUtils.getCoded(e.getScreen().getTitle()).equals("What would you like to sell?")) return;

        // do things here
    }
}
