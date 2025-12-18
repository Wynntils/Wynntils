/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.mc.extension.EntityExtension;
import com.wynntils.models.combat.label.DamageLabelInfo;
import com.wynntils.models.combat.label.KillLabelInfo;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class HideLabelsFeature extends Feature {
    @Persisted
    private final Config<Boolean> hideDamageLabels = new Config<>(false);

    @Persisted
    private final Config<Boolean> hideKillLabels = new Config<>(false);

    public HideLabelsFeature() {
        super(ProfileDefault.DISABLED);
    }

    @SubscribeEvent
    public void onLabelIdentified(LabelIdentifiedEvent event) {
        if (hideDamageLabels.get() && event.getLabelInfo() instanceof DamageLabelInfo damageLabelInfo) {
            ((EntityExtension) damageLabelInfo.getEntity()).setRendered(false);
        } else if (hideKillLabels.get() && event.getLabelInfo() instanceof KillLabelInfo killLabelInfo) {
            ((EntityExtension) killLabelInfo.getEntity()).setRendered(false);
        }
    }
}
