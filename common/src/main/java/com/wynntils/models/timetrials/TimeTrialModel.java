/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.timetrials;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.models.timetrials.label.TimeTrialLabelParser;
import java.util.List;

public final class TimeTrialModel extends Model {
    public TimeTrialModel() {
        super(List.of());

        Handlers.Label.registerParser(new TimeTrialLabelParser());
    }
}
