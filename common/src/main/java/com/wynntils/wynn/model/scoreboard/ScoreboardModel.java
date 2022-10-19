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
import com.wynntils.utils.Pair;
import com.wynntils.wynn.event.ScoreboardSegmentAdditionEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import com.wynntils.wynn.model.quests.QuestManager;
import com.wynntils.wynn.model.scoreboard.objectives.ObjectiveHandler;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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

    private static boolean firstExecution = false;

    private static final Runnable changeHandlerRunnable = () -> {
        if (!WynnUtils.onWorld() || McUtils.player() == null) return;

        if (queuedChanges.isEmpty()) {
            handleScoreboardReconstruction();
            return;
        }

        List<ScoreboardLine> scoreboardCopy = new ArrayList<>(reconstructedScoreboard);

        reconstructedScoreboard = processQueuedChanges(queuedChanges, scoreboardCopy);


        List<Segment> parsedSegments = calculateSegments(scoreboardCopy);

        queuedChanges.clear();

        // TODO changedLines
        /*
        for (String changedString : changedLines) {
            int changedLine =
                    scoreboardCopy.stream().map(ScoreboardLine::line).toList().indexOf(changedString);

            if (changedLine == -1) {
                continue;
            }

            Optional<Segment> foundSegment = parsedSegments.stream()
                    .filter(segment -> segment.getStartIndex() <= changedLine
                            && segment.getEndIndex() >= changedLine
                            && !segment.isChanged())
                    .findFirst();

            if (foundSegment.isEmpty()) {
                continue;
            }

            // Prevent bugs where content was not changed, but rather replaced to prepare room for other segment updates
            //  (Objective -> Daily Objective update)
            Optional<Segment> oldMatchingSegment = segments.stream()
                    .filter(segment -> segment.getType() == foundSegment.get().getType())
                    .findFirst();

            if (oldMatchingSegment.isEmpty()) {
                foundSegment.get().setChanged(true);
                continue;
            }

            if (!oldMatchingSegment.get().getContent().equals(foundSegment.get().getContent())
                    || !Objects.equals(
                            oldMatchingSegment.get().getHeader(),
                            foundSegment.get().getHeader())) {
                foundSegment.get().setChanged(true);
            }
        }

         */

        List<Segment> removedSegments = segments.stream()
                .filter(segment -> parsedSegments.stream().noneMatch(parsed -> parsed.getType() == segment.getType()))
                .toList();

        reconstructedScoreboard = scoreboardCopy;
        segments = parsedSegments;

        for (Segment segment : removedSegments) {
            for (ScoreboardHandler scoreboardHandler : scoreboardHandlers) {
                if (scoreboardHandler.handledSegments().contains(segment.getType())) {
                    scoreboardHandler.onSegmentRemove(segment, segment.getType());
                }
            }
        }

        List<Segment> changedSegments;

        if (firstExecution) {
            firstExecution = false;
            changedSegments = parsedSegments.stream().toList();
        } else {
            changedSegments = parsedSegments.stream().filter(Segment::isChanged).toList();
        }

        for (Segment segment : changedSegments) {
            for (ScoreboardHandler scoreboardHandler : scoreboardHandlers) {
                if (scoreboardHandler.handledSegments().contains(segment.getType())) {
                    scoreboardHandler.onSegmentChange(segment, segment.getType());
                }
            }
        }

        handleScoreboardReconstruction();
    };

    private static List<ScoreboardLine> processQueuedChanges(List<ScoreboardLineChange> queuedChanges, List<ScoreboardLine> scoreboard) {
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

    private static void handleScoreboardReconstruction() {
        McUtils.mc().doRunTask(() -> {
            List<String> skipped = new ArrayList<>();

            for (Segment parsedSegment : segments) {
                boolean cancelled = WynntilsMod.postEvent(new ScoreboardSegmentAdditionEvent(parsedSegment));

                if (cancelled) {
                    skipped.addAll(parsedSegment.getScoreboardLines());
                }
            }

            // Filter and remove leading and trailining empty lines
            List<ScoreboardLine> toBeAdded = reconstructedScoreboard.stream()
                    .filter(scoreboardLine -> !skipped.contains(scoreboardLine.line()))
                    .toList();

            // index of first and last nonempty lines
            int frontIndex = -1;
            int backIndex = -1;

            for (int i = 0; i < toBeAdded.size(); i++) {
                if (toBeAdded.get(i).line().matches("À+")) continue;

                frontIndex = i;
                break;
            }

            if (frontIndex == -1) { // check if whole scoreboard is empty
                overrideScoreboard(new ArrayList<>());
                return;
            }

            for (int i = toBeAdded.size() - 1; i >= 0; i--) {
                if (toBeAdded.get(i).line().matches("À+")) continue;

                backIndex = i;
                break;
            }

            overrideScoreboard(toBeAdded.subList(frontIndex, backIndex + 1));
        });
    }

    private static void overrideScoreboard(List<ScoreboardLine> lines) {
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

        for (ScoreboardLine scoreboardLine : lines) {
            Score score = scoreboard.getOrCreatePlayerScore(scoreboardLine.line(), objective);
            score.setScore(scoreboardLine.index());
        }
    }

    private static List<Segment> calculateSegments(List<ScoreboardLine> scoreboardCopy) {
        List<Segment> segments = new ArrayList<>();

        List<String> scoreboardLines =
                scoreboardCopy.stream().map(ScoreboardLine::line).toList();

        Segment currentSegment = null;

        for (int i = 0; i < scoreboardCopy.size(); i++) {
            String line = scoreboardLines.get(i);

            String strippedLine = ComponentUtils.stripFormatting(line);

            if (strippedLine == null) {
                continue;
            }

            if (strippedLine.matches("À+")) { // Segment complete
                if (currentSegment != null) {
                    // Sublisting is not a problem as no overlap
                    currentSegment.setContent(scoreboardLines.subList(currentSegment.getStartIndex() + 1, i));
                    currentSegment.setEndIndex(i - 1);
                    currentSegment.setEnd(strippedLine);
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
                    currentSegment = new Segment(type, line, i);
                    continue;
                }

                WynntilsMod.error(String.format("Failed to parse header of %s", strippedLine));
            }
        }

        if (currentSegment != null) {
            currentSegment.setContent(scoreboardLines.subList(currentSegment.getStartIndex(), scoreboardCopy.size()));
            currentSegment.setEndIndex(scoreboardCopy.size() - 1);
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
        firstExecution = true;
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(changeHandlerRunnable, 0, CHANGE_PROCESS_RATE, TimeUnit.MILLISECONDS);
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
