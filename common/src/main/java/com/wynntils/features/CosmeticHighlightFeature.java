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
import com.wynntils.wc.ItemStackTransformer;
import com.wynntils.wc.objects.item.WynnCosmeticStack;
import com.wynntils.wc.utils.WynnItemMatchers;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

@FeatureInfo(performance = PerformanceImpact.SMALL, gameplay = GameplayImpact.SMALL, stability = Stability.STABLE)
public class CosmeticHighlightFeature extends Feature {

    public static final boolean highlightDuplicates = true;

    @Override
    public MutableComponent getNameComponent() {
        return new TranslatableComponent("feature.wynntils.cosmetichighlight.name");
    }

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {}

    @Override
    protected boolean onEnable() {
        ItemStackTransformer.registerTransformer(WynnItemMatchers::isCosmetic, WynnCosmeticStack::new);
        return true;
    }

    @Override
    protected void onDisable() {
        ItemStackTransformer.unregisterTransformer(WynnItemMatchers::isCosmetic, WynnCosmeticStack::new);
    }
}
