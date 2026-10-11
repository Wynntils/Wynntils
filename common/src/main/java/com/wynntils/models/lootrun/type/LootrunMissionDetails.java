/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LootrunMissionDetails {
    private List<MissionType> missions = new ArrayList<>();

    public List<MissionType> getMissions() {
        return Collections.unmodifiableList(missions);
    }

    public void setMissions(List<MissionType> missions) {
        this.missions = new ArrayList<>(missions);
    }

    public void addMission(MissionType newMission) {
        missions.add(newMission);
    }
}
