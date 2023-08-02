/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun;

import com.google.common.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.models.beacons.BeaconModel;
import com.wynntils.models.beacons.event.BeaconEvent;
import com.wynntils.models.beacons.type.Beacon;
import com.wynntils.models.beacons.type.BeaconColor;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.lootrun.event.LootrunBeaconSelectedEvent;
import com.wynntils.models.lootrun.markers.LootrunBeaconMarkerProvider;
import com.wynntils.models.lootrun.scoreboard.LootrunScoreboardPart;
import com.wynntils.models.lootrun.type.LootrunLocation;
import com.wynntils.models.lootrun.type.LootrunTaskType;
import com.wynntils.models.lootrun.type.LootrunningState;
import com.wynntils.models.lootrun.type.TaskLocation;
import com.wynntils.models.marker.MarkerModel;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.VectorUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector2i;

/** A model dedicated to lootruns (the Wynncraft lootrun runs).
 * Don't confuse this with {@link com.wynntils.services.lootrunpaths.LootrunPathsService}.
 */
public class LootrunModel extends Model {
    //                          À§6§lLootrun Completed!
    //           ÀÀÀ§7Collect your rewards at the reward chest
    //
    //               §aRewards§r                           À§dStatistics
    //          À§f26§7 Reward Pulls§r                ÀÀ§7Time Elapsed: §f23:51
    //         ÀÀ§f2§7 Reward Rerolls§r                  §7Mobs Killed: §f236
    //    ÀÀÀ§f260§7 Lootrun Experience§r       ÀÀ§7Challenges Completed: §f13

    private static final Pattern LOOTRUN_COMPLETED_PATTERN = Pattern.compile("[À\\s]*§6§lLootrun Completed!");

    // Rewards
    private static final Pattern REWARD_PULLS_PATTERN = Pattern.compile("[À\\s]*§.(\\d+)§7 Reward Pulls§r");
    private static final Pattern REWARD_REROLLS_PATTERN = Pattern.compile("[À\\s]*§.(\\d+)§7 Reward Rerolls§r");
    private static final Pattern LOOTRUN_EXPERIENCE_PATTERN = Pattern.compile("[À\\s]*§.(\\d+)§7 Lootrun Experience§r");

    // Statistics
    private static final Pattern TIME_ELAPSED_PATTERN = Pattern.compile("[À\\s]*§7Time Elapsed: §.(\\d+):(\\d+)");
    private static final Pattern MOBS_KILLED_PATTERN = Pattern.compile("[À\\s]*§7Mobs Killed: §.(\\d+)");
    private static final Pattern CHALLENGES_COMPLETED_PATTERN =
            Pattern.compile("[À\\s]*§7Challenges Completed: §.(\\d+)");

    //                             À§c§lLootrun Failed!
    //                         ÀÀ§7Better luck next time!
    //
    //                                 ÀÀ§dStatistics
    //                           ÀÀ§7Time Elapsed: §f13:45
    //                        ÀÀÀ§7Challenges Completed: §f7

    private static final Pattern LOOTRUN_FAILED_PATTERN = Pattern.compile("[À\\s]*§c§lLootrun Failed!");

    private static final float BEACON_REMOVAL_RADIUS = 25f;

    // This value represents how many times a beacon update
    // needs to point to the same (different) task before it is updated.
    // Lower values update faster, higher values update slower, but are much more consistent.
    private static final int BEACON_UPDATE_CHANGE_THRESHOLD = 6;

    // Beacon positions are sometimes off by a few blocks
    private static final int BEACON_POSITION_ERROR = 3;

    private static final LootrunScoreboardPart LOOTRUN_SCOREBOARD_PART = new LootrunScoreboardPart();

    private static final LootrunBeaconMarkerProvider LOOTRUN_BEACON_COMPASS_PROVIDER =
            new LootrunBeaconMarkerProvider();

    private Map<LootrunLocation, Set<TaskLocation>> taskLocations = new HashMap<>();

    private LootrunFinishedEventBuilder.Completed lootrunCompletedBuilder;
    private LootrunFinishedEventBuilder.Failed lootrunFailedBuilder;

    // Data that can live in memory, when joining a class we will parse these
    private LootrunningState lootrunningState = LootrunningState.NOT_RUNNING;
    private LootrunTaskType taskType;

    // rely on color, beacon positions change
    private Map<BeaconColor, Pair<Double, TaskLocation>> beacons = new HashMap<>();
    private Map<BeaconColor, Pair<Integer, TaskLocation>> beaconUpdates = new HashMap<>();

    // Data to be persisted
    @Persisted
    private Storage<Map<String, Map<BeaconColor, Integer>>> selectedBeaconsStorage = new Storage<>(new TreeMap<>());

    @Persisted
    private Storage<Map<String, Beacon>> closestBeaconStorage = new Storage<>(new TreeMap<>());

    private Map<BeaconColor, Integer> selectedBeacons = new TreeMap<>();

    public LootrunModel(BeaconModel beaconModel, MarkerModel markerModel) {
        super(List.of(beaconModel, markerModel));

        Handlers.Scoreboard.addPart(LOOTRUN_SCOREBOARD_PART);
        Models.Marker.registerMarkerProvider(LOOTRUN_BEACON_COMPASS_PROVIDER);
        reloadData();
    }

    @Override
    public void reloadData() {
        loadLootrunTaskLocations();
    }

    private void loadLootrunTaskLocations() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_LOOTRUN_TASKS);
        dl.handleReader(reader -> {
            Type type = new TypeToken<Map<LootrunLocation, Set<TaskLocation>>>() {}.getType();
            taskLocations = Managers.Json.GSON.fromJson(reader, type);
        });
    }

    @SubscribeEvent
    public void onCharacterChange(CharacterUpdateEvent event) {
        String id = Models.Character.getId();

        selectedBeaconsStorage.get().putIfAbsent(id, new TreeMap<>());
        selectedBeacons = selectedBeaconsStorage.get().get(id);

        selectedBeaconsStorage.touched();
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageReceivedEvent event) {
        if (event.getRecipientType() != RecipientType.INFO) return;
        StyledText styledText = event.getOriginalStyledText();

        if (styledText.matches(LOOTRUN_COMPLETED_PATTERN)) {
            lootrunCompletedBuilder = new LootrunFinishedEventBuilder.Completed();
            lootrunFailedBuilder = null;
            return;
        }
        if (styledText.matches(LOOTRUN_FAILED_PATTERN)) {
            lootrunFailedBuilder = new LootrunFinishedEventBuilder.Failed();
            lootrunCompletedBuilder = null;
            return;
        }

        if (lootrunCompletedBuilder != null) {
            parseCompletedMessages(styledText);
        } else if (lootrunFailedBuilder != null) {
            parseFailedMessages(styledText);
        }
    }

    @SubscribeEvent
    public void onWorldStateChanged(WorldStateEvent event) {
        // The world state event is sometimes late compared to lootrun events (beacons, scoreboard)
        // Reseting once when leaving the class is enough
        if (event.getNewState() == WorldState.WORLD) return;

        lootrunCompletedBuilder = null;
        lootrunFailedBuilder = null;

        lootrunningState = LootrunningState.NOT_RUNNING;
        taskType = null;
        beaconUpdates = new HashMap<>();
        beacons = new HashMap<>();
        LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();

        selectedBeacons = new TreeMap<>();
    }

    @SubscribeEvent
    public void onBeaconMoved(BeaconEvent.Moved event) {
        if (getLocation().isEmpty()) return;
        Beacon beacon = event.getBeacon();
        BeaconColor beaconColor = beacon.color();
        if (!beaconColor.isUsedInLootruns()) return;

        Pair<Double, TaskLocation> taskPrediction = getBeaconTaskLocationPrediction(beacon);

        if (taskPrediction == null) {
            WynntilsMod.warn("Could not predict updated task location for beacon " + beacon.location());
            return;
        }

        Pair<Double, TaskLocation> oldPrediction = beacons.get(beaconColor);

        // No prediction yet, or prediction is worse than the old one
        if (Objects.equals(oldPrediction.b(), taskPrediction.b()) || taskPrediction.a() > oldPrediction.a()) return;

        if (beaconUpdates.containsKey(beaconColor)) {
            Pair<Integer, TaskLocation> pair = beaconUpdates.get(beaconColor);

            // New prediction is different from the old one. Reset the counter.
            if (!pair.b().equals(taskPrediction.b())) {
                beaconUpdates.put(beaconColor, Pair.of(1, taskPrediction.b()));
                return;
            }

            // New prediction is the same as the old one. We got the same prediction multiple times in a row.
            if (pair.a() + 1 >= BEACON_UPDATE_CHANGE_THRESHOLD) {
                // We could go with the average, or highest prediction score,
                // but we jsut go with the last one for now.
                // This could be a really edge-case optimization.
                beacons.put(beaconColor, taskPrediction);
                LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();
                beaconUpdates.remove(beaconColor);

                McUtils.sendMessageToClient(Component.literal(
                        "Task location prediction for " + beaconColor + " changed to " + taskPrediction));

                return;
            }

            // New prediction is the same as the old one, but we haven't reached the threshold yet.
            beaconUpdates.put(beaconColor, Pair.of(pair.a() + 1, taskPrediction.b()));
        } else {
            beaconUpdates.put(beaconColor, Pair.of(1, taskPrediction.b()));
        }
    }

    // When we get close to a beacon, it get's removed.
    // This is our signal to know that this can be the current beacon,
    // but we don't know for sure until the scoreboard confirms it.
    @SubscribeEvent
    public void onBeaconRemove(BeaconEvent.Removed event) {
        Beacon beacon = event.getBeacon();
        BeaconColor beaconColor = beacon.color();
        if (!beaconColor.isUsedInLootruns()) return;

        Beacon closestBeacon = getClosestBeacon();

        double newBeaconDistanceToPlayer = VectorUtils.distanceIgnoringY(
                beacon.location().toPosition(), McUtils.mc().player.position());
        double oldBeaconDistanceToPlayer = closestBeacon == null
                ? Double.MAX_VALUE
                : VectorUtils.distanceIgnoringY(
                        closestBeacon.location().toPosition(),
                        McUtils.mc().player.position());
        if (newBeaconDistanceToPlayer < BEACON_REMOVAL_RADIUS
                && newBeaconDistanceToPlayer < oldBeaconDistanceToPlayer) {
            setClosestBeacon(event.getBeacon());
        } else {
            // Note: If we get more accurate predictions, we don't need to remove if we are close.
            beacons.remove(beaconColor);
            LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();
            beaconUpdates.remove(beaconColor);
        }
    }

    @SubscribeEvent
    public void onBeaconAdded(BeaconEvent.Added event) {
        Beacon beacon = event.getBeacon();
        if (!beacon.color().isUsedInLootruns()) return;

        Pair<Double, TaskLocation> taskPrediction = getBeaconTaskLocationPrediction(beacon);
        if (taskPrediction == null) {
            WynntilsMod.warn("Failed to get task prediction for beacon " + beacon.color() + " at " + beacon.location());
            return;
        }

        beacons.put(beacon.color(), taskPrediction);
        LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();
        McUtils.sendMessageToClient(
                Component.literal("Task location prediction for " + beacon.color() + " is " + taskPrediction));
    }

    public int getBeaconCount(BeaconColor color) {
        return selectedBeacons.getOrDefault(color, 0);
    }

    public LootrunningState getState() {
        return lootrunningState;
    }

    public Optional<LootrunLocation> getLocation() {
        if (McUtils.mc().player == null) return Optional.empty();

        return Optional.ofNullable(
                LootrunLocation.fromCoordinates(McUtils.mc().player.position()));
    }

    public Optional<LootrunTaskType> getTaskType() {
        return Optional.ofNullable(taskType);
    }

    public Map<BeaconColor, Pair<Double, TaskLocation>> getBeacons() {
        return beacons;
    }

    public void setState(LootrunningState newState, LootrunTaskType taskType) {
        // If nothing changes, don't do anything.
        if (this.lootrunningState == newState) return;

        LootrunningState oldState = this.lootrunningState;
        this.lootrunningState = newState;
        this.taskType = taskType;

        handleStateChange(oldState, newState);
    }

    public Beacon getClosestBeacon() {
        return closestBeaconStorage.get().get(Models.Character.getId());
    }

    public void setClosestBeacon(Beacon beacon) {
        if (beacon == null) {
            closestBeaconStorage.get().remove(Models.Character.getId());
        } else {
            closestBeaconStorage.get().put(Models.Character.getId(), beacon); // can be null safely
        }

        closestBeaconStorage.touched();
    }

    public void resetBeaconStorage() {
        selectedBeacons = new TreeMap<>();

        selectedBeaconsStorage.get().put(Models.Character.getId(), selectedBeacons);
        selectedBeaconsStorage.touched();
    }

    private void handleStateChange(LootrunningState oldState, LootrunningState newState) {
        if (newState == LootrunningState.NOT_RUNNING) {
            resetBeaconStorage();

            taskType = null;
            setClosestBeacon(null);

            beacons = new HashMap<>();
            beaconUpdates = new HashMap<>();
            return;
        }

        if (oldState == LootrunningState.NOT_RUNNING) {
            WynntilsMod.info("Started a lootrun at " + getLocation());
            return;
        }

        Beacon closestBeacon = getClosestBeacon();
        if (oldState == LootrunningState.CHOOSING_BEACON
                && newState == LootrunningState.IN_TASK
                && closestBeacon != null) {
            WynntilsMod.info("Selected a " + closestBeacon.color() + " beacon at " + closestBeacon.location());
            selectedBeacons.put(closestBeacon.color(), selectedBeacons.getOrDefault(closestBeacon.color(), 0) + 1);
            selectedBeaconsStorage.touched();
            WynntilsMod.postEvent(new LootrunBeaconSelectedEvent(closestBeacon));

            // We selected a beacon, so other beacons are no longer relevant.
            beacons.clear();
            setClosestBeacon(null);
            LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();

            return;
        }
    }

    private Pair<Double, TaskLocation> getBeaconTaskLocationPrediction(Beacon beacon) {
        Optional<LootrunLocation> location = getLocation();
        if (location.isEmpty()) {
            WynntilsMod.warn("Location was when trying to predict for: " + beacon);
            return null;
        }

        Set<TaskLocation> currentTaskLocations = taskLocations.get(location.get());
        if (currentTaskLocations == null || currentTaskLocations.isEmpty()) {
            WynntilsMod.warn("No task locations found for " + location.get());
            return null;
        }

        List<TaskLocation> usedTaskLocations = beacons.entrySet().stream()
                .filter(entry -> entry.getKey() != beacon.color())
                .map(Map.Entry::getValue)
                .map(Pair::b)
                .toList();

        double lowestPredictionScore = Double.MAX_VALUE;
        TaskLocation closestTaskLocation = null;
        for (TaskLocation currentTaskLocation : currentTaskLocations) {
            // Possible optimization:
            // Calculate the prediction score for tasks that are used by other beacons, and if our prediction score
            // is lower than the other beacons, we can override it and calculate a new prediction for the other beacon.
            if (usedTaskLocations.contains(currentTaskLocation)) {
                continue;
            }

            // Player Location
            Vector2i playerPosition =
                    new Vector2i((int) McUtils.player().position().x(), (int)
                            McUtils.player().position().z());
            // Task Location
            Vector2i taskLocationPosition = new Vector2i(
                    currentTaskLocation.location().x(),
                    currentTaskLocation.location().z());
            // Wynn Beacon
            Vector2i beaconPosition =
                    new Vector2i(beacon.location().x(), beacon.location().z());

            // Short circuit if the beacon matches a task location.
            if (taskLocationPosition.distance(beaconPosition) < BEACON_POSITION_ERROR) {
                lowestPredictionScore = 0d;
                closestTaskLocation = currentTaskLocation;
                break;
            }

            double taskLocationDistanceToPlayer = taskLocationPosition.distance(playerPosition);
            double playerDistanceToBeacon = playerPosition.distance(beaconPosition);
            double beaconPositionToTask = beaconPosition.distance(taskLocationPosition);

            if (taskLocationDistanceToPlayer < playerDistanceToBeacon
                    || taskLocationDistanceToPlayer < beaconPositionToTask) {
                // The beacon is not between the player and the task location, but further away.
                continue;
            }

            // Heron's formula
            double s = (taskLocationDistanceToPlayer + playerDistanceToBeacon + beaconPositionToTask) / 2;
            double area = Math.sqrt(
                    s * (s - taskLocationDistanceToPlayer) * (s - playerDistanceToBeacon) * (s - beaconPositionToTask));

            // The prediction score is the distance from the line
            // Calculate the height of the triangle formed by the player, beacon, and task location with the base being
            // the line between the player and the task location.
            double predictionScore = 2 * area / taskLocationDistanceToPlayer;

            if (predictionScore < lowestPredictionScore) {
                lowestPredictionScore = predictionScore;
                closestTaskLocation = currentTaskLocation;
            }
        }

        return Pair.of(lowestPredictionScore, closestTaskLocation);
    }

    private void parseCompletedMessages(StyledText styledText) {
        Matcher matcher = styledText.getMatcher(REWARD_PULLS_PATTERN);
        if (matcher.find()) {
            lootrunCompletedBuilder.setRewardPulls(Integer.parseInt(matcher.group(1)));

            matcher = styledText.getMatcher(TIME_ELAPSED_PATTERN);
            if (matcher.find()) {
                lootrunCompletedBuilder.setTimeElapsed(
                        Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2)));
                return;
            }

            WynntilsMod.warn("Found lootrun pulls but no time elapsed: " + styledText);
        }

        matcher = styledText.getMatcher(REWARD_REROLLS_PATTERN);
        if (matcher.find()) {
            lootrunCompletedBuilder.setRewardRerolls(Integer.parseInt(matcher.group(1)));

            matcher = styledText.getMatcher(MOBS_KILLED_PATTERN);
            if (matcher.find()) {
                lootrunCompletedBuilder.setMobsKilled(Integer.parseInt(matcher.group(1)));
                return;
            }

            WynntilsMod.warn("Found lootrun rerolls but no mobs killed: " + styledText);
        }

        matcher = styledText.getMatcher(LOOTRUN_EXPERIENCE_PATTERN);
        if (matcher.find()) {
            lootrunCompletedBuilder.setExperienceGained(Integer.parseInt(matcher.group(1)));

            matcher = styledText.getMatcher(CHALLENGES_COMPLETED_PATTERN);
            if (matcher.find()) {
                lootrunCompletedBuilder.setChallengesCompleted(Integer.parseInt(matcher.group(1)));
                WynntilsMod.postEvent(lootrunCompletedBuilder.build());
                lootrunCompletedBuilder = null;
                return;
            }

            WynntilsMod.warn("Found lootrun experience but no challenges completed: " + styledText);
        }
    }

    private void parseFailedMessages(StyledText styledText) {
        Matcher matcher = styledText.getMatcher(TIME_ELAPSED_PATTERN);
        if (matcher.find()) {
            lootrunFailedBuilder.setTimeElapsed(
                    Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2)));
            return;
        }

        matcher = styledText.getMatcher(CHALLENGES_COMPLETED_PATTERN);
        if (matcher.find()) {
            lootrunFailedBuilder.setChallengesCompleted(Integer.parseInt(matcher.group(1)));
            WynntilsMod.postEvent(lootrunFailedBuilder.build());
            lootrunFailedBuilder = null;
            return;
        }
    }
}
