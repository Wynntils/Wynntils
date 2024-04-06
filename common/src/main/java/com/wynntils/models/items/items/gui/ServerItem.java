/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.items.properties.CountedItemProperty;

public class ServerItem extends GuiItem implements CountedItemProperty {
    private final int serverId;

    public ServerItem(int serverId) {
        this.serverId = serverId;
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
        return "ServerItem{" + "serverId=" + serverId + '}';
    }
}
