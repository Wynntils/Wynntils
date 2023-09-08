/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.war;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Services;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.models.war.scoreboard.WarScoreboardPart;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.services.hades.HadesUser;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WarModel extends Model {
    private static final int WAR_RADIUS = 100;
    private static final ScoreboardPart WAR_SCOREBOARD_PART =
            new WarScoreboardPart(); // This is basically a party scoreboard part, but for war members

    private List<HadesUser> hadesUsers = new ArrayList<>();

    public WarModel() {
        super(List.of());

        Handlers.Scoreboard.addPart(WAR_SCOREBOARD_PART);
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() != WorldState.WORLD) {
            removeWarPlayers();
        }
    }

    public void findWarPlayers() {
        hadesUsers = Services.Hades.getHadesUsers()
                .filter(hadesUser -> hadesUser
                        .getMapLocation()
                        .asLocation()
                        .toVec3()
                        .closerThan(McUtils.player().position(), WAR_RADIUS))
                .toList();
    }

    public void removeWarPlayers() {
        hadesUsers = new ArrayList<>();
    }

    public List<HadesUser> getHadesUsers() {
        return hadesUsers;
    }
}
