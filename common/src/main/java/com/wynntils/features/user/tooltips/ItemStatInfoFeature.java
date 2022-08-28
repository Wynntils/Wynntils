/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.tooltips;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.ItemStackTransformModel;

@FeatureInfo(stability = Stability.STABLE, category = FeatureCategory.TOOLTIPS)
public class ItemStatInfoFeature extends UserFeature {
    @Config
    public static boolean showStars = true;

    @Config
    public static boolean colorLerp = true;

    @Config
    public static boolean perfect = true;

    @Config
    public static boolean defective = true;

    @Config
    public static float obfuscationChanceStart = 0.08f;

    @Config
    public static float obfuscationChanceEnd = 0.04f;

    @Config
    public static boolean reorderIdentifications = true;

    @Config
    public static boolean groupIdentifications = true;

    @Override
    protected void onInit(
            ImmutableList.Builder<Condition> conditions, ImmutableList.Builder<Class<? extends Model>> dependencies) {
        conditions.add(new WebLoadedCondition());
        dependencies.add(ItemStackTransformModel.class);
    }
}
