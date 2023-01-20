/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.map.pois;

import com.wynntils.core.net.hades.objects.HadesUser;
import com.wynntils.models.map.PoiLocation;
import com.wynntils.models.map.type.DisplayPriority;

public abstract class PlayerPoiBase implements Poi {
    private static final float INITIAL_PLAYER_HEAD_RENDER_SIZE = 20;
    private static final float ADDITIONAL_WIDTH = 20;
    private static final float ADDITIONAL_HEIGHT = 17;

    protected final HadesUser user;
    protected final float playerHeadRenderSize;

    protected PlayerPoiBase(HadesUser user, float playerHeadScale) {
        this.user = user;
        this.playerHeadRenderSize = INITIAL_PLAYER_HEAD_RENDER_SIZE * playerHeadScale;
    }

    @Override
    public PoiLocation getLocation() {
        return user.getMapLocation();
    }

    @Override
    public boolean hasStaticLocation() {
        return false;
    }

    @Override
    public int getWidth(float mapZoom, float scale) {
        return (int) (playerHeadRenderSize + ADDITIONAL_WIDTH);
    }

    @Override
    public int getHeight(float mapZoom, float scale) {
        return (int) (playerHeadRenderSize + ADDITIONAL_HEIGHT);
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public DisplayPriority getDisplayPriority() {
        return DisplayPriority.LOW;
    }
}
