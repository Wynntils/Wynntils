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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

@FeatureInfo(performance = PerformanceImpact.SMALL, gameplay = GameplayImpact.LARGE, stability = Stability.STABLE)
public class ItemGuessFeature extends Feature {

    public static final boolean showGuessesPrice = true;

    @Override
    public MutableComponent getNameComponent() {
        return new TranslatableComponent("feature.wynntils.itemGuess.name");
    }

    @Override
    public void onInit(ImmutableList.Builder<Condition> conditions) {
        conditions.add(new WebLoadedCondition());
    }

    @Override
    protected boolean onEnable() {
        return WebManager.isItemGuessesLoaded() || WebManager.tryLoadItemGuesses();
    }

    @Override
    protected void onDisable() {}
}
