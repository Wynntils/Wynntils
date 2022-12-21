/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.scoreboard;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.core.managers.Models;
import com.wynntils.mc.event.ScoreboardSetScoreEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.scoreboard.objectives.ObjectiveListener;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ScoreboardModel extends Model {
    public static final Pattern GUILD_ATTACK_UPCOMING_PATTERN = Pattern.compile("Upcoming Attacks:");
    public static final Pattern QUEST_TRACK_PATTERN = Pattern.compile("Tracked Quest:");
    public static final Pattern OBJECTIVE_HEADER_PATTERN = Pattern.compile("([★⭑] )?(Daily )?Objectives?:");
    public static final Pattern GUILD_OBJECTIVE_HEADER_PATTERN = Pattern.compile("([★⭑] )?Guild Obj: (.+)");
    public static final Pattern PARTY_PATTERN = Pattern.compile("Party:\\s\\[Lv. (\\d+)]");

    @Override
    public void init() {
        ScoreboardHandler.INSTANCE.registerHandler(
                new ObjectiveListener(), Set.of(SegmentType.Objective, SegmentType.GuildObjective));
        ScoreboardHandler.INSTANCE.registerHandler(Managers.Quest.SCOREBOARD_HANDLER, SegmentType.Quest);
        ScoreboardHandler.INSTANCE.registerHandler(
                Models.GuildAttackTimer.SCOREBOARD_HANDLER, SegmentType.GuildAttackTimer);

        ScoreboardHandler.INSTANCE.init();
    }

    @Override
    public void disable() {
        ScoreboardHandler.INSTANCE.disable();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSetScore(ScoreboardSetScoreEvent event) {
        // For now, be a proxy
        ScoreboardHandler.INSTANCE.onSetScore(event);
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        // For now, be a proxy
        ScoreboardHandler.INSTANCE.onWorldStateChange(event);
    }

    public enum SegmentType {
        Quest(QUEST_TRACK_PATTERN),
        Party(PARTY_PATTERN),
        Objective(OBJECTIVE_HEADER_PATTERN),
        GuildObjective(GUILD_OBJECTIVE_HEADER_PATTERN),
        GuildAttackTimer(GUILD_ATTACK_UPCOMING_PATTERN);

        private final Pattern headerPattern;

        SegmentType(Pattern headerPattern) {
            this.headerPattern = headerPattern;
        }

        public Pattern getHeaderPattern() {
            return headerPattern;
        }
    }
}
