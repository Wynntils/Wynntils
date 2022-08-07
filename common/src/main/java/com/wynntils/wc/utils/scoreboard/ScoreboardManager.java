/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils.scoreboard;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.ScoreboardSetScoreEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.objects.Pair;
import com.wynntils.wc.event.ScoreboardSegmentAdditionEvent;
import com.wynntils.wc.event.WorldStateEvent;
import com.wynntils.wc.model.WorldState;
import com.wynntils.wc.utils.WynnUtils;
import com.wynntils.wc.utils.scoreboard.objectives.ObjectiveManager;
import com.wynntils.wc.utils.scoreboard.quests.QuestManager;
import java.util.ArrayList;
import java.util.Comparator;
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
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ScoreboardManager {
    private static final Pattern GUILD_ATTACK_UPCOMING_PATTERN = Pattern.compile("Upcoming Attacks:");
    private static final Pattern QUEST_TRACK_PATTERN = Pattern.compile("Tracked Quest:");
    private static final Pattern OBJECTIVE_HEADER_PATTERN = Pattern.compile("(★ )?(Daily )?Objectives?:");
    private static final Pattern GUILD_OBJECTIVE_HEADER_PATTERN = Pattern.compile("(★ )?Guild Obj: (.+)");
    private static final Pattern PARTY_PATTERN = Pattern.compile("Party:\\s\\[Lv. (\\d+)]");

    // TimeUnit.MILLISECONDS
    // 250 -> 4 times a second
    private static final int CHANGE_PROCESS_RATE = 250;

    private static List<ScoreboardLine> reconstructedScoreboard = new ArrayList<>();

    private static List<Segment> segments = new ArrayList<>();

    private static final LinkedList<ScoreboardLineChange> queuedChanges = new LinkedList<>();

    private static final List<Pair<ScoreboardHandler, Set<SegmentType>>> scoreboardHandlers = new ArrayList<>();

    private static ScheduledExecutorService executor = null;

    private static final Runnable changeHandlerRunnable = () -> {
        if (queuedChanges.isEmpty()) {
            handleScoreboardReconstruction();
            return;
        }

        List<ScoreboardLine> scoreboardCopy = new ArrayList<>(reconstructedScoreboard);

        for (ScoreboardLineChange queuedChange : queuedChanges) {
            if (queuedChange.method() == ServerScoreboard.Method.REMOVE) {
                scoreboardCopy.removeIf(
                        scoreboardLine -> Objects.equals(scoreboardLine.line(), queuedChange.lineText()));
            }
        }

        Set<String> changedLines = new HashSet<>();
        while (!queuedChanges.isEmpty()) {
            ScoreboardLineChange processed = queuedChanges.pop();

            if (processed.method() == ServerScoreboard.Method.REMOVE) {
                continue;
            }

            scoreboardCopy.removeIf(scoreboardLine -> Objects.equals(scoreboardLine.line(), processed.lineText()));

            ScoreboardLine line = new ScoreboardLine(processed.lineText(), processed.lineIndex());
            scoreboardCopy.add(line);
            changedLines.add(processed.lineText());
        }

        scoreboardCopy.sort(Comparator.comparingInt(ScoreboardLine::index).reversed());

        List<Segment> parsedSegments = calculateSegments(scoreboardCopy);

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

        List<Segment> removedSegments = segments.stream()
                .filter(segment -> parsedSegments.stream().noneMatch(parsed -> parsed.getType() == segment.getType()))
                .toList();

        reconstructedScoreboard = scoreboardCopy;
        segments = parsedSegments;

        for (Segment segment : removedSegments) {
            for (Pair<ScoreboardHandler, Set<SegmentType>> scoreboardHandler : scoreboardHandlers) {
                if (scoreboardHandler.b.contains(segment.getType())) {
                    scoreboardHandler.a.onSegmentRemove(segment, segment.getType());
                }
            }
        }

        for (Segment segment :
                parsedSegments.stream().filter(Segment::isChanged).toList()) {
            for (Pair<ScoreboardHandler, Set<SegmentType>> scoreboardHandler : scoreboardHandlers) {
                if (scoreboardHandler.b.contains(segment.getType())) {
                    scoreboardHandler.a.onSegmentChange(segment, segment.getType());
                }
            }
        }

        handleScoreboardReconstruction();
    };

    private static void handleScoreboardReconstruction() {
        McUtils.mc().doRunTask(() -> {
            Scoreboard scoreboard = McUtils.player().getScoreboard();

            List<String> skipped = new ArrayList<>();

            for (Segment parsedSegment : segments) {
                boolean cancelled = WynntilsMod.getEventBus().post(new ScoreboardSegmentAdditionEvent(parsedSegment));

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
                        new TextComponent("play.wynncraft.com")
                                .withStyle(ChatFormatting.GOLD)
                                .withStyle(ChatFormatting.BOLD),
                        ObjectiveCriteria.RenderType.INTEGER);
            }

            scoreboard.setDisplayObjective(1, objective);

            for (Map<Objective, Score> scoreMap : scoreboard.playerScores.values()) {
                scoreMap.remove(objective);
            }

            // Filter and skip leading empty lines
            List<ScoreboardLine> toBeAdded = reconstructedScoreboard.stream()
                    .filter(scoreboardLine -> !skipped.contains(scoreboardLine.line()))
                    .dropWhile(scoreboardLine -> scoreboardLine.line().matches("À+"))
                    .toList();

            boolean allEmpty = true;

            // Skip trailing empty lines
            for (int i = toBeAdded.size() - 1; i >= 0; i--) {
                if (allEmpty && toBeAdded.get(i).line().matches("À+")) {
                    continue;
                }

                allEmpty = false;
                Score score = scoreboard.getOrCreatePlayerScore(toBeAdded.get(i).line(), objective);
                score.setScore(toBeAdded.get(i).index());
            }
        });
    }

    private static List<Segment> calculateSegments(List<ScoreboardLine> scoreboardCopy) {
        List<Segment> segments = new ArrayList<>();

        Segment currentSegment = null;

        for (int i = 0; i < scoreboardCopy.size(); i++) {
            String strippedLine =
                    ChatFormatting.stripFormatting(scoreboardCopy.get(i).line());

            if (strippedLine == null) {
                continue;
            }

            if (strippedLine.matches("À+")) {
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

            for (SegmentType value : SegmentType.values()) {
                if (!value.getHeaderPattern().matcher(strippedLine).matches()) continue;

                if (currentSegment != null) {
                    if (currentSegment.getType() != value) {
                        WynntilsMod.error(
                                "ScoreboardManager: currentSegment was not null and SegmentType was mismatched. We might have skipped a scoreboard category.");
                    }
                    continue;
                }

                currentSegment = new Segment(value, scoreboardCopy.get(i).line(), i);
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

    public static void init() {
        WynntilsMod.getEventBus().register(ScoreboardManager.class);

        registerHandler(new ObjectiveManager(), Set.of(SegmentType.Objective, SegmentType.GuildObjective));
        registerHandler(new QuestManager(), SegmentType.Quest);
    }

    private static void registerHandler(ScoreboardHandler handlerInstance, SegmentType segmentType) {
        registerHandler(handlerInstance, Set.of(segmentType));
    }

    private static void registerHandler(ScoreboardHandler handlerInstance, Set<SegmentType> segmentTypes) {
        scoreboardHandlers.add(new Pair<>(handlerInstance, segmentTypes));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSetScore(ScoreboardSetScoreEvent event) {
        if (!WynnUtils.onServer()) return;

        event.setCanceled(true);

        queuedChanges.add(new ScoreboardLineChange(event.getOwner(), event.getMethod(), event.getScore()));
    }

    @SubscribeEvent
    public static void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.State.WORLD) {
            executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(changeHandlerRunnable, 0, CHANGE_PROCESS_RATE, TimeUnit.MILLISECONDS);
            return;
        }

        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        queuedChanges.clear();
        reconstructedScoreboard.clear();
        segments.clear();

        ObjectiveManager.resetObjectives();
        QuestManager.resetCurrentQuest();
    }

    public enum SegmentType {
        Quest(ScoreboardManager.QUEST_TRACK_PATTERN),
        Party(ScoreboardManager.PARTY_PATTERN),
        Objective(ScoreboardManager.OBJECTIVE_HEADER_PATTERN),
        GuildObjective(ScoreboardManager.GUILD_OBJECTIVE_HEADER_PATTERN),
        GuildAttackTimer(ScoreboardManager.GUILD_ATTACK_UPCOMING_PATTERN);

        private final Pattern headerPattern;

        SegmentType(Pattern headerPattern) {
            this.headerPattern = headerPattern;
        }

        public Pattern getHeaderPattern() {
            return headerPattern;
        }
    }
}
