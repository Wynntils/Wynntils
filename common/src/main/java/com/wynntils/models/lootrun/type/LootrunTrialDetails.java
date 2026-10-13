/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LootrunTrialDetails {
    private List<TrialType> trials = new ArrayList<>();

    public List<TrialType> getTrials() {
        return Collections.unmodifiableList(trials);
    }

    public void setTrials(List<TrialType> trials) {
        this.trials = new ArrayList<>(trials);
    }

    public void addTrial(TrialType newTrial) {
        trials.add(newTrial);
    }
}
