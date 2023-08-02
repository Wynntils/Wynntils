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
import com.wynntils.core.storage.RegisterStorage;
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

    private static final LootrunScoreboardPart LOOTRUN_SCOREBOARD_PART = new LootrunScoreboardPart();

    private static final LootrunBeaconMarkerProvider LOOTRUN_BEACON_COMPASS_PROVIDER =
            new LootrunBeaconMarkerProvider();

    private Map<LootrunLocation, Set<TaskLocation>> taskLocations = new HashMap<>();

    private LootrunFinishedEventBuilder.Completed lootrunCompletedBuilder;
    private LootrunFinishedEventBuilder.Failed lootrunFailedBuilder;

    // Data that can live in memory, when joining a class we will parse these
    private LootrunningState lootrunningState = LootrunningState.NOT_RUNNING;
    private LootrunLocation location;
    private LootrunTaskType taskType;
    private Map<Beacon, TaskLocation> beacons = new HashMap<>();
    private Map<Beacon, Pair<Integer, TaskLocation>> beaconUpdates = new HashMap<>();

    // Data to be persisted
    @RegisterStorage
    private Storage<Map<String, Map<BeaconColor, Integer>>> selectedBeaconsStorage = new Storage<>(new TreeMap<>());

    @RegisterStorage
    private Storage<Map<String, Beacon>> closestBeaconStorage = new Storage<>(new TreeMap<>());

    private Map<BeaconColor, Integer> selectedBeacons = new TreeMap<>();
    private Beacon closestBeacon;

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
        closestBeacon = closestBeaconStorage.get().get(id); // can be null safely

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
        lootrunCompletedBuilder = null;
        lootrunFailedBuilder = null;

        lootrunningState = LootrunningState.NOT_RUNNING;
        location = null;
        taskType = null;
        beaconUpdates = new HashMap<>();
        beacons = new HashMap<>();
        LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();

        selectedBeacons = new TreeMap<>();
        closestBeacon = null;

        // We set this here too because this might be used by beacons before the scoreboard is updated.
        location = LootrunLocation.fromCoordinates(McUtils.mc().player.position());
    }

    @SubscribeEvent
    public void onBeaconMoved(BeaconEvent.Moved event) {
        if (location == null) return;
        Beacon beacon = event.getBeacon();
        if (!beacon.color().isUsedInLootruns()) return;

        TaskLocation taskPrediction = getBeaconTaskLocationPrediction(beacon);
        TaskLocation oldPrediction = beacons.get(beacon);

        if (Objects.equals(oldPrediction, taskPrediction)) return;

        if (beaconUpdates.containsKey(beacon)) {
            Pair<Integer, TaskLocation> pair = beaconUpdates.get(beacon);

            // New prediction is different from the old one. Reset the counter.
            if (!pair.b().equals(taskPrediction)) {
                beaconUpdates.put(beacon, Pair.of(1, taskPrediction));
                return;
            }

            // New prediction is the same as the old one. We got the same prediction multiple times in a row.
            if (pair.a() + 1 >= BEACON_UPDATE_CHANGE_THRESHOLD) {
                beacons.put(beacon, taskPrediction);
                LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();
                beaconUpdates.remove(beacon);

                McUtils.sendMessageToClient(Component.literal(
                        "Task location prediction for " + beacon.color() + " changed to " + taskPrediction));

                return;
            }

            // New prediction is the same as the old one, but we haven't reached the threshold yet.
            beaconUpdates.put(beacon, Pair.of(pair.a() + 1, taskPrediction));
        } else {
            beaconUpdates.put(beacon, Pair.of(1, taskPrediction));
        }
    }

    // When we get close to a beacon, it get's removed.
    // This is our signal to know that this can be the current beacon,
    // but we don't know for sure until the scoreboard confirms it.
    @SubscribeEvent
    public void onBeaconRemove(BeaconEvent.Removed event) {
        Beacon beacon = event.getBeacon();
        if (!beacon.color().isUsedInLootruns()) return;

        double newBeaconDistanceToPlayer = VectorUtils.distanceIgnoringY(
                beacon.location().toPosition(), McUtils.mc().player.position());
        double oldBeaconDistanceToPlayer = closestBeacon == null
                ? Double.MAX_VALUE
                : VectorUtils.distanceIgnoringY(
                        closestBeacon.location().toPosition(),
                        McUtils.mc().player.position());
        if (newBeaconDistanceToPlayer < BEACON_REMOVAL_RADIUS
                && newBeaconDistanceToPlayer < oldBeaconDistanceToPlayer) {
            closestBeacon = event.getBeacon();
            closestBeaconStorage.touched();
        } else {
            beacons.remove(beacon);
            LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();
            beaconUpdates.remove(beacon);
        }
    }

    @SubscribeEvent
    public void onBeaconAdded(BeaconEvent.Added event) {
        Beacon beacon = event.getBeacon();
        if (!beacon.color().isUsedInLootruns()) return;

        TaskLocation taskPrediction = getBeaconTaskLocationPrediction(beacon);
        beacons.put(beacon, taskPrediction);
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
        return Optional.ofNullable(location);
    }

    public Optional<LootrunTaskType> getTaskType() {
        return Optional.ofNullable(taskType);
    }

    public Map<Beacon, TaskLocation> getBeacons() {
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

    private void handleStateChange(LootrunningState oldState, LootrunningState newState) {
        if (newState == LootrunningState.NOT_RUNNING) {
            selectedBeacons = new HashMap<>();
            closestBeacon = null;
            taskType = null;
            return;
        }

        if (oldState == LootrunningState.NOT_RUNNING) {
            location = LootrunLocation.fromCoordinates(McUtils.mc().player.position());
            WynntilsMod.info("Started a lootrun at " + location);
            return;
        }

        if (oldState == LootrunningState.CHOOSING_BEACON
                && newState == LootrunningState.IN_TASK
                && closestBeacon != null) {
            WynntilsMod.info("Selected a " + closestBeacon.color() + " beacon at " + closestBeacon.location());
            selectedBeacons.put(closestBeacon.color(), selectedBeacons.getOrDefault(closestBeacon.color(), 0) + 1);
            WynntilsMod.postEvent(new LootrunBeaconSelectedEvent(closestBeacon));

            // We selected a beacon, so other beacons are no longer relevant.
            beacons.clear();
            LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();

            return;
        }
    }

    // FIXME: Handle the same task location being used for multiple beacons.
    private TaskLocation getBeaconTaskLocationPrediction(Beacon beacon) {
        Set<TaskLocation> currentTaskLocations = taskLocations.get(location);
        if (currentTaskLocations.isEmpty()) return null;

        double lowestDistance = Double.MAX_VALUE;
        TaskLocation closestTaskLocation = null;
        for (TaskLocation currentTaskLocation : currentTaskLocations) {
            // 1: player location
            Vector2i playerPosition =
                    new Vector2i((int) McUtils.player().position().x(), (int)
                            McUtils.player().position().z());
            // 2: task location
            Vector2i taskLocationPosition = new Vector2i(
                    currentTaskLocation.location().x(),
                    currentTaskLocation.location().z());
            // 3: wynn beacon
            Vector2i beaconPosition =
                    new Vector2i(beacon.location().x(), beacon.location().z());

            // d = ((x2 - x1)(y1 - y3) - (x1 - x3)(y2 - y1)) / sqrt((x2 - x1)^2 + (y2 - y1)^2)
            double distance = Math.abs(
                    ((taskLocationPosition.x() - playerPosition.x()) * (playerPosition.y() - beaconPosition.y())
                                    - (playerPosition.x() - beaconPosition.x())
                                            * (taskLocationPosition.y() - playerPosition.y()))
                            / Math.sqrt(Math.pow(taskLocationPosition.x() - playerPosition.x(), 2)
                                    + Math.pow(taskLocationPosition.y() - playerPosition.y(), 2)));

            if (distance < lowestDistance) {
                lowestDistance = distance;
                closestTaskLocation = currentTaskLocation;
            }
        }

        return closestTaskLocation;
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
