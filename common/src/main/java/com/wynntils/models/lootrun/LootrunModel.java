/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
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
import com.wynntils.core.text.StyledText;
import com.wynntils.features.combat.CustomLootrunBeaconsFeature;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.mc.extension.EntityExtension;
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
import com.wynntils.models.lootrun.type.TaskPrediction;
import com.wynntils.models.marker.MarkerModel;
import com.wynntils.models.particle.ParticleModel;
import com.wynntils.models.particle.event.ParticleVerifiedEvent;
import com.wynntils.models.particle.type.ParticleType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.VectorUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.PosUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector2d;

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

    // Beacon positions are sometimes off by a few blocks
    private static final int TASK_POSITION_ERROR = 3;

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
    private Map<BeaconColor, TaskPrediction> beacons = new HashMap<>();
    private Map<BeaconColor, Pair<Integer, TaskLocation>> beaconUpdates = new HashMap<>();

    // particles can accurately show task locations
    private Set<TaskLocation> possibleTaskLocations = new HashSet<>();

    // Data to be persisted
    @Persisted
    private Storage<Map<String, Map<BeaconColor, Integer>>> selectedBeaconsStorage = new Storage<>(new TreeMap<>());

    @Persisted
    private Storage<Map<String, BeaconColor>> lastTaskBeaconColorStorage = new Storage<>(new TreeMap<>());

    @Persisted
    private Storage<Map<String, Beacon>> closestBeaconStorage = new Storage<>(new TreeMap<>());

    @Persisted
    private Storage<Map<String, Integer>> redBeaconTaskCountStorage = new Storage<>(new TreeMap<>());

    private Map<BeaconColor, Integer> selectedBeacons = new TreeMap<>();

    private int timeLeft = 0;
    private CappedValue challenges = CappedValue.EMPTY;

    public LootrunModel(BeaconModel beaconModel, MarkerModel markerModel, ParticleModel particleModel) {
        super(List.of(beaconModel, markerModel, particleModel));

        Handlers.Scoreboard.addPart(LOOTRUN_SCOREBOARD_PART);
        Models.Marker.registerMarkerProvider(LOOTRUN_BEACON_COMPASS_PROVIDER);
        reloadData();
    }

    @Override
    public void reloadData() {
        loadLootrunTaskLocations();
    }

    private void loadLootrunTaskLocations() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_LOOTRUN_TASKS_NAMED);
        dl.handleReader(reader -> {
            Type type = new TypeToken<Map<LootrunLocation, Set<TaskLocation>>>() {}.getType();
            taskLocations = Managers.Json.GSON.fromJson(reader, type);
        });
    }

    @SubscribeEvent
    public void onLootrunParticle(ParticleVerifiedEvent event) {
        if (event.getParticle().particleType() != ParticleType.LOOTRUN_TASK) return;

        Optional<LootrunLocation> currentLocation = getLocation();
        if (currentLocation.isEmpty()) return;

        for (TaskLocation taskLocation : taskLocations.getOrDefault(currentLocation.get(), Set.of())) {
            if (PosUtils.closerThanIgnoringY(
                    taskLocation.location().toPosition(), event.getParticle().position(), TASK_POSITION_ERROR)) {
                possibleTaskLocations.add(taskLocation);
                return;
            }
        }
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

        possibleTaskLocations = new HashSet<>();

        lootrunningState = LootrunningState.NOT_RUNNING;
        taskType = null;
        beaconUpdates = new HashMap<>();
        beacons = new HashMap<>();
        LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();

        selectedBeacons = new TreeMap<>();

        challenges = CappedValue.EMPTY;
        timeLeft = 0;
    }

    @SubscribeEvent
    public void onBeaconMoved(BeaconEvent.Moved event) {
        Beacon beacon = event.getNewBeacon();
        BeaconColor beaconColor = beacon.color();
        if (!beaconColor.isUsedInLootruns()) return;

        updateTaskLocationPrediction(beacon);
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
                beacon.position(), McUtils.mc().player.position());
        double oldBeaconDistanceToPlayer = closestBeacon == null
                ? Double.MAX_VALUE
                : VectorUtils.distanceIgnoringY(
                        closestBeacon.position(), McUtils.mc().player.position());
        if (newBeaconDistanceToPlayer < BEACON_REMOVAL_RADIUS
                && newBeaconDistanceToPlayer <= oldBeaconDistanceToPlayer) {
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

        // FIXME: Feature-model dependency
        CustomLootrunBeaconsFeature feature = Managers.Feature.getFeatureInstance(CustomLootrunBeaconsFeature.class);
        if (feature.removeOriginalBeacons.get() && feature.isEnabled()) {
            for (Entity entity : event.getEntities()) {
                // Only set this once they are added.
                // This is cleaner than posting an event on render,
                // but a change in the config will only have effect on newly placed beacons.
                ((EntityExtension) entity).setRendered(false);
            }
        }

        updateTaskLocationPrediction(beacon);
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

    public Map<BeaconColor, TaskPrediction> getBeacons() {
        return beacons;
    }

    public TaskLocation getTaskForColor(BeaconColor beaconColor) {
        TaskPrediction taskPrediction = beacons.get(beaconColor);
        if (taskPrediction == null) return null;

        return taskPrediction.taskLocation();
    }

    public void setState(LootrunningState newState, LootrunTaskType taskType) {
        // If nothing changes, don't do anything.
        if (this.lootrunningState == newState) return;

        LootrunningState oldState = this.lootrunningState;
        this.lootrunningState = newState;
        this.taskType = taskType;

        handleStateChange(oldState, newState);
    }

    public int getCurrentTime() {
        return timeLeft;
    }

    public CappedValue getChallenges() {
        return challenges;
    }

    public BeaconColor getLastTaskBeaconColor() {
        return lastTaskBeaconColorStorage.get().get(Models.Character.getId());
    }

    public Beacon getClosestBeacon() {
        return closestBeaconStorage.get().get(Models.Character.getId());
    }

    public int getRedBeaconTaskCount() {
        return redBeaconTaskCountStorage.get().getOrDefault(Models.Character.getId(), 0);
    }

    private void setLastTaskBeaconColor(BeaconColor beaconColor) {
        if (beaconColor == null) {
            lastTaskBeaconColorStorage.get().remove(Models.Character.getId());
        } else {
            lastTaskBeaconColorStorage.get().put(Models.Character.getId(), beaconColor);
        }

        lastTaskBeaconColorStorage.touched();
    }

    private void setClosestBeacon(Beacon beacon) {
        if (beacon == null) {
            closestBeaconStorage.get().remove(Models.Character.getId());
        } else {
            closestBeaconStorage.get().put(Models.Character.getId(), beacon); // can be null safely
        }

        closestBeaconStorage.touched();
    }

    private void resetBeaconStorage() {
        selectedBeacons = new TreeMap<>();

        selectedBeaconsStorage.get().put(Models.Character.getId(), selectedBeacons);
        selectedBeaconsStorage.touched();
    }

    public void addToRedBeaconTaskCount(int changeAmount) {
        Integer oldCount = redBeaconTaskCountStorage.get().getOrDefault(Models.Character.getId(), 0);

        int newCount = Math.max(oldCount + changeAmount, 0);
        redBeaconTaskCountStorage.get().put(Models.Character.getId(), newCount);
        redBeaconTaskCountStorage.touched();
    }

    public void resetRedBeaconTaskCount() {
        redBeaconTaskCountStorage.get().remove(Models.Character.getId());
        redBeaconTaskCountStorage.touched();
    }

    public void setTimeLeft(int seconds) {
        timeLeft = seconds;
    }

    public void setChallenges(CappedValue amount) {
        CappedValue oldChallenges = challenges;
        challenges = amount;

        if (oldChallenges == CappedValue.EMPTY) return;

        // First, check if we completed a challenge.
        if (amount.current() > oldChallenges.current()) {
            addToRedBeaconTaskCount(-1);
        }

        // Then, check if we completed have new challenges from a red beacon.
        if (getLastTaskBeaconColor() == BeaconColor.RED && amount.max() > oldChallenges.max()) {
            addToRedBeaconTaskCount(amount.max() - oldChallenges.max());
        }
    }

    private void handleStateChange(LootrunningState oldState, LootrunningState newState) {
        if (newState == LootrunningState.NOT_RUNNING) {
            resetBeaconStorage();

            taskType = null;
            setClosestBeacon(null);
            setLastTaskBeaconColor(null);
            resetRedBeaconTaskCount();

            possibleTaskLocations = new HashSet<>();

            beacons = new HashMap<>();
            beaconUpdates = new HashMap<>();

            timeLeft = 0;
            challenges = CappedValue.EMPTY;
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
            WynntilsMod.info("Selected a " + closestBeacon.color() + " beacon at " + closestBeacon.position());
            selectedBeacons.put(closestBeacon.color(), selectedBeacons.getOrDefault(closestBeacon.color(), 0) + 1);
            selectedBeaconsStorage.touched();
            setLastTaskBeaconColor(closestBeacon.color());
            WynntilsMod.postEvent(new LootrunBeaconSelectedEvent(closestBeacon));

            possibleTaskLocations = new HashSet<>();

            // We selected a beacon, so other beacons are no longer relevant.
            beacons.clear();
            setClosestBeacon(null);
            LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();
            return;
        }
    }

    private void updateTaskLocationPrediction(Beacon beacon) {
        Optional<LootrunLocation> location = getLocation();
        if (location.isEmpty()) {
            WynntilsMod.warn("Location was when trying to predict for: " + beacon);
            return;
        }

        Set<TaskLocation> currentTaskLocations = possibleTaskLocations;
        if (currentTaskLocations == null || currentTaskLocations.isEmpty()) {
            WynntilsMod.warn("No task locations found for " + location.get() + ". Using fallback, all locations.");
            currentTaskLocations = taskLocations.get(location.get());
        }
        if (currentTaskLocations == null || currentTaskLocations.isEmpty()) {
            WynntilsMod.warn("Fallback failed, no task locations found for " + location.get());
            return;
        }

        List<TaskPrediction> usedTaskLocations = beacons.entrySet().stream()
                .filter(entry -> entry.getKey() != beacon.color())
                .map(Map.Entry::getValue)
                .toList();

        Map<Double, TaskLocation> predictionScores = new TreeMap<>();
        for (TaskLocation currentTaskLocation : currentTaskLocations) {
            Pair<Double, TaskLocation> prediction = calculatePredictionScore(beacon, currentTaskLocation);
            if (prediction == null) continue;

            predictionScores.put(prediction.a(), prediction.b());
        }

        // According to TreeMap's sort order, the first entry is the lowest prediction score.
        for (Map.Entry<Double, TaskLocation> entry : predictionScores.entrySet()) {
            TaskLocation closestTaskLocation = entry.getValue();
            Double predictionValue = entry.getKey();

            TaskPrediction oldPrediction = beacons.get(beacon.color());
            TaskPrediction newTaskPrediction = new TaskPrediction(beacon, closestTaskLocation, predictionValue);

            // If the prediction is the same, don't update.
            if (oldPrediction != null
                    && Objects.equals(oldPrediction.taskLocation(), newTaskPrediction.taskLocation())) {
                if (newTaskPrediction.predictionScore() < oldPrediction.predictionScore()) {
                    // The prediction is the same, but the score is better, so update.
                    beacons.put(beacon.color(), newTaskPrediction);
                }
                return;
            }

            // The prediction is a location where another colored beacon is already at
            Optional<TaskPrediction> usedTaskPredictionOpt = usedTaskLocations.stream()
                    .filter(pair -> Objects.equals(pair.taskLocation(), closestTaskLocation))
                    .findFirst();
            if (usedTaskPredictionOpt.isPresent()) {
                TaskPrediction usedTaskPrediction = usedTaskPredictionOpt.get();

                // We predict that we are closer to the task location than the other beacon.
                // Overwrite the other beacon's prediction.
                if (newTaskPrediction.predictionScore() < usedTaskPrediction.predictionScore()) {
                    beacons.put(beacon.color(), newTaskPrediction);
                    beacons.remove(usedTaskPrediction.beacon().color());

                    // Update the other beacon's prediction.
                    updateTaskLocationPrediction(usedTaskPrediction.beacon());
                    break;
                } else {
                    // We predict that the other beacon is closer to the task location than us.
                    // Use the second best prediction.
                    continue;
                }
            }

            // The prediction is not used by another beacon.
            beacons.put(beacon.color(), newTaskPrediction);
            break;
        }

        // Finally, update the markers.
        LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();
    }

    private Pair<Double, TaskLocation> calculatePredictionScore(Beacon beacon, TaskLocation currentTaskLocation) {
        // Player Location
        Vector2d playerPosition = new Vector2d(
                McUtils.player().position().x(), McUtils.player().position().z());
        // Task Location
        Vector2d taskLocationPosition = new Vector2d(
                currentTaskLocation.location().x(),
                currentTaskLocation.location().z());
        // Wynn Beacon
        Vector2d beaconPosition =
                new Vector2d(beacon.position().x(), beacon.position().z());

        // Short circuit if the beacon matches a task location.
        // Wynn beacons are always at the center of a block, if they are in their "final" position.
        if (Math.abs(beaconPosition.x() % 1) == 0.5d
                && Math.abs(beaconPosition.y() % 1) == 0.5d
                && taskLocationPosition.distance(beaconPosition) < TASK_POSITION_ERROR) {
            return Pair.of(0d, currentTaskLocation);
        }

        double taskLocationDistanceToPlayer = taskLocationPosition.distance(playerPosition);
        double playerDistanceToBeacon = playerPosition.distance(beaconPosition);
        double beaconPositionToTask = beaconPosition.distance(taskLocationPosition);

        if (taskLocationDistanceToPlayer < playerDistanceToBeacon
                || taskLocationDistanceToPlayer < beaconPositionToTask) {
            // The beacon is not between the player and the task location, but further away.
            return null;
        }

        // Heron's formula
        double s = (taskLocationDistanceToPlayer + playerDistanceToBeacon + beaconPositionToTask) / 2;
        double area = Math.sqrt(
                s * (s - taskLocationDistanceToPlayer) * (s - playerDistanceToBeacon) * (s - beaconPositionToTask));

        // The prediction score is the distance from the line
        // Calculate the height of the triangle formed by the player, beacon, and task location with the base being
        // the line between the player and the task location.
        double predictionScore = 2 * area / taskLocationDistanceToPlayer;

        return Pair.of(predictionScore, currentTaskLocation);
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
