/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.core.webapi.WebManager;

@FeatureInfo(stability = Stability.STABLE, gameplay = GameplayImpact.LARGE, performance = PerformanceImpact.SMALL)
public class ItemStatInfoFeature extends Feature {

    // TODO: Replace these with configs
    public static final boolean showStars = true;
    public static final boolean colorLerp = true;

    public static final boolean perfect = true;
    public static final boolean defective = true;
    public static final float obfuscationChanceStart = 0.08f;
    public static final float obfuscationChanceEnd = 0.04f;

    public static final boolean reorderIdentifications = true;
    public static final boolean groupIdentifications = true;

    public ItemStatInfoFeature() {}

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {
        conditions.add(new WebLoadedCondition());
    }

    @Override
    protected boolean onEnable() {
        return WebManager.isItemListLoaded() || WebManager.tryLoadItemList();
    }

    @Override
    protected void onDisable() {}
}
