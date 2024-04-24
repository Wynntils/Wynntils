/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.war.type;

import java.util.ArrayList;
import java.util.List;

public class WarBattleInfo {
    private final String territory;
    private final String ownerGuild;

    private final List<WarTowerState> states = new ArrayList<>();

    public WarBattleInfo(String territory, String ownerGuild, WarTowerState initialState) {
        this.territory = territory;
        this.ownerGuild = ownerGuild;
        this.states.add(initialState);
    }

    public String getTerritory() {
        return territory;
    }

    public String getOwnerGuild() {
        return ownerGuild;
    }

    public WarTowerState getInitialState() {
        return states.get(0);
    }

    public WarTowerState getCurrentState() {
        return states.get(states.size() - 1);
    }

    public long getTotalLength() {
        return states.get(states.size() - 1).timestamp() - states.get(0).timestamp();
    }

    public long getDps(long seconds) {
        // Get the dps over the last x seconds
        long now = states.get(states.size() - 1).timestamp();
        long start = now - seconds * 1000L;

        List<WarTowerState> relevantStates =
                states.stream().filter(state -> state.timestamp() >= start).toList();

        return relevantStates.size() < 2
                ? 0
                : (relevantStates.get(0).health()
                                - relevantStates.get(relevantStates.size() - 1).health())
                        / seconds;
    }

    public void addNewState(WarTowerState towerState) {
        states.add(towerState);
    }
}
