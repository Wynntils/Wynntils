/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npc;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.models.npc.label.FastTravelLabelParser;
import com.wynntils.models.npc.label.NpcLabelParser;
import java.util.List;

public final class NpcModel extends Model {
    public NpcModel() {
        super(List.of());

        Handlers.Label.registerParser(new NpcLabelParser());
        Handlers.Label.registerParser(new FastTravelLabelParser());
    }
}
