/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.crowdsource;

import com.wynntils.core.components.Models;
import com.wynntils.core.crowdsource.CrowdSourcedDataCollector;
import com.wynntils.core.crowdsource.type.CrowdSourcedDataType;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.models.npc.label.NpcLabelInfo;
import net.neoforged.bus.api.SubscribeEvent;

public class NpcLocationDataCollector extends CrowdSourcedDataCollector<NpcLabelInfo> {
    @SubscribeEvent
    public void onLabelIdentified(LabelIdentifiedEvent event) {
        if (Models.WorldState.onHousing()) return;

        if (event.getLabelInfo() instanceof NpcLabelInfo npcLabelInfo) {
            collect(npcLabelInfo);
        }
    }

    @Override
    protected CrowdSourcedDataType getDataType() {
        return CrowdSourcedDataType.NPC_LOCATIONS;
    }
}
