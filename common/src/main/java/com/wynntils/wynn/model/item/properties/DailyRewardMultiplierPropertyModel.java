/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item.properties;

import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.DailyRewardMultiplierProperty;
import com.wynntils.wynn.model.item.ItemStackTransformManager;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemPropertyWriter;

public class DailyRewardMultiplierPropertyModel extends Model {
    private static final ItemPropertyWriter DAILY_REWARD_MULTIPLIER_PROPERTY_WRITER =
            new ItemPropertyWriter(WynnItemMatchers::isDailyRewardsChest, DailyRewardMultiplierProperty::new);

    public static void init() {
        ItemStackTransformManager.registerProperty(DAILY_REWARD_MULTIPLIER_PROPERTY_WRITER);
    }

    public static void disable() {
        ItemStackTransformManager.unregisterProperty(DAILY_REWARD_MULTIPLIER_PROPERTY_WRITER);
    }
}
