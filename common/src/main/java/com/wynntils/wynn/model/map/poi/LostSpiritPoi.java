/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

public class LostSpiritPoi extends ServicePoi {

    private final int number;
    public LostSpiritPoi(MapLocation location, int number) {
        super(location, ServiceKind.LOST_SPRIT);

        this.number = number;
    }

    @Override
    public String getName() {
        return String.format("Lost Spirit #%s", number);
    }
}
