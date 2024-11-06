/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.mc.extension.EntityExtension;
import com.wynntils.models.damage.label.DamageLabelInfo;
import net.neoforged.bus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.COMBAT)
public class HideDamageLabelsFeature extends Feature {
    @SubscribeEvent
    public void onLabelIdentified(LabelIdentifiedEvent event) {
        if (event.getLabelInfo() instanceof DamageLabelInfo damageLabelInfo) {
            ((EntityExtension) damageLabelInfo.getEntity()).setRendered(false);
        }
    }
}
