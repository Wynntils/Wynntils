/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.gui;

import com.wynntils.model.item.properties.CountedItemProperty;

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
}
