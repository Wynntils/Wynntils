/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.tooltips;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.core.managers.Model;
import com.wynntils.wc.item.ItemStackTransformModel;

@FeatureInfo(stability = Stability.STABLE, category = "Item Tooltips")
public class ItemGuessFeature extends UserFeature {
    private static ItemGuessFeature INSTANCE;

    @Config
    public static boolean showGuessesPrice = true;

    @Override
    public void onInit(
            ImmutableList.Builder<Condition> conditions, ImmutableList.Builder<Class<? extends Model>> dependencies) {
        conditions.add(new WebLoadedCondition());
        dependencies.add(ItemStackTransformModel.class);
    }

    public static ItemGuessFeature getInstance() {
        return INSTANCE;
    }
}
