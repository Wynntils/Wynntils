/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.config.properties.ConfigOption;
import com.wynntils.core.config.properties.Configurable;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.core.webapi.WebManager;

@FeatureInfo(stability = Stability.STABLE)
@Configurable(category = "Item Identifications")
public class ItemStatInfoFeature extends UserFeature {

    @ConfigOption(displayName = "Show Stars")
    public static boolean showStars = true;

    @ConfigOption(displayName = "Color Lerp")
    public static boolean colorLerp = true;

    @ConfigOption(displayName = "Rainbow Perfect Items")
    public static boolean perfect = true;

    @ConfigOption(displayName = "Obfuscated Defective Items")
    public static boolean defective = true;

    @ConfigOption(displayName = "Obfuscation Start Chance")
    public static float obfuscationChanceStart = 0.08f;

    @ConfigOption(displayName = "Obfuscation End Chance")
    public static float obfuscationChanceEnd = 0.04f;

    @ConfigOption(displayName = "Reorder Identifications")
    public static boolean reorderIdentifications = true;

    @ConfigOption(displayName = "Group Identifications")
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
