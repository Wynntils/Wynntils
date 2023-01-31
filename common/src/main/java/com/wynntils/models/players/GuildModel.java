/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.models.players.event.RelationsUpdateEvent;
import com.wynntils.models.players.hades.event.HadesEvent;
import com.wynntils.models.worlds.WorldStateModel;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * This model handles the player's guild relations.
 */
public final class GuildModel extends Model {

    private boolean expectingGuildMessage = false;
    private Set<String> guildMembers = new HashSet<>();

    public GuildModel(WorldStateModel worldStateModel) {
        super(List.of(worldStateModel));
        resetRelations();
    }

    @SubscribeEvent
    public void onAuth(HadesEvent.Authenticated event) {
        if (!Models.WorldState.onWorld()) return;

        requestGuildListUpdate();
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.WORLD) {
            requestGuildListUpdate();
        } else {
            resetRelations();
        }
    }

    private void resetRelations() {
        guildMembers = new HashSet<>();

        WynntilsMod.postEvent(new RelationsUpdateEvent.GuildList(guildMembers, RelationsUpdateEvent.ChangeType.RELOAD));
    }

    public void requestGuildListUpdate() {
        if (McUtils.player() == null) return;

        expectingGuildMessage = true;
        McUtils.sendCommand("guild list");
        WynntilsMod.info("Requested guild list from Wynncraft.");
    }

    private Set<String> getGuildMembers() {
        return guildMembers;
    }
}
