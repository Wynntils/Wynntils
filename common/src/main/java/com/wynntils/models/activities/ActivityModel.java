/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities;

import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.combat.ContentTrackerFeature;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.extension.EntityExtension;
import com.wynntils.models.activities.beacons.ActivityBeaconKind;
import com.wynntils.models.activities.beacons.ActivityBeaconMarkerKind;
import com.wynntils.models.activities.caves.CaveInfo;
import com.wynntils.models.activities.event.ActivityTrackerUpdatedEvent;
import com.wynntils.models.activities.event.DialogueHistoryReloadedEvent;
import com.wynntils.models.activities.markers.ActivityMarkerProvider;
import com.wynntils.models.activities.quests.QuestInfo;
import com.wynntils.models.activities.type.ActivityDifficulty;
import com.wynntils.models.activities.type.ActivityDistance;
import com.wynntils.models.activities.type.ActivityInfo;
import com.wynntils.models.activities.type.ActivityLength;
import com.wynntils.models.activities.type.ActivityRequirements;
import com.wynntils.models.activities.type.ActivityRewardType;
import com.wynntils.models.activities.type.ActivityStatus;
import com.wynntils.models.activities.type.ActivityTrackingState;
import com.wynntils.models.activities.type.ActivityType;
import com.wynntils.models.activities.type.WorldEventFastTravelStatus;
import com.wynntils.models.beacons.event.BeaconEvent;
import com.wynntils.models.beacons.event.BeaconMarkerEvent;
import com.wynntils.models.beacons.type.Beacon;
import com.wynntils.models.beacons.type.BeaconMarker;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.containers.containers.ContentBookContainer;
import com.wynntils.models.marker.MarkerModel;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.screens.activities.ContentBookHolder;
import com.wynntils.screens.maps.MainMapScreen;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

/* An "Activity" is the name we've given to the kind of stuff that appears in the Wynncraft
 * "Content Book". In user fronting text, it could be referred to as a "Content Book Activity",
 * to make the connection to the Content Book clear.
 *
 * Examples of activities are quests, caves, dungeons and raids, but also the various
 * discoveries.
 */
public final class ActivityModel extends Model {
    public static final String CONTENT_BOOK_TITLE = "\uDAFF\uDFEE\uE004";
    private static final String WIKI_APOSTROPHE = "&#039;";
    private static final String PLAYER_PROGRESS_ITEM_NAME = "All Player Progress";

    private static final Pattern LEVEL_REQ_PATTERN =
            Pattern.compile("^§(.).À?§7(?: Recommended)? Combat Lv(?:\\. Min)?: (\\d+)$");
    private static final Pattern PROFESSION_REQ_PATTERN = Pattern.compile("^§(.).À?§7 (\\w+)? Lv\\. Min: (\\d+)$");
    private static final Pattern QUEST_REQ_PATTERN = Pattern.compile("^§(.).À?§7 Quest Req: (.+)$");
    private static final Pattern DISTANCE_PATTERN =
            Pattern.compile("^\uDB00\uDC0E§7Distance: §.([\\w\\s]*)(?:§8 \\((.+)\\))?$");
    private static final Pattern LENGTH_PATTERN = Pattern.compile("^\uDB00\uDC0E§7Length: (\\w*)(?:§8 \\((.+)\\))?$");
    private static final Pattern DIFFICULTY_PATTERN = Pattern.compile("^\uDB00\uDC0E§7Difficulty: (\\w*)$");
    private static final Pattern REWARD_HEADER_PATTERN = Pattern.compile("^\uDB00\uDC0E§dRewards:$");
    private static final Pattern REWARD_PATTERN = Pattern.compile("^§d\uDB00\uDC04(- )?§7\\+?(?<reward>.+)$");
    private static final Pattern TRACKING_PATTERN =
            Pattern.compile(".+§f\uE000 §#[a-f0-9]{8}§lClick To (Untrack|Track)");
    private static final Pattern REQUIRES_HERO_PLUS_PATTERN = Pattern.compile(".+§7Requires §f\uE08A");
    private static final Pattern OVERALL_PROGRESS_PATTERN = Pattern.compile("^\\S*§7(\\d+) of (\\d+) completed$");
    private static final Pattern WIKI_REDIRECT_PATTERN = Pattern.compile("#REDIRECT \\[\\[(?<redirectname>.+)\\]\\]");

    private static final ScoreboardPart TRACKER_SCOREBOARD_PART = new ActivityTrackerScoreboardPart();
    private static final ContentBookQueries CONTAINER_QUERIES = new ContentBookQueries();
    private static final DialogueHistoryQueries DIALOGUE_HISTORY_QUERIES = new DialogueHistoryQueries();
    public static final ActivityMarkerProvider ACTIVITY_MARKER_PROVIDER = new ActivityMarkerProvider();

    private TrackedActivity trackedActivity;
    private List<List<StyledText>> dialogueHistory = List.of();
    private CappedValue overallProgress = CappedValue.EMPTY;
    private boolean overallProgressOutdated = true;
    private String currentProgressCharacter = "";

    public ActivityModel(MarkerModel markerModel) {
        super(List.of(markerModel));

        Handlers.Scoreboard.addPart(TRACKER_SCOREBOARD_PART);
        Models.Marker.registerMarkerProvider(ACTIVITY_MARKER_PROVIDER);
        Handlers.WrappedScreen.registerWrappedScreen(new ContentBookHolder());

        for (ActivityBeaconKind beaconKind : ActivityBeaconKind.values()) {
            Models.Beacon.registerBeacon(beaconKind);
        }

        for (ActivityBeaconMarkerKind beaconMarkerKind : ActivityBeaconMarkerKind.values()) {
            Models.Beacon.registerBeaconMarker(beaconMarkerKind);
        }
    }

    @SubscribeEvent
    public void onBeaconAdded(BeaconEvent.Added event) {
        Beacon beacon = event.getBeacon();
        if (!(beacon.beaconKind() instanceof ActivityBeaconKind)) return;

        // FIXME: Feature-model dependency
        ContentTrackerFeature feature = Managers.Feature.getFeatureInstance(ContentTrackerFeature.class);
        if (feature.hideOriginalMarker.get() && feature.isEnabled()) {
            // Only set this once they are added.
            // This is cleaner than posting an event on render,
            // but a change in the config will only have effect on newly placed beacons.
            ((EntityExtension) event.getEntity()).setRendered(false);
        }
    }

    @SubscribeEvent
    public void onBeaconMarkerAdded(BeaconMarkerEvent.Added event) {
        BeaconMarker beaconMarker = event.getBeaconMarker();
        if (!(beaconMarker.beaconMarkerKind() instanceof ActivityBeaconMarkerKind activityBeaconMarker)) return;

        // FIXME: Feature-model dependency
        ContentTrackerFeature feature = Managers.Feature.getFeatureInstance(ContentTrackerFeature.class);
        if (feature.isEnabled()) {
            if (feature.hideOriginalMarker.get()) {
                // Only set this once they are added.
                // This is cleaner than posting an event on render,
                // but a change in the config will only have effect on newly placed beacons.
                ((EntityExtension) event.getEntity()).setRendered(false);
            }
            if (feature.autoTrackCoordinates.get()) {
                Location spawn = new Location(McUtils.mc().level.getSharedSpawnPos());
                ACTIVITY_MARKER_PROVIDER.setSpawnLocation(activityBeaconMarker.getActivityType(), spawn);

                if (trackedActivity == null) return;

                Location trackedLocation = getTrackedLocation();
                if (trackedLocation != null && !spawn.equals(trackedLocation)) {
                    ACTIVITY_MARKER_PROVIDER.setTrackedActivityLocation(
                            activityBeaconMarker.getActivityType(), trackedLocation);
                }
            }
        }
    }

    @SubscribeEvent
    public void onBeaconMarkerRemoved(BeaconMarkerEvent.Removed event) {
        BeaconMarker beaconMarker = event.getBeaconMarker();
        if (!(beaconMarker.beaconMarkerKind() instanceof ActivityBeaconMarkerKind)) return;

        ACTIVITY_MARKER_PROVIDER.setSpawnLocation(null, null);
        ACTIVITY_MARKER_PROVIDER.setTrackedActivityLocation(null, null);
    }

    @SubscribeEvent
    public void onContainerItemsSet(ContainerSetContentEvent.Pre event) {
        if (!(Models.Container.getCurrentContainer() instanceof ContentBookContainer)) return;

        for (ItemStack itemStack : event.getItems()) {
            if (itemStack.getHoverName().getString().equals(PLAYER_PROGRESS_ITEM_NAME)) {
                if (parseOverallProgress(LoreUtils.getLore(itemStack))) return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWorldStateChanged(WorldStateEvent e) {
        resetTracker();
        // We need to rescan the overall progress when the world state changes
        overallProgressOutdated = true;
    }

    @SubscribeEvent
    public void onCharacterUpdated(CharacterUpdateEvent event) {
        // Same character, no need to scan again
        if (Models.Character.getId().equals(currentProgressCharacter)) {
            overallProgressOutdated = false;
            return;
        }
        // First thing to do when we just loaded a class
        scanOverallProgress();
    }

    public ActivityInfo parseItem(String name, ActivityType type, ItemStack itemStack) {
        Deque<StyledText> lore = LoreUtils.getLore(itemStack);

        StyledText statusLine = lore.pop();

        StyledText[] statusLineParts = statusLine.split(" - ");

        String specialInfo;
        String statusMessage;

        ActivityStatus status;

        if (type == ActivityType.WORLD_EVENT) {
            // World events have a slightly different format,
            // with the first description line being the region,
            // and the second being the status line.
            specialInfo = statusLine.getString();
            statusMessage = lore.pop().getString();

            status = ActivityStatus.fromWorldEvent(statusMessage);
        } else {
            // Handle every other activity type
            if (statusLineParts.length == 1) {
                specialInfo = null;
                statusMessage = statusLineParts[0].getString();
            } else {
                specialInfo = statusLineParts[0].getString();
                statusMessage = statusLineParts[1].getString();
            }

            status = ActivityStatus.from(statusMessage);
        }

        if (status == null) return null;

        if (!lore.pop().isBlank()) return null;

        Pair<Integer, Boolean> levelReq = Pair.of(0, true);
        ActivityDistance distance = null;
        String distanceInfo = null;
        ActivityLength length = null;
        String lengthInfo = null;
        ActivityDifficulty difficulty = null;
        ActivityTrackingState trackingState = ActivityTrackingState.UNTRACKABLE;
        List<Pair<Pair<ProfessionType, Integer>, Boolean>> professionLevels = new ArrayList<>();
        List<Pair<String, Boolean>> quests = new ArrayList<>();
        WorldEventFastTravelStatus worldEventFastTravelStatus = null;
        Map<ActivityRewardType, List<StyledText>> rewards = new TreeMap<>();
        List<StyledText> descriptionLines = new ArrayList<>();

        ActivityRewardType previousRewardType = null;
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
                ActivityRewardType rewardType = ActivityRewardType.matchRewardType(rewardMatcher.group("reward"));
                List<StyledText> existingRewards = rewards.getOrDefault(rewardType, new ArrayList<>());
                existingRewards.add(
                        StyledText.fromString(rewardMatcher.group("reward").trim() + ChatFormatting.RESET));

                rewards.put(rewardType, existingRewards);
                previousRewardType = rewardType;
                continue;
            }

            Matcher trackingMatcher = line.getMatcher(TRACKING_PATTERN);
            if (trackingMatcher.matches()) {
                trackingState = trackingMatcher.group(1).equals("Track")
                        ? ActivityTrackingState.TRACKABLE
                        : ActivityTrackingState.TRACKED;
                continue;
            }

            if (line.trim().isEmpty()) {
                previousRewardType = null;
                continue;
            }

            if (previousRewardType != null) {
                StyledText existingReward = rewards.get(previousRewardType).getLast();
                existingReward = existingReward.append(
                        " " + line.getStringWithoutFormatting().trim() + ChatFormatting.RESET);
                rewards.get(previousRewardType)
                        .set(rewards.get(previousRewardType).size() - 1, existingReward);
                continue;
            }

            if (type == ActivityType.WORLD_EVENT) {
                if (worldEventFastTravelStatus == null) {
                    worldEventFastTravelStatus = WorldEventFastTravelStatus.fromLine(line);

                    if (worldEventFastTravelStatus != null) continue;
                }
            }

            if (worldEventFastTravelStatus == WorldEventFastTravelStatus.UNAVAILABLE
                    && line.matches(REQUIRES_HERO_PLUS_PATTERN)) {
                continue;
            }

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
                Optional.ofNullable(worldEventFastTravelStatus),
                requirements,
                rewards,
                trackingState);
    }

    public void openActivityOnWiki(ActivityInfo activityInfo) {
        switch (activityInfo.type()) {
            case QUEST, STORYLINE_QUEST -> openQuestOnWiki(activityInfo);
            case MINI_QUEST -> openMiniQuestOnWiki(activityInfo);
            default ->
                Managers.Net.openLink(
                        UrlId.LINK_WIKI_LOOKUP,
                        Map.of("title", activityInfo.name().replace(" ", "_")));
        }
    }

    private void openQuestOnWiki(ActivityInfo activityInfo) {
        ApiResponse apiResponse =
                Managers.Net.callApi(UrlId.API_WIKI_QUEST_PAGE_QUERY, Map.of("name", activityInfo.name()));
        apiResponse.handleJsonArray(json -> {
            String pageTitle = json.get(0)
                    .getAsJsonObject()
                    .get("_pageTitle")
                    .getAsString()
                    .replace(WIKI_APOSTROPHE, "'");
            Managers.Net.openLink(UrlId.LINK_WIKI_LOOKUP, Map.of("title", pageTitle));
        });
    }

    private void openMiniQuestOnWiki(ActivityInfo activityInfo) {
        String type = activityInfo.name().split(" ")[0];

        String wikiName = "Quests#" + type + "ing_Posts";

        Managers.Net.openLink(UrlId.LINK_WIKI_LOOKUP, Map.of("title", wikiName));
    }

    public void openMapOnActivity(ActivityInfo activityInfo) {
        switch (activityInfo.type()) {
            case QUEST, STORYLINE_QUEST, MINI_QUEST -> openMapOnQuest(activityInfo);
            case CAVE -> openMapOnCave(activityInfo);
            case WORLD_EVENT -> {
                return;
            }
            default -> locateActivity(activityInfo, ActivityOpenAction.MAP);
        }
    }

    private void openMapOnQuest(ActivityInfo activityInfo) {
        QuestInfo questInfo = Models.Quest.getQuestInfoFromActivity(activityInfo);

        if (questInfo.nextLocation().isPresent()) {
            McUtils.player().closeContainer();
            McUtils.mc()
                    .setScreen(MainMapScreen.create(
                            questInfo.nextLocation().get().x(),
                            questInfo.nextLocation().get().z()));
        }
    }

    private void openMapOnCave(ActivityInfo activityInfo) {
        CaveInfo caveInfo = Models.Cave.getCaveInfoFromActivity(activityInfo);

        if (caveInfo.getNextLocation().isPresent()) {
            McUtils.player().closeContainer();
            McUtils.mc()
                    .setScreen(MainMapScreen.create(
                            caveInfo.getNextLocation().get().x(),
                            caveInfo.getNextLocation().get().z()));
        }
    }

    public void placeCompassOnActivity(ActivityInfo activityInfo) {
        switch (activityInfo.type()) {
            case QUEST, STORYLINE_QUEST, MINI_QUEST, WORLD_EVENT -> {
                return;
            }
            default -> locateActivity(activityInfo, ActivityOpenAction.COMPASS);
        }
    }

    private void locateActivity(ActivityInfo activityInfo, ActivityOpenAction openAction) {
        checkWikiForActivity(activityInfo.name(), activityInfo, openAction);
    }

    private void checkWikiForActivity(String activityName, ActivityInfo activityInfo, ActivityOpenAction openAction) {
        ApiResponse apiResponse = Managers.Net.callApi(UrlId.API_WIKI_DISCOVERY_QUERY, Map.of("name", activityName));

        apiResponse.handleJsonObject(json -> handleActivityJsonResponse(json, activityInfo, openAction));
    }

    private void handleActivityJsonResponse(JsonObject json, ActivityInfo activityInfo, ActivityOpenAction openAction) {
        if (json.has("error")) {
            McUtils.sendErrorToClient("Unable to find activity coordinates. (Wiki page not found)");
            return;
        }

        String wikiText = json.get("parse")
                .getAsJsonObject()
                .get("wikitext")
                .getAsJsonObject()
                .get("*")
                .getAsString();

        Matcher redirectMatcher = WIKI_REDIRECT_PATTERN.matcher(wikiText);
        if (redirectMatcher.matches()) {
            checkWikiForActivity(redirectMatcher.group("redirectname"), activityInfo, openAction);
            return;
        }

        wikiText = wikiText.replace(" ", "").replace("\n", "");

        String xLocation = wikiText.substring(wikiText.indexOf("xcoordinate="));
        String zLocation = wikiText.substring(wikiText.indexOf("zcoordinate="));

        int xEnd = Math.min(xLocation.indexOf('|'), xLocation.indexOf("}}"));
        int zEnd = Math.min(zLocation.indexOf('|'), zLocation.indexOf("}}"));

        int x;
        int z;

        try {
            x = Integer.parseInt(xLocation.substring(12, xEnd));
            z = Integer.parseInt(zLocation.substring(12, zEnd));
        } catch (NumberFormatException e) {
            McUtils.sendErrorToClient("Unable to find discovery coordinates. (Wiki template not located)");
            return;
        }

        if (x == 0 && z == 0) {
            McUtils.sendErrorToClient("Unable to find discovery coordinates. (Wiki coordinates not located)");
            return;
        }

        switch (openAction) {
            // We can't run this is on request thread
            case MAP ->
                Managers.TickScheduler.scheduleNextTick(() -> {
                    McUtils.player().closeContainer();
                    McUtils.setScreen(MainMapScreen.create(x, z));
                });
            case COMPASS -> {
                McUtils.playSoundUI(SoundEvents.EXPERIENCE_ORB_PICKUP);
                Models.Marker.USER_WAYPOINTS_PROVIDER.addLocation(new Location(x, 0, z), activityInfo.name());
            }
        }
    }

    private boolean isFulfilled(Matcher colorCodeMatcher) {
        // Check if the requirement is colored green
        return colorCodeMatcher.group(1).charAt(0) == ChatFormatting.GREEN.getChar();
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

    public QuestInfo getTrackedQuestInfo() {
        if (trackedActivity == null) return null;

        return Models.Quest.getQuestInfoFromName(trackedActivity.trackedName()).orElse(null);
    }

    public CaveInfo getTrackedCaveInfo() {
        if (trackedActivity == null) return null;

        return Models.Cave.getCaveInfoFromName(trackedActivity.trackedName()).orElse(null);
    }

    void updateTracker(String name, String type, StyledText nextTask) {
        ActivityType trackedType = ActivityType.from(type);
        trackedActivity = new TrackedActivity(name, trackedType, nextTask);

        WynntilsMod.postEvent(new ActivityTrackerUpdatedEvent(
                trackedActivity.trackedType(), trackedActivity.trackedName(), trackedActivity.trackedTask()));
    }

    void resetTracker() {
        trackedActivity = null;
        ACTIVITY_MARKER_PROVIDER.setTrackedActivityLocation(null, null);
    }

    public void scanContentBook(
            ActivityType activityType, BiConsumer<List<ActivityInfo>, List<StyledText>> processResult) {
        CONTAINER_QUERIES.queryContentBook(activityType, processResult, true, false);
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

    private void scanOverallProgress() {
        if (!overallProgressOutdated) return;

        overallProgressOutdated = false;
        CONTAINER_QUERIES.queryContentBook(
                ActivityType.RECOMMENDED,
                (ignored, progress) -> {
                    if (parseOverallProgress(progress)) {
                        return;
                    }
                },
                false,
                true);
    }

    private boolean parseOverallProgress(List<StyledText> progress) {
        for (StyledText line : progress) {
            Matcher m = line.getMatcher(OVERALL_PROGRESS_PATTERN);
            if (m.matches()) {
                int completed = Integer.parseInt(m.group(1));
                int total = Integer.parseInt(m.group(2));
                overallProgress = new CappedValue(completed, total);
                currentProgressCharacter = Models.Character.getId();
                return true;
            }
        }

        return false;
    }

    void setDialogueHistory(List<List<StyledText>> newDialogueHistory) {
        dialogueHistory = newDialogueHistory;
        WynntilsMod.postEvent(new DialogueHistoryReloadedEvent());
    }

    private Location getTrackedLocation() {
        return StyledTextUtils.extractLocation(trackedActivity.trackedTask()).orElse(null);
    }

    private record TrackedActivity(String trackedName, ActivityType trackedType, StyledText trackedTask) {}

    public enum ActivityOpenAction {
        MAP,
        COMPASS
    }
}
