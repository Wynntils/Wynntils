/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.wynntils.sockets.objects.HadesUser;

public abstract class PlayerPoiBase implements Poi {
    protected final HadesUser user;
    protected float playerHeadRenderSize;

    protected PlayerPoiBase(HadesUser user, float playerHeadScale) {
        this.user = user;
        this.playerHeadRenderSize = 20 * playerHeadScale;
    }

    public MapLocation getLocation() {
        return user.getMapLocation();
    }

    public boolean hasStaticLocation() {
        return false;
    }

    public int getWidth(float mapZoom, float scale) {
        return (int) (playerHeadRenderSize + 20);
    }

    public int getHeight(float mapZoom, float scale) {
        return (int) (playerHeadRenderSize + 17);
    }

    public String getName() {
        return user.getName();
    }
}
