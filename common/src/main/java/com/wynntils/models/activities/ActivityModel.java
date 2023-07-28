/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.ui.WynntilsContentBookFeature;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.models.activities.caves.CaveInfo;
import com.wynntils.models.activities.event.ActivityTrackerUpdatedEvent;
import com.wynntils.models.activities.event.DialogueHistoryReloadedEvent;
import com.wynntils.models.activities.quests.QuestInfo;
import com.wynntils.models.activities.type.ActivityDifficulty;
import com.wynntils.models.activities.type.ActivityDistance;
import com.wynntils.models.activities.type.ActivityInfo;
import com.wynntils.models.activities.type.ActivityLength;
import com.wynntils.models.activities.type.ActivityRequirements;
import com.wynntils.models.activities.type.ActivityStatus;
import com.wynntils.models.activities.type.ActivityTrackingState;
import com.wynntils.models.activities.type.ActivityType;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/* An "Activity" is the name we've given to the kind of stuff that appears in the Wynncraft
 * "Content Book". In user fronting text, it could be referred to as a "Content Book Activity",
 * to make the connection to the Content Book clear.
 *
 * Examples of activities are quests, caves, dungeons and raids, but also the various
 * discoveries.
 */
public final class ActivityModel extends Model {
    public static final String CONTENT_BOOK_TITLE = "§f\uE000\uE072";

    private static final Pattern LEVEL_REQ_PATTERN =
            Pattern.compile("^§(.).À?§7(?: Recommended)? Combat Lv(?:\\. Min)?: (\\d+)$");
    private static final Pattern PROFESSION_REQ_PATTERN = Pattern.compile("^§(.).À?§7 (\\w+)? Lv\\. Min: (\\d+)$");
    private static final Pattern QUEST_REQ_PATTERN = Pattern.compile("^§(.).À?§7 Quest Req: (.+)$");
    private static final Pattern DISTANCE_PATTERN = Pattern.compile("^   §7Distance: §.(\\w*)(?:§8 \\((.+)\\))?$");
    private static final Pattern LENGTH_PATTERN = Pattern.compile("^   §7Length: (\\w*)(?:§8 \\((.+)\\))?$");
    private static final Pattern DIFFICULTY_PATTERN = Pattern.compile("^   §7Difficulty: (\\w*)$");
    private static final Pattern REWARD_HEADER_PATTERN = Pattern.compile("^   §dRewards:$");
    private static final Pattern REWARD_PATTERN = Pattern.compile("^   §d- §7\\+?(.*)$");
    private static final Pattern TRACKING_PATTERN = Pattern.compile("^ *À*§.§lCLICK TO (UN)?TRACK$");
    private static final Pattern OVERALL_PROGRESS_PATTERN = Pattern.compile("^\\s*À*§7(\\d+) of (\\d+) completed$");

    private static final ScoreboardPart TRACKER_SCOREBOARD_PART = new ActivityTrackerScoreboardPart();
    private static final ContentBookQueries CONTAINER_QUERIES = new ContentBookQueries();
    private static final DialogueHistoryQueries DIALOGUE_HISTORY_QUERIES = new DialogueHistoryQueries();

    private TrackedActivity trackedActivity;
    private List<List<StyledText>> dialogueHistory = List.of();
    private CappedValue overallProgress = CappedValue.EMPTY;

    public ActivityModel() {
        super(List.of());

        Handlers.Scoreboard.addPart(TRACKER_SCOREBOARD_PART);
    }

    public ActivityInfo parseItem(String name, ActivityType type, ItemStack itemStack) {
        LinkedList<StyledText> lore = LoreUtils.getLore(itemStack);

        String statusLine = lore.pop().getString();
        if (statusLine.charAt(0) != '§') return null;

        ActivityStatus status = ActivityStatus.from(statusLine.charAt(1), itemStack.getItem());
        int specialInfoEnd = statusLine.indexOf(" - ");
        // If we have a specialInfo, skip the §x marker in the beginning, and keep everything
        // until the " - " comes. Examples of specialInfo can be "Unlocks Dungeon" or
        // "Storyline" (on most, but not all (!) storyline quests), or "Wynn Plains" (for
        // discoveries).
        String specialInfo = specialInfoEnd != -1 ? statusLine.substring(2, specialInfoEnd) : null;
        if (!lore.pop().isEmpty()) return null;

        Pair<Integer, Boolean> levelReq = Pair.of(0, true);
        ActivityDistance distance = null;
        String distanceInfo = null;
        ActivityLength length = null;
        String lengthInfo = null;
        ActivityDifficulty difficulty = null;
        ActivityTrackingState trackingState = ActivityTrackingState.UNTRACKABLE;
        List<Pair<Pair<ProfessionType, Integer>, Boolean>> professionLevels = new ArrayList<>();
        List<Pair<String, Boolean>> quests = new ArrayList<>();
        List<String> rewards = new ArrayList<>();
        List<StyledText> descriptionLines = new ArrayList<>();

        for (StyledText line : lore) {
            // Must be tested before profession requirement pattern
            Matcher levelReqMatcher = line.getMatcher(LEVEL_REQ_PATTERN);
            if (levelReqMatcher.matches()) {
                boolean fulfilled = isFulfilled(levelReqMatcher);
                int level = Integer.parseInt(levelReqMatcher.group(2));
                levelReq = Pair.of(level, fulfilled);
                continue;
            }

            Matcher professionReqMatcher = line.getMatcher(PROFESSION_REQ_PATTERN);
            if (professionReqMatcher.matches()) {
                boolean fulfilled = isFulfilled(professionReqMatcher);
                ProfessionType profession = ProfessionType.fromString(professionReqMatcher.group(2));
                int level = Integer.parseInt(professionReqMatcher.group(3));
                professionLevels.add(Pair.of(Pair.of(profession, level), fulfilled));
                continue;
            }

            Matcher questReqMatcher = line.getMatcher(QUEST_REQ_PATTERN);
            if (questReqMatcher.matches()) {
                boolean fulfilled = isFulfilled(questReqMatcher);
                String quest = questReqMatcher.group(2);
                quests.add(Pair.of(quest, fulfilled));
                continue;
            }

            Matcher distanceMatcher = line.getMatcher(DISTANCE_PATTERN);
            if (distanceMatcher.matches()) {
                distance = ActivityDistance.from(distanceMatcher.group(1));
                distanceInfo = distanceMatcher.group(2);
                continue;
            }

            Matcher lengthMatcher = line.getMatcher(LENGTH_PATTERN);
            if (lengthMatcher.matches()) {
                length = ActivityLength.from(lengthMatcher.group(1));
                lengthInfo = lengthMatcher.group(2);
                continue;
            }

            Matcher difficultyMatcher = line.getMatcher(DIFFICULTY_PATTERN);
            if (difficultyMatcher.matches()) {
                difficulty = ActivityDifficulty.from(difficultyMatcher.group(1));
                continue;
            }

            Matcher rewardHeaderMatcher = line.getMatcher(REWARD_HEADER_PATTERN);
            if (rewardHeaderMatcher.matches()) {
                // Just ignore the header
                continue;
            }

            Matcher rewardMatcher = line.getMatcher(REWARD_PATTERN);
            if (rewardMatcher.matches()) {
                rewards.add(rewardMatcher.group(1));
                continue;
            }

            Matcher trackingMatcher = line.getMatcher(TRACKING_PATTERN);
            if (trackingMatcher.matches()) {
                trackingState = trackingMatcher.group(1) == null
                        ? ActivityTrackingState.TRACKABLE
                        : ActivityTrackingState.TRACKED;
                continue;
            }

            if (line.isEmpty()) continue;

            // For all other lines, append it to the description
            descriptionLines.add(line);
        }

        StyledText description = StyledTextUtils.joinLines(descriptionLines).getNormalized();
        if (description.isEmpty()) {
            description = null;
        }

        ActivityRequirements requirements = new ActivityRequirements(levelReq, professionLevels, quests);
        return new ActivityInfo(
                type,
                name,
                status,
                Optional.ofNullable(specialInfo),
                Optional.ofNullable(description),
                Optional.ofNullable(length),
                Optional.ofNullable(lengthInfo),
                Optional.ofNullable(distance),
                Optional.ofNullable(distanceInfo),
                Optional.ofNullable(difficulty),
                requirements,
                rewards,
                trackingState);
    }

    private boolean isFulfilled(Matcher colorCodeMatcher) {
        // Check if the requirement is colored green
        return colorCodeMatcher.group(1).charAt(0) == ChatFormatting.GREEN.getChar();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWorldStateChanged(WorldStateEvent e) {
        resetTracker();
    }

    @SubscribeEvent
    public void onCharacterUpdated(CharacterUpdateEvent event) {
        // First thing to do when we just loaded a class
        scanOverallProgress();
    }

    public CappedValue getOverallProgress() {
        return overallProgress;
    }

    public String getTrackedName() {
        if (trackedActivity == null) return "";

        return trackedActivity.trackedName();
    }

    public ActivityType getTrackedType() {
        if (trackedActivity == null) return null;
        return trackedActivity.trackedType();
    }

    public StyledText getTrackedTask() {
        if (trackedActivity == null) return StyledText.EMPTY;

        return trackedActivity.trackedTask();
    }

    public Location getTrackedLocation() {
        if (trackedActivity == null) return null;

        return StyledTextUtils.extractLocation(trackedActivity.trackedTask()).orElse(null);
    }

    public QuestInfo getTrackedQuestInfo() {
        if (trackedActivity == null) return null;

        return Models.Quest.getQuestInfoFromName(trackedActivity.trackedName()).orElse(null);
    }

    public CaveInfo getTrackedCaveInfo() {
        if (trackedActivity == null) return null;

        return Models.Cave.getCaveInfoFromName(trackedActivity.trackedName()).orElse(null);
    }

    void updateTracker(String name, String type, StyledText nextTask) {
        trackedActivity = new TrackedActivity(name, ActivityType.from(type), nextTask);

        WynntilsMod.postEvent(new ActivityTrackerUpdatedEvent(
                trackedActivity.trackedType(), trackedActivity.trackedName(), trackedActivity.trackedTask()));
    }

    void resetTracker() {
        trackedActivity = null;
    }

    public void scanContentBook(
            ActivityType activityType, BiConsumer<List<ActivityInfo>, List<StyledText>> processResult) {
        // Feature dependency until Model configs
        boolean showUpdates = Managers.Feature.getFeatureInstance(WynntilsContentBookFeature.class)
                .showContentBookLoadingUpdates
                .get();
        CONTAINER_QUERIES.queryContentBook(activityType, processResult, showUpdates, false);
    }

    public void startTracking(String name, ActivityType activityType) {
        CONTAINER_QUERIES.toggleTracking(name, activityType);
    }

    public void stopTracking() {
        CONTAINER_QUERIES.toggleTracking(trackedActivity.trackedName(), trackedActivity.trackedType());
    }

    public boolean isTracking() {
        return trackedActivity != null;
    }

    public List<List<StyledText>> getDialogueHistory() {
        return dialogueHistory;
    }

    public void rescanDialogueHistory() {
        DIALOGUE_HISTORY_QUERIES.scanDialogueHistory();
    }

    public void scanOverallProgress() {
        CONTAINER_QUERIES.queryContentBook(
                ActivityType.ALL,
                (ignored, progress) -> {
                    for (StyledText line : progress) {
                        Matcher m = line.getMatcher(OVERALL_PROGRESS_PATTERN);
                        if (m.matches()) {
                            int completed = Integer.parseInt(m.group(1));
                            int total = Integer.parseInt(m.group(2));
                            overallProgress = new CappedValue(completed, total);
                            return;
                        }
                    }
                },
                false,
                true);
    }

    void setDialogueHistory(List<List<StyledText>> newDialogueHistory) {
        dialogueHistory = newDialogueHistory;
        WynntilsMod.postEvent(new DialogueHistoryReloadedEvent());
    }

    private record TrackedActivity(String trackedName, ActivityType trackedType, StyledText trackedTask) {}
}
