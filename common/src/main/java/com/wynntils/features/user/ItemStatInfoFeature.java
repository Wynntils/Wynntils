/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.core.webapi.WebManager;

@FeatureInfo(stability = Stability.STABLE, category = "Item Tooltips")
public class ItemStatInfoFeature extends UserFeature {

    @Config(displayName = "Show Stars")
    public static boolean showStars = true;

    @Config(displayName = "Color Lerp")
    public static boolean colorLerp = true;

    @Config(displayName = "Rainbow Perfect Items")
    public static boolean perfect = true;

    @Config(displayName = "Obfuscated Defective Items")
    public static boolean defective = true;

    @Config(displayName = "Obfuscation Start Chance")
    public static float obfuscationChanceStart = 0.08f;

    @Config(displayName = "Obfuscation End Chance")
    public static float obfuscationChanceEnd = 0.04f;

    @Config(displayName = "Reorder Identifications")
    public static boolean reorderIdentifications = true;

    @Config(displayName = "Group Identifications")
    public static boolean groupIdentifications = true;

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {
        conditions.add(new WebLoadedCondition());
    }

    @Override
    protected boolean onEnable() {
        return WebManager.isItemListLoaded() || WebManager.tryLoadItemList();
    }
}
