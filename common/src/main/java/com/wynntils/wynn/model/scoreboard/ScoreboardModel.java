/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.scoreboard;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.ScoreboardSetScoreEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.ScoreboardSegmentAdditionEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import com.wynntils.wynn.model.quests.QuestManager;
import com.wynntils.wynn.model.scoreboard.objectives.ObjectiveHandler;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ScoreboardModel extends Model {
    private static final Pattern GUILD_ATTACK_UPCOMING_PATTERN = Pattern.compile("Upcoming Attacks:");
    private static final Pattern QUEST_TRACK_PATTERN = Pattern.compile("Tracked Quest:");
    private static final Pattern OBJECTIVE_HEADER_PATTERN = Pattern.compile("([★⭑] )?(Daily )?Objectives?:");
    private static final Pattern GUILD_OBJECTIVE_HEADER_PATTERN = Pattern.compile("([★⭑] )?Guild Obj: (.+)");
    private static final Pattern PARTY_PATTERN = Pattern.compile("Party:\\s\\[Lv. (\\d+)]");

    // TimeUnit.MILLISECONDS
    // 250 -> 4 times a second
    private static final int CHANGE_PROCESS_RATE = 250;

    private static List<ScoreboardLine> reconstructedScoreboard = new ArrayList<>();

    private static List<Segment> segments = new ArrayList<>();

    private static final LinkedList<ScoreboardLineChange> queuedChanges = new LinkedList<>();

    private static final List<ScoreboardHandler> scoreboardHandlers = new ArrayList<>();

    private static ScheduledExecutorService executor = null;

    private static void handleChanges() {
        if (!WynnUtils.onWorld() || McUtils.player() == null) return;

        if (queuedChanges.isEmpty()) {
            return; // no changes
        }

        // Process queue
        reconstructedScoreboard = addQueuedChanges(queuedChanges, reconstructedScoreboard);

        queuedChanges.clear();

        // Find segments
        List<Segment> parsedSegments = calculateSegments(reconstructedScoreboard);

        // Send events
        List<Segment> removedSegments = segments.stream()
                .filter(segment -> parsedSegments.stream().noneMatch(parsed -> parsed.getType() == segment.getType()))
                .toList();

        List<Segment> changedSegments = parsedSegments.stream()
                .filter(segment -> segments.stream().noneMatch(parsed -> parsed.equals(segment)))
                .toList();

        segments = parsedSegments;

        for (Segment segment : removedSegments) {
            for (ScoreboardHandler scoreboardHandler : scoreboardHandlers) {
                if (scoreboardHandler.handledSegments().contains(segment.getType())) {
                    scoreboardHandler.onSegmentRemove(segment, segment.getType());
                }
            }
        }

        for (Segment segment : changedSegments) {
            for (ScoreboardHandler scoreboardHandler : scoreboardHandlers) {
                if (scoreboardHandler.handledSegments().contains(segment.getType())) {
                    scoreboardHandler.onSegmentChange(segment, segment.getType());
                }
            }
        }

        McUtils.mc().doRunTask(() -> handleScoreboardReconstruction(segments));
    }
    ;

    private static List<ScoreboardLine> addQueuedChanges(
            List<ScoreboardLineChange> queuedChanges, List<ScoreboardLine> scoreboard) {
        Map<Integer, ScoreboardLine> scoreboardLineMap = new TreeMap<>();

        for (ScoreboardLine scoreboardLine : scoreboard) {
            scoreboardLineMap.put(scoreboardLine.index(), scoreboardLine);
        }

        for (ScoreboardLineChange change : queuedChanges) {
            if (change.method() == ServerScoreboard.Method.REMOVE) {
                scoreboardLineMap.remove(change.lineIndex());
            } else {
                ScoreboardLine line = new ScoreboardLine(change.lineText(), change.lineIndex());
                scoreboardLineMap.put(line.index(), line);
            }
        }

        return scoreboardLineMap.values().stream().toList();
    }

    private static void handleScoreboardReconstruction(List<Segment> segments) {
        List<String> toBeAdded = new ArrayList<>();

        int segmentsAdded = 0;
        for (Segment segment : segments) {

            if (!WynntilsMod.postEvent(new ScoreboardSegmentAdditionEvent(segment))) {
                if (segmentsAdded != 1) {
                    toBeAdded.add("À".repeat(segmentsAdded)); // avoid duplicates
                }
                toBeAdded.addAll(segment.getScoreboardLines());

                segmentsAdded++;
            }
        }

        // index of first and last nonempty lines
        int frontIndex = -1;
        int backIndex = -1;

        for (int i = 0; i < toBeAdded.size(); i++) {
            if (toBeAdded.get(i).matches("À+")) continue;

            frontIndex = i;
            break;
        }

        if (frontIndex == -1) { // check if whole scoreboard is empty
            overrideScoreboard(new ArrayList<>());
            return;
        }

        for (int i = toBeAdded.size() - 1; i >= 0; i--) {
            if (toBeAdded.get(i).matches("À+")) continue;

            backIndex = i;
            break;
        }

        overrideScoreboard(toBeAdded.subList(frontIndex, backIndex + 1));
    }

    private static void overrideScoreboard(List<String> lines) {
        Scoreboard scoreboard = McUtils.player().getScoreboard();

        String objectiveName = "wynntilsSB" + McUtils.player().getScoreboardName();

        Objective objective;

        if (scoreboard.hasObjective(objectiveName)) {
            objective = scoreboard.getObjective(objectiveName);
        } else {
            objective = scoreboard.addObjective(
                    objectiveName,
                    ObjectiveCriteria.DUMMY,
                    new TextComponent(" play.wynncraft.com")
                            .withStyle(ChatFormatting.GOLD)
                            .withStyle(ChatFormatting.BOLD),
                    ObjectiveCriteria.RenderType.INTEGER);
        }

        // Set as side bar and player team objective if existing
        scoreboard.setDisplayObjective(1, objective);

        PlayerTeam playerTeam = scoreboard.getPlayersTeam(McUtils.player().getScoreboardName());
        if (playerTeam != null) {
            if (playerTeam.getColor().getId() >= 0) {
                int id = playerTeam.getColor().getId() + 3;
                scoreboard.setDisplayObjective(id, objective);
            }
        }

        // Update score map with new values
        for (Map<Objective, Score> scoreMap : scoreboard.playerScores.values()) {
            scoreMap.remove(objective);
        }

        for (int i = 0; i < lines.size(); i++) {
            Score score = scoreboard.getOrCreatePlayerScore(lines.get(i), objective);

            // Order through using score
            score.setScore(lines.size() - i);
        }
    }

    private static List<Segment> calculateSegments(List<ScoreboardLine> scoreboard) {
        List<Segment> segments = new ArrayList<>();

        List<String> scoreboardLines =
                scoreboard.stream().map(ScoreboardLine::line).toList();

        Segment currentSegment = null;

        int startIndex = 0;
        for (int i = 0; i < scoreboardLines.size(); i++) {
            String line = scoreboardLines.get(i);

            String strippedLine = ComponentUtils.stripFormatting(line);

            if (strippedLine == null) {
                continue;
            }

            if (strippedLine.matches("À+")) { // Segment complete
                if (currentSegment != null) {
                    // Sublisting is not a problem as no overlap
                    currentSegment.setContent(scoreboardLines.subList(startIndex + 1, i));
                    segments.add(currentSegment);
                    currentSegment = null;
                }
            } else if (currentSegment == null) { // Is header
                SegmentType type = null;

                for (SegmentType value : SegmentType.values()) {
                    if (!value.getHeaderPattern().matcher(strippedLine).matches()) continue;

                    type = value;
                    break;
                }

                if (type != null) {
                    currentSegment = new Segment(type, line);
                    startIndex = i;
                    continue;
                }

                WynntilsMod.error(String.format("Failed to parse header of %s", strippedLine));
            }
        }

        if (currentSegment != null) {
            currentSegment.setContent(scoreboardLines.subList(startIndex + 1, scoreboardLines.size()));
            segments.add(currentSegment);
        }

        return segments;
    }

    public static void init() {
        registerHandler(new ObjectiveHandler());
        registerHandler(QuestManager.SCOREBOARD_HANDLER);

        startThread();
    }

    public static void disable() {
        resetState();
        scoreboardHandlers.clear();
    }

    private static void registerHandler(ScoreboardHandler handlerInstance) {
        scoreboardHandlers.add(handlerInstance);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSetScore(ScoreboardSetScoreEvent event) {
        queuedChanges.add(new ScoreboardLineChange(event.getOwner(), event.getMethod(), event.getScore()));
    }

    @SubscribeEvent
    public static void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldStateManager.State.WORLD) {
            startThread();
            return;
        }

        resetState();
    }

    private static void startThread() {
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(ScoreboardModel::handleChanges, 0, CHANGE_PROCESS_RATE, TimeUnit.MILLISECONDS);
    }

    private static void resetState() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        queuedChanges.clear();
        reconstructedScoreboard.clear();
        segments.clear();

        for (ScoreboardHandler scoreboardHandler : scoreboardHandlers) {
            scoreboardHandler.resetHandler();
        }
    }

    public enum SegmentType {
        Quest(ScoreboardModel.QUEST_TRACK_PATTERN),
        Party(ScoreboardModel.PARTY_PATTERN),
        Objective(ScoreboardModel.OBJECTIVE_HEADER_PATTERN),
        GuildObjective(ScoreboardModel.GUILD_OBJECTIVE_HEADER_PATTERN),
        GuildAttackTimer(ScoreboardModel.GUILD_ATTACK_UPCOMING_PATTERN);

        private final Pattern headerPattern;

        SegmentType(Pattern headerPattern) {
            this.headerPattern = headerPattern;
        }

        public Pattern getHeaderPattern() {
            return headerPattern;
        }
    }
}
