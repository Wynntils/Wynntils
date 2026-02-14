/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.models.worlds.bossbars.StreamerModeBar;
import com.wynntils.models.worlds.event.StreamModeEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import java.util.List;
import net.neoforged.bus.api.SubscribeEvent;

public class StreamerModeModel extends Model {
    private static final StreamerModeBar streamerModeBar = new StreamerModeBar();

    private boolean inStream = false;

    public StreamerModeModel() {
        super(List.of());

        Handlers.BossBar.registerBar(streamerModeBar);
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        // When changing world state, even just switching worlds, streamer mode is always reset.
        // If the boss bar reset isn't triggered with the state change, just manually reset it here
        if (inStream) {
            setStreamerMode(false);
        }
    }

    public void setStreamerMode(boolean inStream) {
        this.inStream = inStream;
        WynntilsMod.postEvent(new StreamModeEvent(inStream));
    }

    public boolean isInStream() {
        return inStream;
    }
}
