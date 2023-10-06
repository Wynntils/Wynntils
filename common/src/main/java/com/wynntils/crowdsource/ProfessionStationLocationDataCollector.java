/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.crowdsource;

import com.wynntils.core.crowdsource.CrowdSourcedDataCollector;
import com.wynntils.core.crowdsource.type.CrowdSourcedDataType;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.models.profession.label.GatheringStationLabelInfo;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ProfessionStationLocationDataCollector extends CrowdSourcedDataCollector<GatheringStationLabelInfo> {
    @SubscribeEvent
    public void onLabelIdentified(LabelIdentifiedEvent event) {
        if (event.getLabelInfo() instanceof GatheringStationLabelInfo gatheringStationLabelInfo) {
            collect(gatheringStationLabelInfo);
        }
    }

    @Override
    protected CrowdSourcedDataType getDataType() {
        return CrowdSourcedDataType.PROFESSION_CRAFTING_STATION_LOCATIONS;
    }
}
