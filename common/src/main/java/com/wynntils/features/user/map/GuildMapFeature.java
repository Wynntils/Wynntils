/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.map;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.AdvancementUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.MAP)
public class GuildMapFeature extends UserFeature {
    @SubscribeEvent
    public void onAdvancementUpdate(AdvancementUpdateEvent event) {}
}
