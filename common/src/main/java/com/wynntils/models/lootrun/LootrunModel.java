/*
 * Copyright © Wynntils 2023-2024.
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
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.handlers.particle.event.ParticleVerifiedEvent;
import com.wynntils.handlers.particle.type.ParticleType;
import com.wynntils.mc.event.SetEntityDataEvent;
import com.wynntils.mc.extension.EntityExtension;
import com.wynntils.models.beacons.event.BeaconEvent;
import com.wynntils.models.beacons.type.Beacon;
import com.wynntils.models.beacons.type.BeaconColor;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.containers.event.MythicFoundEvent;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.InsulatorItem;
import com.wynntils.models.items.items.game.SimulatorItem;
import com.wynntils.models.lootrun.event.LootrunBeaconSelectedEvent;
import com.wynntils.models.lootrun.event.LootrunFinishedEvent;
import com.wynntils.models.lootrun.event.LootrunFinishedEventBuilder;
import com.wynntils.models.lootrun.markers.LootrunBeaconMarkerProvider;
import com.wynntils.models.lootrun.particle.LootrunTaskParticleVerifier;
import com.wynntils.models.lootrun.scoreboard.LootrunScoreboardPart;
import com.wynntils.models.lootrun.type.LootrunLocation;
import com.wynntils.models.lootrun.type.LootrunTaskType;
import com.wynntils.models.lootrun.type.LootrunningState;
import com.wynntils.models.lootrun.type.MissionType;
import com.wynntils.models.lootrun.type.TaskLocation;
import com.wynntils.models.lootrun.type.TaskPrediction;
import com.wynntils.models.marker.MarkerModel;
import com.wynntils.models.npc.label.NpcLabelInfo;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.VectorUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.PosUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
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
    private static final Pattern REWARD_SACRIFICES_PATTERN = Pattern.compile("[À\\s]*§.(\\d+)§7 Reward Sacrifices§r");
    private static final Pattern LOOTRUN_EXPERIENCE_PATTERN = Pattern.compile("[À\\s]*§.(\\d+)§7 Lootrun Experience§r");

    // Statistics
    private static final Pattern TIME_ELAPSED_PATTERN = Pattern.compile("[À\\s]*§7Time Elapsed: §.(\\d+):(\\d+)");
    private static final Pattern MOBS_KILLED_PATTERN = Pattern.compile("[À\\s]*§7Mobs Killed: §.(\\d+)");
    private static final Pattern CHESTS_OPENED_PATTERN = Pattern.compile("[À\\s]*§7Chests Open: §.(\\d+)");
    private static final Pattern CHALLENGES_COMPLETED_PATTERN =
            Pattern.compile("[À\\s]*§7Challenges Completed: §.(\\d+)");

    //                             À§c§lLootrun Failed!
    //                         ÀÀ§7Better luck next time!
    //
    //                                 ÀÀ§dStatistics
    //                           ÀÀ§7Time Elapsed: §f13:45
    //                        ÀÀÀ§7Challenges Completed: §f7

    private static final Pattern LOOTRUN_FAILED_PATTERN = Pattern.compile("[À\\s]*§c§lLootrun Failed!");
    private static final Pattern CHALLENGE_FAILED_PATTERN = Pattern.compile("À.*?§c§lChallenge Failed!");

    private static final Pattern MISSION_COMPLETED_PATTERN = Pattern.compile("[À\\s]*§b§lMission Completed");

    // These two prefixes are used to identify when a mission is completed,
    // as this information is presented in multiple ways
    private static final String ADVANCED_MISSION_PREFIX = "À.*?§b§l";
    private static final String MISSION_PREFIX = ".*?§.";

    private static final float BEACON_REMOVAL_RADIUS = 25f;

    // Beacon positions are sometimes off by a few blocks
    private static final int TASK_POSITION_ERROR = 3;

    private static final int LOOTRUN_MASTER_REWARDS_RADIUS = 20;
    private static final String LOOTRUN_MASTER_NAME = "Lootrun Master";

    private static final LootrunScoreboardPart LOOTRUN_SCOREBOARD_PART = new LootrunScoreboardPart();

    private static final LootrunBeaconMarkerProvider LOOTRUN_BEACON_COMPASS_PROVIDER =
            new LootrunBeaconMarkerProvider();

    @Persisted
    public final Storage<Integer> dryPulls = new Storage<>(0);

    private Location closestLootrunMasterLocation = null;
    private Set<UUID> checkedItemEntities = new HashSet<>();

    private Map<LootrunLocation, Set<TaskLocation>> taskLocations = new HashMap<>();

    private LootrunFinishedEventBuilder.Completed lootrunCompletedBuilder;
    private LootrunFinishedEventBuilder.Failed lootrunFailedBuilder;

    // Data that can live in memory, when joining a class we will parse these
    private LootrunningState lootrunningState = LootrunningState.NOT_RUNNING;
    private LootrunTaskType taskType;

    // rely on color, beacon positions change
    private Map<BeaconColor, TaskPrediction> beacons = new HashMap<>();

    // particles can accurately show task locations
    private Set<TaskLocation> possibleTaskLocations = new HashSet<>();

    // Data to be persisted
    @Persisted
    private final Storage<Map<String, Map<BeaconColor, Integer>>> selectedBeaconsStorage =
            new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<String, BeaconColor>> lastTaskBeaconColorStorage = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<String, Beacon>> closestBeaconStorage = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<String, Integer>> redBeaconTaskCountStorage = new Storage<>(new TreeMap<>());

    private Map<BeaconColor, Integer> selectedBeacons = new TreeMap<>();

    @Persisted
    private final Storage<Map<String, List<String>>> missionStorage = new Storage<>(new TreeMap<>());

    private final Pattern[] missionPatterns = new Pattern[2];
    private final StyledText[] recentChatMessages = new StyledText[5];

    private int timeLeft = 0;
    private CappedValue challenges = CappedValue.EMPTY;

    public LootrunModel(MarkerModel markerModel) {
        super(List.of(markerModel));

        Handlers.Scoreboard.addPart(LOOTRUN_SCOREBOARD_PART);
        Handlers.Particle.registerParticleVerifier(ParticleType.LOOTRUN_TASK, new LootrunTaskParticleVerifier());
        Models.Marker.registerMarkerProvider(LOOTRUN_BEACON_COMPASS_PROVIDER);
        reloadData();
        generateMissionPatterns();
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

        boolean foundTaskLocation = false;
        for (Set<TaskLocation> taskLocationsForLocation : taskLocations.values()) {
            for (TaskLocation taskLocation : taskLocationsForLocation) {
                if (PosUtils.closerThanIgnoringY(
                        taskLocation.location().toVec3(), event.getParticle().position(), TASK_POSITION_ERROR)) {
                    // Note: We do this re-allocation so we always display the correct location,
                    //       even if it slightly changed, or our data is imprecise.
                    possibleTaskLocations.add(new TaskLocation(
                            taskLocation.name(),
                            Location.containing(event.getParticle().position()),
                            taskLocation.taskType()));

                    foundTaskLocation = true;
                    break;
                }
            }

            if (foundTaskLocation) break;
        }

        if (!foundTaskLocation) {
            // Our possible task location set did not contain the particle location,
            // so add a new "unknown" task location to the set.
            Location location = Location.containing(event.getParticle().position());
            possibleTaskLocations.add(new TaskLocation(location.toString(), location, LootrunTaskType.UNKNOWN));
        }

        // Only log this in development environments.
        if (!WynntilsMod.isDevelopmentEnvironment()) return;

        // Check if we have tasks from multiple locations, log in case we do.
        for (LootrunLocation location : LootrunLocation.values()) {
            List<TaskLocation> tasksInLocation = possibleTaskLocations.stream()
                    .filter(taskLocation ->
                            taskLocations.getOrDefault(location, Set.of()).contains(taskLocation))
                    .toList();

            if (!tasksInLocation.isEmpty() && tasksInLocation.size() < possibleTaskLocations.size()) {
                List<TaskLocation> tasksNotInLocation = possibleTaskLocations.stream()
                        .filter(taskLocation -> !tasksInLocation.contains(taskLocation))
                        .toList();

                WynntilsMod.warn("Found tasks from multiple locations: " + possibleTaskLocations);
                WynntilsMod.warn("Task location is: " + location);
                WynntilsMod.warn("Tasks in location: " + tasksInLocation);
                WynntilsMod.warn("Tasks outside location: " + tasksNotInLocation);
                break;
            }
        }
    }

    @SubscribeEvent
    public void onCharacterChange(CharacterUpdateEvent event) {
        String id = Models.Character.getId();

        selectedBeaconsStorage.get().putIfAbsent(id, new TreeMap<>());
        selectedBeacons = selectedBeaconsStorage.get().get(id);

        selectedBeaconsStorage.touched();

        missionStorage.get().putIfAbsent(id, new LinkedList<>());
        missionStorage.touched();
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageReceivedEvent event) {
        if (event.getRecipientType() != RecipientType.INFO) return;
        StyledText styledText = event.getOriginalStyledText();
        updateRecentChatMessages(styledText);

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

        Matcher matcher = missionPatterns[0].matcher(styledText.toString());
        if (matcher.find()) {
            MissionType mission = MissionType.fromName(matcher.group(1));
            addMission(mission.name());
            return;
        }

        matcher = missionPatterns[1].matcher(styledText.toString());

        if (matcher.find()) {
            MissionType mission = MissionType.fromName(matcher.group(1));

            for (StyledText recentChatMessage : recentChatMessages) {
                matcher = MISSION_COMPLETED_PATTERN.matcher(recentChatMessage.toString());
                if (matcher.find()) {
                    addMission(mission.name());
                    return;
                }
            }
        }

        matcher = CHALLENGE_FAILED_PATTERN.matcher(styledText.toString());

        if (matcher.find()) {
            BeaconColor color = getLastTaskBeaconColor();
            if (color == BeaconColor.GRAY) addMission("FAILED");
        }
    }

    @SubscribeEvent
    public void onNpcLabelFound(LabelIdentifiedEvent event) {
        if (event.getLabelInfo() instanceof NpcLabelInfo npcLabelInfo) {
            if (npcLabelInfo.getName().equals(LOOTRUN_MASTER_NAME)) {
                closestLootrunMasterLocation = event.getLabelInfo().getLocation();
            }
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(SetEntityDataEvent event) {
        Entity entity = McUtils.mc().level.getEntity(event.getId());
        if (!(entity instanceof ItemEntity itemEntity)) return;

        // We only care about items that are close to the lootrun master
        // If we don't know where the lootrun master is, we probably don't care
        if (closestLootrunMasterLocation == null) return;

        // Check if the item is close enough to the lootrun master
        if (closestLootrunMasterLocation.toBlockPos().distSqr(itemEntity.blockPosition())
                > Math.pow(LOOTRUN_MASTER_REWARDS_RADIUS, 2)) {
            return;
        }

        // Check if we've already checked this item entity
        // Otherwise duplication can occur
        if (checkedItemEntities.contains(itemEntity.getUUID())) return;

        checkedItemEntities.add(itemEntity.getUUID());

        // Detect lootrun end reward items by checking the appearing item entities
        // This is much more reliable than checking the item in the chest,
        // as the chest can be rerolled, etc.
        for (SynchedEntityData.DataValue<?> packedItem : event.getPackedItems()) {
            if (packedItem.id() == ItemEntity.DATA_ITEM.getId()) {
                if (!(packedItem.value() instanceof ItemStack itemStack)) return;

                boolean foundLootrunMythic = false;
                Optional<GearItem> gearItemOpt = Models.Item.asWynnItem(itemStack, GearItem.class);
                if (gearItemOpt.isPresent()) {
                    GearItem gearItem = gearItemOpt.get();

                    if (gearItem.getGearTier() == GearTier.MYTHIC) {
                        foundLootrunMythic = true;
                    }
                }

                // No need to check tier for these as they are only mythic
                Optional<InsulatorItem> insulatorItemOpt = Models.Item.asWynnItem(itemStack, InsulatorItem.class);
                if (insulatorItemOpt.isPresent()) {
                    foundLootrunMythic = true;
                }

                Optional<SimulatorItem> simulatorItemOpt = Models.Item.asWynnItem(itemStack, SimulatorItem.class);
                if (simulatorItemOpt.isPresent()) {
                    foundLootrunMythic = true;
                }

                if (foundLootrunMythic) {
                    WynntilsMod.postEvent(new MythicFoundEvent(itemStack, true));
                    dryPulls.store(0);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLootrunCompleted(LootrunFinishedEvent.Completed event) {
        dryPulls.store(dryPulls.get() + event.getRewardPulls());
        checkedItemEntities.clear();
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

    public String getMissionStatus(int index) {
        List<String> missions = missionStorage.get().get(Models.Character.getId());
        if (missions == null) return "§cNone";

        if (index < 0 || index >= missions.size()) return "§cNone";

        String identifier = missionStorage.get().get(Models.Character.getId()).get(index);
        if (identifier.equals("FAILED")) return "§4Failed";

        MissionType mission = MissionType.valueOf(identifier);
        return mission == null ? "§7Unknown" : mission.getColoredName();
    }

    public LootrunningState getState() {
        return lootrunningState;
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

    private void resetRedBeaconTaskCount() {
        redBeaconTaskCountStorage.get().remove(Models.Character.getId());
        redBeaconTaskCountStorage.touched();
    }

    private void resetMissions() {
        List<String> missions = missionStorage.get().get(Models.Character.getId());
        if (missions == null) return;

        missions.clear();
        missionStorage.get().put(Models.Character.getId(), missions);
        missionStorage.touched();
    }

    private void addMission(String identifier) {
        List<String> missions = missionStorage.get().getOrDefault(Models.Character.getId(), new LinkedList<>());

        for (String mission : missions) {
            if (mission.equals(identifier)) {
                WynntilsMod.info("Mission already exists.");
                return;
            }
        }

        missions.add(identifier);
        missionStorage.get().put(Models.Character.getId(), missions);
        missionStorage.touched();
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
            resetMissions();

            taskType = null;
            setClosestBeacon(null);
            setLastTaskBeaconColor(null);
            resetRedBeaconTaskCount();

            possibleTaskLocations = new HashSet<>();

            beacons = new HashMap<>();

            timeLeft = 0;
            challenges = CappedValue.EMPTY;
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
            WynntilsMod.postEvent(new LootrunBeaconSelectedEvent(
                    closestBeacon, beacons.get(closestBeacon.color()).taskLocation()));

            possibleTaskLocations = new HashSet<>();

            // We selected a beacon, so other beacons are no longer relevant.
            beacons.clear();
            setClosestBeacon(null);
            LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();
            return;
        }
    }

    private void updateTaskLocationPrediction(Beacon beacon) {
        Set<TaskLocation> currentTaskLocations = possibleTaskLocations;
        if (currentTaskLocations == null || currentTaskLocations.isEmpty()) {
            WynntilsMod.warn("No task locations found. Using fallback, all locations.");
            currentTaskLocations =
                    taskLocations.values().stream().flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet());
        }
        if (currentTaskLocations == null || currentTaskLocations.isEmpty()) {
            WynntilsMod.warn("Fallback failed, no task locations found!");
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

        matcher = styledText.getMatcher(REWARD_SACRIFICES_PATTERN);
        if (matcher.find()) {
            lootrunCompletedBuilder.setRewardSacrifices(Integer.parseInt(matcher.group(1)));

            matcher = styledText.getMatcher(CHESTS_OPENED_PATTERN);
            if (matcher.find()) {
                lootrunCompletedBuilder.setChestsOpened(Integer.parseInt(matcher.group(1)));
                return;
            }

            WynntilsMod.warn("Found lootrun sacrifices but no chests opened: " + styledText);
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

    private void generateMissionPatterns() {
        List<String> patterns = new LinkedList<>();
        patterns.add(ADVANCED_MISSION_PREFIX);
        patterns.add(MISSION_PREFIX);

        for (int i = 0; i < patterns.size(); i++) {
            StringBuilder patternBuilder = new StringBuilder(patterns.get(i));

            patternBuilder.append("(");

            for (MissionType mission : MissionType.values()) {
                patternBuilder.append(Pattern.quote(mission.getName())).append("|");
            }

            patternBuilder.setLength(patternBuilder.length() - 1);

            patternBuilder.append(")");
            missionPatterns[i] = Pattern.compile(patternBuilder.toString());
        }
    }

    private void updateRecentChatMessages(StyledText newMessage) {
        for (int i = recentChatMessages.length - 1; i > 0; i--) {
            recentChatMessages[i] = recentChatMessages[i - 1];
        }
        recentChatMessages[0] = newMessage;
    }
}
