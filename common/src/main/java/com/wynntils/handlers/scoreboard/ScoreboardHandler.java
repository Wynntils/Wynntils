/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.scoreboard;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.scoreboard.event.ScoreboardSegmentAdditionEvent;
import com.wynntils.handlers.scoreboard.type.ScoreboardLine;
import com.wynntils.handlers.scoreboard.type.ScoreboardLineChange;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;
import com.wynntils.mc.event.ScoreboardSetScoreEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.StyledText;
import com.wynntils.utils.type.Pair;
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
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ScoreboardHandler extends Handler {
    // TimeUnit.MILLISECONDS
    // 250 -> 4 times a second
    private static final int CHANGE_PROCESS_RATE = 250;
    private static final Pattern EMPTY_LINE = Pattern.compile("À+");

    private List<ScoreboardLine> reconstructedScoreboard = new ArrayList<>();

    private List<ScoreboardSegment> segments = new ArrayList<>();

    private final LinkedList<ScoreboardLineChange> queuedChanges = new LinkedList<>();

    private final List<Pair<ScoreboardPart, Set<SegmentMatcher>>> scoreboardParts = new ArrayList<>();
    private List<SegmentMatcher> segmentMatchers = List.of();

    private ScheduledExecutorService executor = null;

    private boolean firstExecution = false;

    private void periodicTask() {
        // FIXME: Reverse dependency
        if (!Models.WorldState.onWorld() || McUtils.player() == null) return;

        if (queuedChanges.isEmpty()) {
            handleScoreboardReconstruction();
            return;
        }

        List<ScoreboardLine> scoreboardCopy = new ArrayList<>(reconstructedScoreboard);
        LinkedList<ScoreboardLineChange> queueCopy = new LinkedList<>(queuedChanges);
        queuedChanges.clear();

        Map<Integer, ScoreboardLine> scoreboardLineMap = new TreeMap<>();

        for (ScoreboardLine scoreboardLine : scoreboardCopy) {
            scoreboardLineMap.put(scoreboardLine.index(), scoreboardLine);
        }

        Set<StyledText> changedLines = new HashSet<>();
        while (!queueCopy.isEmpty()) {
            ScoreboardLineChange processed = queueCopy.pop();

            if (processed.method() == ServerScoreboard.Method.REMOVE) {
                for (ScoreboardLine lineToRemove : scoreboardLineMap.values().stream()
                        .filter(scoreboardLine -> Objects.equals(scoreboardLine.line(), processed.lineText()))
                        .toList()) {
                    scoreboardLineMap.remove(lineToRemove.index());
                }
            } else {
                ScoreboardLine line = new ScoreboardLine(processed.lineText(), processed.lineIndex());
                scoreboardLineMap.put(line.index(), line);
                changedLines.add(processed.lineText());
            }
        }

        scoreboardCopy.clear();

        scoreboardCopy = scoreboardLineMap.values().stream()
                .sorted(Comparator.comparing(ScoreboardLine::index).reversed())
                .collect(Collectors.toList());

        List<ScoreboardSegment> parsedSegments = calculateSegments(scoreboardCopy);

        for (StyledText changedString : changedLines) {
            int changedLine =
                    scoreboardCopy.stream().map(ScoreboardLine::line).toList().indexOf(changedString);

            if (changedLine == -1) {
                continue;
            }

            Optional<ScoreboardSegment> foundSegment = parsedSegments.stream()
                    .filter(segment -> segment.getStartIndex() <= changedLine
                            && segment.getEndIndex() >= changedLine
                            && !segment.isChanged())
                    .findFirst();

            if (foundSegment.isEmpty()) {
                continue;
            }

            // Prevent bugs where content was not changed, but rather replaced to prepare room for other segment updates
            //  (Objective -> Daily Objective update)
            Optional<ScoreboardSegment> oldMatchingSegment = segments.stream()
                    .filter(segment ->
                            segment.getMatcher() == foundSegment.get().getMatcher())
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

        List<ScoreboardSegment> removedSegments = segments.stream()
                .filter(segment ->
                        parsedSegments.stream().noneMatch(parsed -> parsed.getMatcher() == segment.getMatcher()))
                .toList();

        reconstructedScoreboard = scoreboardCopy;
        segments = parsedSegments;

        for (ScoreboardSegment segment : removedSegments) {
            for (Pair<ScoreboardPart, Set<SegmentMatcher>> scoreboardPart : scoreboardParts) {
                if (scoreboardPart.b().contains(segment.getMatcher())) {
                    scoreboardPart.a().onSegmentRemove(segment, segment.getMatcher());
                }
            }
        }

        List<ScoreboardSegment> changedSegments;

        if (firstExecution) {
            firstExecution = false;
            changedSegments = parsedSegments.stream().toList();
        } else {
            changedSegments =
                    parsedSegments.stream().filter(ScoreboardSegment::isChanged).toList();
        }

        for (ScoreboardSegment segment : changedSegments) {
            for (Pair<ScoreboardPart, Set<SegmentMatcher>> scoreboardPart : scoreboardParts) {
                if (scoreboardPart.b().contains(segment.getMatcher())) {
                    scoreboardPart.a().onSegmentChange(segment, segment.getMatcher());
                }
            }
        }

        handleScoreboardReconstruction();
    }

    private void handleScoreboardReconstruction() {
        McUtils.mc().doRunTask(() -> {
            Scoreboard scoreboard = McUtils.player().getScoreboard();

            List<StyledText> skipped = new ArrayList<>();

            for (ScoreboardSegment parsedSegment : segments) {
                boolean cancelled = WynntilsMod.postEvent(new ScoreboardSegmentAdditionEvent(parsedSegment));

                if (cancelled) {
                    skipped.addAll(parsedSegment.getScoreboardLines());
                }
            }

            final String objectiveName = "wynntilsSB" + McUtils.player().getScoreboardName();

            Objective objective = scoreboard.getObjective(objectiveName);

            if (objective == null) {
                objective = scoreboard.addObjective(
                        objectiveName,
                        ObjectiveCriteria.DUMMY,
                        Component.literal(" play.wynncraft.com")
                                .withStyle(ChatFormatting.GOLD)
                                .withStyle(ChatFormatting.BOLD),
                        ObjectiveCriteria.RenderType.INTEGER);
            }

            scoreboard.setDisplayObjective(1, objective);

            // Set player team display objective
            // This fixes scoreboard gui flickering
            PlayerTeam playerTeam = scoreboard.getPlayersTeam(McUtils.player().getScoreboardName());
            if (playerTeam != null) {
                if (playerTeam.getColor().getId() >= 0) {
                    int id = playerTeam.getColor().getId() + 3;
                    scoreboard.setDisplayObjective(id, objective);
                }
            }

            for (Map<Objective, Score> scoreMap : scoreboard.playerScores.values()) {
                scoreMap.remove(objective);
            }

            // Filter and skip leading empty lines
            List<ScoreboardLine> toBeAdded = reconstructedScoreboard.stream()
                    .filter(scoreboardLine -> !skipped.contains(scoreboardLine.line()))
                    .dropWhile(scoreboardLine ->
                            scoreboardLine.line().match(EMPTY_LINE).matches())
                    .toList();

            boolean allEmpty = true;

            // Skip trailing empty lines
            for (int i = toBeAdded.size() - 1; i >= 0; i--) {
                if (allEmpty && toBeAdded.get(i).line().match(EMPTY_LINE).matches()) {
                    continue;
                }

                allEmpty = false;
                Score score = scoreboard.getOrCreatePlayerScore(
                        toBeAdded.get(i).line().str(), objective);
                score.setScore(toBeAdded.get(i).index());
            }
        });
    }

    private List<ScoreboardSegment> calculateSegments(List<ScoreboardLine> scoreboardCopy) {
        List<ScoreboardSegment> segments = new ArrayList<>();

        ScoreboardSegment currentSegment = null;

        for (int i = 0; i < scoreboardCopy.size(); i++) {
            String strippedLine =
                    ComponentUtils.stripFormatting(scoreboardCopy.get(i).line());

            if (strippedLine == null) {
                continue;
            }

            if (EMPTY_LINE.matcher(strippedLine).matches()) {
                if (currentSegment != null) {
                    currentSegment.setContent(scoreboardCopy.stream()
                            .map(ScoreboardLine::line)
                            .collect(Collectors.toList())
                            .subList(currentSegment.getStartIndex() + 1, i));
                    currentSegment.setEndIndex(i - 1);
                    currentSegment.setEnd(strippedLine);
                    segments.add(currentSegment);
                    currentSegment = null;
                }

                continue;
            }

            for (SegmentMatcher value : segmentMatchers) {
                if (!value.headerPattern().matcher(strippedLine).matches()) continue;

                if (currentSegment != null) {
                    if (currentSegment.getMatcher() != value) {
                        WynntilsMod.error(
                                "ScoreboardModel: currentSegment was not null and SegmentMatcher was mismatched. We might have skipped a scoreboard category.");
                    }
                    continue;
                }

                currentSegment =
                        new ScoreboardSegment(value, scoreboardCopy.get(i).line(), i);
                break;
            }
        }

        if (currentSegment != null) {
            currentSegment.setContent(scoreboardCopy.stream()
                    .map(ScoreboardLine::line)
                    .collect(Collectors.toList())
                    .subList(currentSegment.getStartIndex(), scoreboardCopy.size()));
            currentSegment.setEndIndex(scoreboardCopy.size() - 1);
            segments.add(currentSegment);
        }

        return segments;
    }

    public void addPart(ScoreboardPart scoreboardPart) {
        scoreboardParts.add(new Pair<>(scoreboardPart, scoreboardPart.getSegmentMatchers()));
        updateSegmentMatchers();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSetScore(ScoreboardSetScoreEvent event) {
        queuedChanges.add(new ScoreboardLineChange(event.getOwner(), event.getMethod(), event.getScore()));
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.WORLD) {
            startThread();
            return;
        }

        resetState();
    }

    private void startThread() {
        firstExecution = true;
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(this::periodicTask, 0, CHANGE_PROCESS_RATE, TimeUnit.MILLISECONDS);
    }

    private void resetState() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        queuedChanges.clear();
        reconstructedScoreboard.clear();
        segments.clear();

        for (Pair<ScoreboardPart, Set<SegmentMatcher>> scoreboardPart : scoreboardParts) {
            scoreboardPart.a().reset();
        }
    }

    private void updateSegmentMatchers() {
        segmentMatchers =
                scoreboardParts.stream().flatMap(pair -> pair.b().stream()).toList();
    }
}
