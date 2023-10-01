/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.label;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.labels.event.EntityLabelChangedEvent;
import com.wynntils.models.label.event.LabelIdentifiedEvent;
import com.wynntils.models.label.parsers.GatheringNodeLabelParser;
import com.wynntils.models.label.parsers.GatheringStationLabelParser;
import com.wynntils.models.label.parsers.NpcLabelParser;
import com.wynntils.models.label.type.LabelInfo;
import com.wynntils.models.label.type.LabelParser;
import com.wynntils.utils.mc.type.Location;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LabelModel extends Model {
    private final List<LabelParser> parsers = new ArrayList<>();

    public LabelModel() {
        super(List.of());

        registerParsers();
    }

    @SubscribeEvent
    public void onLabelChange(EntityLabelChangedEvent event) {
        for (LabelParser parser : parsers) {
            LabelInfo info = parser.getInfo(
                    event.getName(), Location.containing(event.getEntity().position()));

            if (info == null) continue;

            WynntilsMod.postEvent(new LabelIdentifiedEvent(info));
            return;
        }
    }

    private void registerParsers() {
        // Special order for optimization
        registerParser(new NpcLabelParser());
        registerParser(new GatheringNodeLabelParser());

        // Non-frequent parsers, alphabetically ordered
        registerParser(new GatheringStationLabelParser());
    }

    private void registerParser(LabelParser labelParser) {
        parsers.add(labelParser);
    }
}
