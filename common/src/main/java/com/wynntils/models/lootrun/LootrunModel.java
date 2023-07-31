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
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.models.beacons.BeaconModel;
import com.wynntils.models.beacons.event.BeaconEvent;
import com.wynntils.models.beacons.type.BeaconColor;
import com.wynntils.models.beacons.type.VerifiedBeacon;
import com.wynntils.models.lootrun.event.LootrunBeaconSelectedEvent;
import com.wynntils.models.lootrun.scoreboard.LootrunScoreboardPart;
import com.wynntils.models.lootrun.type.LootrunLocation;
import com.wynntils.models.lootrun.type.LootrunTaskType;
import com.wynntils.models.lootrun.type.LootrunningState;
import com.wynntils.models.lootrun.type.TaskLocation;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.VectorUtils;
import com.wynntils.utils.mc.McUtils;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
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

    private static final LootrunScoreboardPart LOOTRUN_SCOREBOARD_PART = new LootrunScoreboardPart();

    private Map<LootrunLocation, Set<TaskLocation>> taskLocations = new HashMap<>();

    private LootrunFinishedEventBuilder.Completed lootrunCompletedBuilder;
    private LootrunFinishedEventBuilder.Failed lootrunFailedBuilder;

    private LootrunningState lootrunningState = LootrunningState.NOT_RUNNING;
    private LootrunLocation currentLocation;
    private LootrunTaskType currentTaskType;
    private Map<BeaconColor, Integer> currentLootrunSelectedBeacons = new HashMap<>();
    private Map<VerifiedBeacon, TaskLocation> currentBeacons = new HashMap<>();
    private VerifiedBeacon currentClosestBeacon;

    public LootrunModel(BeaconModel beaconModel) {
        super(List.of(beaconModel));

        Handlers.Scoreboard.addPart(LOOTRUN_SCOREBOARD_PART);
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

        // FIXME: Persist this in a later PR.
        lootrunningState = LootrunningState.NOT_RUNNING;
        currentLocation = null;
        currentTaskType = null;
        currentLootrunSelectedBeacons = new HashMap<>();
        currentBeacons = new HashMap<>();
        currentClosestBeacon = null;
    }

    @SubscribeEvent
    public void onBeaconMoved(BeaconEvent.Moved event) {
        if (currentLocation == null) return;
        VerifiedBeacon beacon = event.getBeacon();
        if (!beacon.getColor().getContentType().showsUpInLootruns()) return;

        TaskLocation taskPrediction = getBeaconTaskLocationPrediction(beacon);
        TaskLocation oldPrediction = currentBeacons.put(beacon, taskPrediction);

        if (!Objects.equals(oldPrediction, taskPrediction)) {
            McUtils.sendMessageToClient(Component.literal(
                    "Task location prediction for " + beacon.getColor() + " changed to " + taskPrediction));
        }
    }

    // When we get close to a beacon, it get's removed.
    // This is our signal to know that this can be the current beacon,
    // but we don't know for sure until the scoreboard confirms it.
    @SubscribeEvent
    public void onBeaconRemove(BeaconEvent.Removed event) {
        VerifiedBeacon beacon = event.getBeacon();
        if (!beacon.getColor().getContentType().showsUpInLootruns()) return;

        double newBeaconDistanceToPlayer = VectorUtils.distanceIgnoringY(
                beacon.getPosition(), McUtils.mc().player.position());
        double oldBeaconDistanceToPlayer = currentClosestBeacon == null
                ? Double.MAX_VALUE
                : VectorUtils.distanceIgnoringY(
                        currentClosestBeacon.getPosition(), McUtils.mc().player.position());
        if (newBeaconDistanceToPlayer < BEACON_REMOVAL_RADIUS
                && newBeaconDistanceToPlayer < oldBeaconDistanceToPlayer) {
            currentClosestBeacon = event.getBeacon();
        } else {
            currentBeacons.remove(beacon);
        }
    }

    @SubscribeEvent
    public void onBeaconAdd(BeaconEvent.Added event) {
        VerifiedBeacon beacon = event.getBeacon();
        if (!beacon.getColor().getContentType().showsUpInLootruns()) return;

        TaskLocation taskPrediction = getBeaconTaskLocationPrediction(beacon);
        currentBeacons.put(beacon, taskPrediction);
        McUtils.sendMessageToClient(
                Component.literal("Task location prediction for " + beacon.getColor() + " is " + taskPrediction));
    }

    public int getSelectedBeaconCount(BeaconColor color) {
        return currentLootrunSelectedBeacons.getOrDefault(color, 0);
    }

    public LootrunningState getState() {
        return lootrunningState;
    }

    public Optional<LootrunLocation> getCurrentLocation() {
        return Optional.ofNullable(currentLocation);
    }

    public Optional<LootrunTaskType> getCurrentTaskType() {
        return Optional.ofNullable(currentTaskType);
    }

    public void setState(LootrunningState newState, LootrunTaskType taskType) {
        // If nothing changes, don't do anything.
        if (this.lootrunningState == newState) return;

        LootrunningState oldState = this.lootrunningState;
        this.lootrunningState = newState;
        this.currentTaskType = taskType;

        handleStateChange(oldState, newState);
    }

    private void handleStateChange(LootrunningState oldState, LootrunningState newState) {
        if (newState == LootrunningState.NOT_RUNNING) {
            currentLootrunSelectedBeacons = new HashMap<>();
            currentClosestBeacon = null;
            currentTaskType = null;
            return;
        }

        if (oldState == LootrunningState.NOT_RUNNING) {
            currentLocation =
                    LootrunLocation.fromCoordinates(McUtils.mc().player.position());
            WynntilsMod.info("Started a lootrun at " + currentLocation);
            return;
        }

        if (oldState == LootrunningState.CHOOSING_BEACON
                && newState == LootrunningState.IN_TASK
                && currentClosestBeacon != null) {
            WynntilsMod.info("Selected a " + currentClosestBeacon.getColor() + " beacon at "
                    + currentClosestBeacon.getPosition());
            currentLootrunSelectedBeacons.put(
                    currentClosestBeacon.getColor(),
                    currentLootrunSelectedBeacons.getOrDefault(currentClosestBeacon.getColor(), 0) + 1);
            WynntilsMod.postEvent(new LootrunBeaconSelectedEvent(currentClosestBeacon));
            return;
        }
    }

    private TaskLocation getBeaconTaskLocationPrediction(VerifiedBeacon beacon) {
        Set<TaskLocation> currentTaskLocations = taskLocations.get(currentLocation);
        if (currentTaskLocations.isEmpty()) return null;

        // FIXME: too basic math
        double lowestDistance = Double.MAX_VALUE;
        TaskLocation closestTaskLocation = null;
        for (TaskLocation currentTaskLocation : currentTaskLocations) {
            // 1: player location
            Vector2d playerPosition = new Vector2d(
                    McUtils.player().position().x(), McUtils.player().position().z());
            // 2: wynn beacon
            Vector2d beaconPosition =
                    new Vector2d(beacon.getPosition().x(), beacon.getPosition().z());
            // 3: task location
            Vector2d taskLocationPosition = new Vector2d(
                    currentTaskLocation.location().x(),
                    currentTaskLocation.location().z());

            Vector2d distanceBetweenPlayerTask = new Vector2d(0, 0);
            taskLocationPosition.sub(playerPosition, distanceBetweenPlayerTask);

            Vector2d distanceBetweenPlayerBeacon = new Vector2d(0, 0);
            beaconPosition.sub(playerPosition, distanceBetweenPlayerBeacon);

            Vector2d distanceBetweenTaskBeacon = new Vector2d(0, 0);
            taskLocationPosition.sub(beaconPosition, distanceBetweenTaskBeacon);

            double distance = Math.abs(distanceBetweenPlayerTask.length()
                    - distanceBetweenPlayerBeacon.length()
                    - distanceBetweenTaskBeacon.length());

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
