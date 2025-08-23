/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.mc.extension.EntityExtension;
import com.wynntils.models.combat.label.DamageLabelInfo;
import com.wynntils.models.combat.label.KillLabelInfo;
import net.neoforged.bus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.COMBAT)
public class HideLabelsFeature extends Feature {
    @Persisted
    public final Config<Boolean> hideDamageLabels = new Config<>(false);

    @Persisted
    public final Config<Boolean> hideKillLabels = new Config<>(false);

    @SubscribeEvent
    public void onLabelIdentified(LabelIdentifiedEvent event) {
        if (hideDamageLabels.get() && event.getLabelInfo() instanceof DamageLabelInfo damageLabelInfo) {
            ((EntityExtension) damageLabelInfo.getEntity()).setRendered(false);
        } else if (hideKillLabels.get() && event.getLabelInfo() instanceof KillLabelInfo killLabelInfo) {
            ((EntityExtension) killLabelInfo.getEntity()).setRendered(false);
        }
    }
}
