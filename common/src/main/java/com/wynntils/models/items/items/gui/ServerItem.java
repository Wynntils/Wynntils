/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.items.properties.CountedItemProperty;
import com.wynntils.models.worlds.type.ServerRegion;

public class ServerItem extends GuiItem implements CountedItemProperty {
    private final ServerRegion region;
    private final int serverId;

    public ServerItem(ServerRegion region, int serverId) {
        this.serverId = serverId;
        this.region = region;
    }

    public ServerRegion getRegion() {
        return region;
    }

    public int getServerId() {
        return serverId;
    }

    @Override
    public int getCount() {
        return serverId;
    }

    @Override
    public String toString() {
        return "ServerItem{" + "region=" + region + ", serverId=" + serverId + '}';
    }
}
