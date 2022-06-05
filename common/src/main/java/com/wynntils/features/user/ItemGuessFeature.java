/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.config.properties.ConfigOption;
import com.wynntils.core.config.properties.ConfigurableInfo;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.core.webapi.WebManager;

@FeatureInfo(stability = Stability.STABLE)
@ConfigurableInfo(category = "Item Tooltips")
public class ItemGuessFeature extends UserFeature {

    @ConfigOption(displayName = "Show Guess Price")
    public static boolean showGuessesPrice = true;

    @Override
    public void onInit(ImmutableList.Builder<Condition> conditions) {
        conditions.add(new WebLoadedCondition());
    }

    @Override
    protected boolean onEnable() {
        return WebManager.isItemGuessesLoaded() || WebManager.tryLoadItemGuesses();
    }
}
