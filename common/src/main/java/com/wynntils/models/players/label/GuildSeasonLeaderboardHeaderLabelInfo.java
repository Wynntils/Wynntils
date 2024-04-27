/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

public class GuildSeasonLeaderboardHeaderLabelInfo extends LabelInfo {
    private final int season;

    protected GuildSeasonLeaderboardHeaderLabelInfo(StyledText label, Location location, Entity entity, int season) {
        super(label, location, entity);
        this.season = season;
    }

    public int getSeason() {
        return season;
    }
}
