/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.scoreboard;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;
import com.wynntils.models.lootrun.type.LootrunTaskType;
import com.wynntils.models.lootrun.type.LootrunningState;
import com.wynntils.models.lootrun.type.MissionType;
import com.wynntils.models.lootrun.type.TrialType;
import com.wynntils.utils.type.CappedValue;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LootrunScoreboardPart extends ScoreboardPart {
    private static final Pattern CHOOSE_BEACON_PATTERN = Pattern.compile("^Choose a beacon!$");

    // This pattern indirectly indicates that we're done with the lootrun
    private static final Pattern WARPING_BACK_PATTERN = Pattern.compile("^Warping back to camp!$");

    private static final Pattern LOOT_PATTERN = Pattern.compile("^Loot (\\d)/(\\d) chests!$");
    private static final Pattern SLAY_PATTERN = Pattern.compile("^Slay! Wave (\\d) [-—] (\\d) (Target|Mob)s? Left!$");
    private static final Pattern DESTROY_PATTERN = Pattern.compile("^Destroy the objective!$");
    private static final Pattern DEFEND_PATTERN = Pattern.compile("^Defend for (\\d+)s!$");

    private static final Pattern TIMER_PATTERN =
            Pattern.compile("^[-—] Time Left: (\\d+):(\\d+)(?: \\[[+-]\\d+[msMS]\\])?$");
    private static final Pattern CHALLENGES_PATTERN =
            Pattern.compile("^[-—] Challenges: (\\d+)/(\\d+)(?: \\[[+-]\\d+\\])?$");
    private static final Pattern MISSION_AND_TRIAL_NAME_PATTERN = Pattern.compile("§.(?<name>[^:]+):");
    private static final Pattern MISSION_AND_TRIAL_OBJECTIVE_PATTERN = Pattern.compile(
            "§.- (?:§.)?(?<objectiveStart>.+) (?:§.)?(?<current>[0-9]+)/(?<total>[0-9]+)m?(?:§.)? (?<objectiveEnd>.+)");

    @Override
    public SegmentMatcher getSegmentMatcher() {
        return SegmentMatcher.fromPattern("Lootrun:");
    }

    @Override
    public void onSegmentChange(ScoreboardSegment newValue) {
        List<StyledText> content = newValue.getContent();

        if (newValue.getContent().isEmpty()) {
            WynntilsMod.warn("Lootrun scoreboard segment content is empty; we might be in a bad state");
            return;
        }

        StyledText currentStateLine = content.getFirst();

        if (currentStateLine.matches(CHOOSE_BEACON_PATTERN, StyleType.NONE)) {
            Models.Lootrun.setState(LootrunningState.CHOOSING_BEACON, null);
        } else if (currentStateLine.matches(WARPING_BACK_PATTERN, StyleType.NONE)) {
            Models.Lootrun.setState(LootrunningState.NOT_RUNNING, null);
        } else if (currentStateLine.matches(LOOT_PATTERN, StyleType.NONE)) {
            Models.Lootrun.setState(LootrunningState.IN_TASK, LootrunTaskType.LOOT);
        } else if (currentStateLine.matches(SLAY_PATTERN, StyleType.NONE)) {
            Models.Lootrun.setState(LootrunningState.IN_TASK, LootrunTaskType.SLAY);
        } else if (currentStateLine.matches(DESTROY_PATTERN, StyleType.NONE)) {
            Models.Lootrun.setState(LootrunningState.IN_TASK, LootrunTaskType.DESTROY);
        } else if (currentStateLine.matches(DEFEND_PATTERN, StyleType.NONE)) {
            Models.Lootrun.setState(LootrunningState.IN_TASK, LootrunTaskType.DEFEND);
        }

        if (content.size() < 2) {
            WynntilsMod.warn(
                    "Lootrun scoreboard segment content is too short; less than 2; we might be in a bad state");
            return;
        }

        StyledText timeRemainingLine = content.get(1);
        Matcher matcher = timeRemainingLine.getMatcher(TIMER_PATTERN, StyleType.NONE);
        if (matcher.matches()) {
            Models.Lootrun.setTimeLeft(Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2)));
        }

        if (content.size() < 3) {
            WynntilsMod.warn(
                    "Lootrun scoreboard segment content is too short; less than 3; we might be in a bad state");
            return;
        }

        StyledText challengesLine = content.get(2);
        matcher = challengesLine.getMatcher(CHALLENGES_PATTERN, StyleType.NONE);
        if (matcher.matches()) {
            Models.Lootrun.setChallenges(
                    new CappedValue(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))));
        }

        // Mission and Trial objectives may take up multiple lines
        List<String> currentMissionObjective = new ArrayList<>();
        List<CappedValue> currentMissionProgress = new ArrayList<>();
        List<String> currentTrialObjective = new ArrayList<>();
        List<CappedValue> currentTrialProgress = new ArrayList<>();
        String currentMission = "";
        String currentTrial = "";
        MissionAndTrialState state = MissionAndTrialState.UNKNOWN;
        for (int i = 3; i < content.size(); i++) {
            StyledText line = content.get(i);
            if (line.isBlank()) continue;
            matcher = line.getMatcher(MISSION_AND_TRIAL_NAME_PATTERN);
            if (matcher.matches()) {
                String name = matcher.group("name");
                if (MissionType.fromName(name) != MissionType.UNKNOWN) {
                    currentMission = name;
                    state = MissionAndTrialState.MISSION;
                } else if (TrialType.fromName(name) != TrialType.UNKNOWN) {
                    currentTrial = name;
                    state = MissionAndTrialState.TRIAL;
                } else {
                    WynntilsMod.warn("Found unknown Lootrun Mission or Trial name: " + line.getString());
                    state = MissionAndTrialState.UNKNOWN;
                }
                continue;
            }
            matcher = line.getMatcher(MISSION_AND_TRIAL_OBJECTIVE_PATTERN);
            if (matcher.matches()) {
                String name = matcher.group("objectiveStart") + " " + matcher.group("objectiveEnd");
                CappedValue progress = new CappedValue(
                        Integer.parseInt(matcher.group("current")), Integer.parseInt(matcher.group("total")));
                if (state == MissionAndTrialState.MISSION) {
                    currentMissionObjective.add(name);
                    currentMissionProgress.add(progress);
                } else if (state == MissionAndTrialState.TRIAL) {
                    currentTrialObjective.add(name);
                    currentTrialProgress.add(progress);
                } else {
                    WynntilsMod.warn("Found unknown Lootrun Mission or Trial objective: " + line.getString());
                }
                continue;
            }
            // If we hit a line that's neither a Mission nor a Trial break immediately
            break;
        }
        Models.Lootrun.setCurrentMission(currentMission);
        Models.Lootrun.setCurrentTrial(currentTrial);
        Models.Lootrun.setCurrentMissionObjective(currentMissionObjective);
        Models.Lootrun.setCurrentMissionProgress(currentMissionProgress);
        Models.Lootrun.setCurrentTrialObjective(currentTrialObjective);
        Models.Lootrun.setCurrentTrialProgress(currentTrialProgress);
    }

    @Override
    public void onSegmentRemove(ScoreboardSegment segment) {
        // Do nothing here, we will know when the lootrun is over from patterns
    }

    @Override
    public void reset() {
        // Do nothing here, we will know when the lootrun is over from patterns
    }

    @Override
    public String toString() {
        return "LootrunScoreboardPart{}";
    }

    private enum MissionAndTrialState {
        UNKNOWN,
        MISSION,
        TRIAL
    }
}
