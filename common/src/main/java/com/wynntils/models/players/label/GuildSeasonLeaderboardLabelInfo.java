/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.models.players.type.GuildLeaderboardInfo;
import com.wynntils.utils.mc.type.Location;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.entity.Entity;

public class GuildSeasonLeaderboardLabelInfo extends LabelInfo {
    private final int season;
    private final boolean currentSeason;
    private final List<Integer> endingDate;
    private final List<GuildLeaderboardInfo> guildLeaderboardInfo;
    private final boolean firstPage;
    private final boolean lastPage;

    public GuildSeasonLeaderboardLabelInfo(
            StyledText label,
            Location location,
            Entity entity,
            int season,
            boolean currentSeason,
            List<Integer> endingDate,
            List<GuildLeaderboardInfo> guildLeaderboardInfo,
            boolean firstPage,
            boolean lastPage) {
        super(label, location, entity);
        this.season = season;
        this.currentSeason = currentSeason;
        this.endingDate = endingDate;
        this.guildLeaderboardInfo = guildLeaderboardInfo;
        this.firstPage = firstPage;
        this.lastPage = lastPage;
    }

    public int getSeason() {
        return season;
    }

    public boolean isCurrentSeason() {
        return currentSeason;
    }

    public List<Integer> getEndingDate() {
        return Collections.unmodifiableList(endingDate);
    }

    public List<GuildLeaderboardInfo> getGuildLeaderboardInfo() {
        return Collections.unmodifiableList(guildLeaderboardInfo);
    }

    public boolean isFirstPage() {
        return firstPage;
    }

    public boolean isLastPage() {
        return lastPage;
    }

    @Override
    public String toString() {
        return "GuildSeasonLeaderboardLabelInfo{" + "label="
                + label + ", season="
                + season + ", currentSeason="
                + currentSeason + ", endingDate="
                + endingDate + ", guildLeaderboardInfo="
                + guildLeaderboardInfo + ", firstPage="
                + firstPage + ", lastPage="
                + lastPage + ", name='"
                + name + '\'' + ", location="
                + location + ", entity="
                + entity + '}';
    }
}
