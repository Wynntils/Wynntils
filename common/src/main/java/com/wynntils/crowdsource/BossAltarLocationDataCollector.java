/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.crowdsource;

import com.wynntils.core.crowdsource.CrowdSourcedDataCollector;
import com.wynntils.core.crowdsource.type.CrowdSourcedDataType;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.models.npc.label.BossAltarLabelInfo;
import net.neoforged.bus.api.SubscribeEvent;

public class BossAltarLocationDataCollector extends CrowdSourcedDataCollector<BossAltarLabelInfo> {
    @SubscribeEvent
    public void onLabelIdentified(LabelIdentifiedEvent event) {
        if (event.getLabelInfo() instanceof BossAltarLabelInfo bossAltarLabelInfo) {
            collect(bossAltarLabelInfo);
        }
    }

    @Override
    protected CrowdSourcedDataType getDataType() {
        return CrowdSourcedDataType.BOSS_ALTAR_LOCATIONS;
    }
}
