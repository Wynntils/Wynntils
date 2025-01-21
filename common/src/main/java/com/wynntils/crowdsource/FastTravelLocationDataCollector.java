/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.crowdsource;

import com.wynntils.core.crowdsource.CrowdSourcedDataCollector;
import com.wynntils.core.crowdsource.type.CrowdSourcedDataType;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.models.npc.label.FastTravelLabelInfo;
import net.neoforged.bus.api.SubscribeEvent;

public class FastTravelLocationDataCollector extends CrowdSourcedDataCollector<FastTravelLabelInfo> {
    @SubscribeEvent
    public void onLabelIdentified(LabelIdentifiedEvent event) {
        if (event.getLabelInfo() instanceof FastTravelLabelInfo fastTravelLabelInfo) {
            // Seaskipper locations are collected as NPC locations instead
            if (fastTravelLabelInfo.getName().equals("V.S.S. Seaskipper")) return;

            collect(fastTravelLabelInfo);
        }
    }

    @Override
    protected CrowdSourcedDataType getDataType() {
        return CrowdSourcedDataType.FAST_TRAVEL_LOCATIONS;
    }
}
