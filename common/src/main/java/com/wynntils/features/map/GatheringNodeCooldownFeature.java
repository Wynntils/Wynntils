/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.map;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.models.profession.event.ProfessionNodeGatheredEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.MAP)
public class GatheringNodeCooldownFeature extends Feature {
    @SubscribeEvent
    public void onNodeGathered(ProfessionNodeGatheredEvent.LabelShown event) {
        event.setAddCooldownArmorstand(true);
    }
}
