/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils.scoreboard;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.ScoreboardSetScoreEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.event.WorldStateEvent;
import com.wynntils.wc.event.WynntilsScoreboardUpdateEvent;
import com.wynntils.wc.model.WorldState;
import com.wynntils.wc.utils.scoreboard.objectives.ObjectiveManager;
import com.wynntils.wc.utils.scoreboard.quests.QuestManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ScoreboardManager {

    private static final String GUILD_ATTACK_UPCOMING = "§b§lUpcoming Attacks:";
    private static final Pattern GUILD_ATTACK_TERRITORY_PATTERN = Pattern.compile("§b-\\s(\\d+):(\\d+)\\s§3(.+)");
    private static final Pattern OBJECTIVE_HEADER_PATTERN = Pattern.compile("(★ )?(Daily )?Objectives?:");
    private static final Pattern GUILD_OBJECTIVE_HEADER_PATTERN = Pattern.compile("(★ )?Guild Obj: (.+)");
    private static final Pattern PARTY_PATTERN = Pattern.compile("§e§lParty:§6\\s\\[Lv. (\\d+)]");
    private static final Pattern PARTY_PLAYER_HEALTH_PATTERN =
            Pattern.compile("-\\s\\[\\|\\|(\\d+)\\|\\|]\\s(.+)\\s\\[(\\d+)]");

    // TimeUnit.Seconds
    private static final int CHANGE_PROCESS_RATE = 1;

    private static List<ScoreboardLine> reconstructedScoreboard = new ArrayList<>();

    private static final LinkedList<ScoreboardLineChange> queuedChanges = new LinkedList<>();

    private static ScheduledExecutorService executor = null;

    // Wynn has two different objectives for the same thing, making packets/events duplicated. This is used to prevent
    // the duplication.
    private static Objective trackedObjectivePlayer = null;

    private static final Runnable changeHandlerRunnable = () -> {
        if (queuedChanges.isEmpty()) return;

        Map<WynntilsScoreboardUpdateEvent.ChangeType, Set<WynntilsScoreboardUpdateEvent.Change>> changeTypes =
                new HashMap<>();

        List<ScoreboardLine> scoreboardCopy = new ArrayList<>(reconstructedScoreboard);

        while (!queuedChanges.isEmpty()) {
            ScoreboardLineChange processed = queuedChanges.pop();

            ScoreboardLine line = new ScoreboardLine(processed.lineText(), processed.lineIndex());

            scoreboardCopy.removeIf(scoreboardLine -> scoreboardLine.index() == processed.lineIndex());

            if (processed.method() == ServerScoreboard.Method.CHANGE) {
                scoreboardCopy.add(line);
            }

            Optional<WynntilsScoreboardUpdateEvent.ChangeType> changeType = getChangeType(processed);
            changeType.ifPresent(change -> {
                changeTypes.putIfAbsent(change, new HashSet<>());
                changeTypes.get(change).add(new WynntilsScoreboardUpdateEvent.Change(line.line(), processed.method()));
            });
        }

        scoreboardCopy.sort(Comparator.comparingInt(ScoreboardLine::index).reversed());

        reconstructedScoreboard = scoreboardCopy;

        for (Map.Entry<WynntilsScoreboardUpdateEvent.ChangeType, Set<WynntilsScoreboardUpdateEvent.Change>> entry :
                changeTypes.entrySet()) {
            WynntilsMod.getEventBus().post(entry.getKey().toEvent(entry.getValue()));
        }
    };

    private static Optional<WynntilsScoreboardUpdateEvent.ChangeType> getChangeType(ScoreboardLineChange change) {
        String line = change.lineText();
        String withoutFormat = ChatFormatting.stripFormatting(line);

        if (withoutFormat == null) {
            return Optional.empty();
        }

        if (GUILD_ATTACK_TERRITORY_PATTERN.matcher(line).matches() || GUILD_ATTACK_UPCOMING.equals(line)) {
            return Optional.of(WynntilsScoreboardUpdateEvent.ChangeType.GuildAttackTimer);
        }

        if (PARTY_PLAYER_HEALTH_PATTERN.matcher(withoutFormat).matches()
                || PARTY_PATTERN.matcher(line).matches()) {
            return Optional.of(WynntilsScoreboardUpdateEvent.ChangeType.Party);
        }

        if (ObjectiveManager.OBJECTIVE_PATTERN.matcher(line).matches()) {
            return Optional.of(WynntilsScoreboardUpdateEvent.ChangeType.Objective);
        }

        if (withoutFormat.matches("À+")
                || OBJECTIVE_HEADER_PATTERN.matcher(withoutFormat).matches()
                || GUILD_OBJECTIVE_HEADER_PATTERN.matcher(withoutFormat).matches()) {
            return Optional.empty();
        }

        // For unidentified text, assume it is a quest description
        return Optional.of(WynntilsScoreboardUpdateEvent.ChangeType.Quest);
    }

    public static void init() {
        WynntilsMod.getEventBus().register(ScoreboardManager.class);
        WynntilsMod.getEventBus().register(ObjectiveManager.class);
        WynntilsMod.getEventBus().register(QuestManager.class);
    }

    @SubscribeEvent
    public static void onSetScore(ScoreboardSetScoreEvent event) {
        if (trackedObjectivePlayer == null) {
            getTrackedObjectivePlayer();

            if (trackedObjectivePlayer == null) {
                return;
            }
        }

        // Prevent duplication
        if (!Objects.equals(event.getObjectiveName(), trackedObjectivePlayer.getName())) {
            return;
        }

        queuedChanges.add(new ScoreboardLineChange(event.getOwner(), event.getMethod(), event.getScore()));
    }

    private static void getTrackedObjectivePlayer() {
        Scoreboard scoreboard = McUtils.player().getScoreboard();

        List<Objective> objectives = scoreboard.getObjectives().stream().toList();

        if (objectives.isEmpty()) {
            return;
        }

        trackedObjectivePlayer = objectives.get(0);
    }

    @SubscribeEvent
    public static void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.State.WORLD) {
            executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(changeHandlerRunnable, 0, CHANGE_PROCESS_RATE, TimeUnit.SECONDS);
            return;
        }

        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        queuedChanges.clear();
        reconstructedScoreboard.clear();

        ObjectiveManager.resetObjectives();
        QuestManager.questTrackingStopped();
    }

    public static List<ScoreboardLine> getReconstructedScoreboard() {
        return reconstructedScoreboard;
    }
}
