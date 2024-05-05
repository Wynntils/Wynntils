/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

public class GuildSeasonLeaderboardLabelInfo extends LabelInfo {
    private final String guild;
    private final int place;
    private final long score;

    public GuildSeasonLeaderboardLabelInfo(
            StyledText label, Location location, Entity entity, String guild, int place, long score) {
        super(label, location, entity);
        this.guild = guild;
        this.place = place;
        this.score = score;
    }

    public String getGuild() {
        return guild;
    }

    public int getPlace() {
        return place;
    }

    public long getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "GuildSeasonLeaderboardLabelInfo{" + "guild='"
                + guild + '\'' + ", place="
                + place + ", score="
                + score + ", label="
                + label + ", name='"
                + name + '\'' + ", location="
                + location + ", entity="
                + entity + '}';
    }
}
