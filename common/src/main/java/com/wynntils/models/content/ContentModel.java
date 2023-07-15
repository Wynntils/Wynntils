/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.models.content.event.ContentTrackerUpdatedEvent;
import com.wynntils.models.content.type.ContentDifficulty;
import com.wynntils.models.content.type.ContentDistance;
import com.wynntils.models.content.type.ContentInfo;
import com.wynntils.models.content.type.ContentLength;
import com.wynntils.models.content.type.ContentRequirements;
import com.wynntils.models.content.type.ContentStatus;
import com.wynntils.models.content.type.ContentTrackingState;
import com.wynntils.models.content.type.ContentType;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.models.quests.QuestInfo;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.mc.type.Location;
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

public final class ContentModel extends Model {
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

    private static final ScoreboardPart TRACKER_SCOREBOARD_PART = new ContentTrackerScoreboardPart();
    private static final ContentBookQueries CONTAINER_QUERIES = new ContentBookQueries();

    private TrackedContent trackedContent;

    public ContentModel() {
        super(List.of());

        Handlers.Scoreboard.addPart(TRACKER_SCOREBOARD_PART);
    }

    public ContentInfo parseItem(String name, ContentType type, ItemStack itemStack) {
        LinkedList<StyledText> lore = LoreUtils.getLore(itemStack);

        String statusLine = lore.pop().getString();
        if (statusLine.charAt(0) != '§') return null;

        ContentStatus status = ContentStatus.from(statusLine.charAt(1), itemStack.getItem());
        int specialInfoEnd = statusLine.indexOf(" - ");
        // If we have a specialInfo, skip the §x marker in the beginning, and keep everything
        // until the " - " comes. Examples of specialInfo can be "Unlocks Dungeon" or
        // "Storyline" (on most, but not all (!) storyline quests), or "Wynn Plains" (for
        // discoveries).
        String specialInfo = specialInfoEnd != -1 ? statusLine.substring(2, specialInfoEnd) : null;
        if (!lore.pop().isEmpty()) return null;

        Pair<Integer, Boolean> levelReq = Pair.of(0, true);
        ContentDistance distance = null;
        String distanceInfo = null;
        ContentLength length = null;
        String lengthInfo = null;
        ContentDifficulty difficulty = null;
        ContentTrackingState trackingState = ContentTrackingState.UNTRACKABLE;
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
                distance = ContentDistance.from(distanceMatcher.group(1));
                distanceInfo = distanceMatcher.group(2);
                continue;
            }

            Matcher lengthMatcher = line.getMatcher(LENGTH_PATTERN);
            if (lengthMatcher.matches()) {
                length = ContentLength.from(lengthMatcher.group(1));
                lengthInfo = lengthMatcher.group(2);
                continue;
            }

            Matcher difficultyMatcher = line.getMatcher(DIFFICULTY_PATTERN);
            if (difficultyMatcher.matches()) {
                difficulty = ContentDifficulty.from(difficultyMatcher.group(1));
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
                        ? ContentTrackingState.TRACKABLE
                        : ContentTrackingState.TRACKED;
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

        ContentRequirements requirements = new ContentRequirements(levelReq, professionLevels, quests);
        return new ContentInfo(
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

    public String getTrackedName() {
        if (trackedContent == null) return "";

        return trackedContent.trackedName();
    }

    public ContentType getTrackedType() {
        if (trackedContent == null) return null;
        return trackedContent.trackedType();
    }

    public StyledText getTrackedTask() {
        if (trackedContent == null) return StyledText.EMPTY;

        return trackedContent.trackedTask();
    }

    public Location getTrackedLocation() {
        if (trackedContent == null) return null;

        return StyledTextUtils.extractLocation(trackedContent.trackedTask()).orElse(null);
    }

    public QuestInfo getTrackedQuestInfo() {
        if (trackedContent == null) return null;

        return Models.Quest.getQuestInfoFromName(trackedContent.trackedName()).orElse(null);
    }

    void updateTracker(String name, String type, StyledText nextTask) {
        trackedContent = new TrackedContent(name, ContentType.from(type), nextTask);

        WynntilsMod.postEvent(new ContentTrackerUpdatedEvent(
                trackedContent.trackedType(), trackedContent.trackedName(), trackedContent.trackedTask()));
    }

    void resetTracker() {
        trackedContent = null;
    }

    public void scanContentBook(String filterName, BiConsumer<List<ContentInfo>, List<StyledText>> processResult) {
        CONTAINER_QUERIES.queryContentBook(filterName, processResult);
    }

    public void startTracking(String name, ContentType contentType) {
        CONTAINER_QUERIES.toggleTracking(name, contentType);
    }

    public void stopTracking() {
        CONTAINER_QUERIES.toggleTracking(trackedContent.trackedName(), trackedContent.trackedType());
    }

    public boolean isTracking() {
        return trackedContent != null;
    }

    private record TrackedContent(String trackedName, ContentType trackedType, StyledText trackedTask) {}
}
