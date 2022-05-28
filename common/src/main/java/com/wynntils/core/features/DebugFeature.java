/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

/** Feature for debugging */
@FeatureInfo(stability = Stability.UNSTABLE)
public abstract class DebugFeature extends Feature {

    @Override
    public MutableComponent getNameComponent() {
        return new TranslatableComponent("featureDebug.wynntils." + getNameCamelCase() + ".name");
    }

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {
        conditions.add(new DevelopmentCondition());
    }

    public class DevelopmentCondition extends Condition {

        @Override
        public void init() {
            setSatisfied(WynntilsMod.developmentEnvironment);
        }
    }
}
