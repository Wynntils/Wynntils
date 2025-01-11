/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun;

import com.google.common.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.DownloadRegistry;
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
import com.wynntils.models.beacons.event.BeaconMarkerEvent;
import com.wynntils.models.beacons.type.Beacon;
import com.wynntils.models.beacons.type.BeaconMarker;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.containers.event.MythicFoundEvent;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.InsulatorItem;
import com.wynntils.models.items.items.game.SimulatorItem;
import com.wynntils.models.lootrun.beacons.LootrunBeaconKind;
import com.wynntils.models.lootrun.beacons.LootrunBeaconMarkerKind;
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
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import org.joml.Vector2d;
import org.joml.Vector3d;

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

    private static final Pattern LOOTRUN_COMPLETED_PATTERN = Pattern.compile("\uDB00\uDC61§6§lLootrun Completed!");

    // Rewards
    private static final Pattern REWARD_PULLS_PATTERN = Pattern.compile("§.(\\d+)§7 Reward Pulls§r");
    private static final Pattern REWARD_REROLLS_PATTERN = Pattern.compile("§.(\\d+)§7 Reward Rerolls§r");
    private static final Pattern REWARD_SACRIFICES_PATTERN = Pattern.compile("§.(\\d+)§7 Reward Sacrifices§r");
    private static final Pattern LOOTRUN_EXPERIENCE_PATTERN = Pattern.compile("§.(\\d+)§7 Lootrun Experience§r");

    // Statistics
    private static final Pattern TIME_ELAPSED_PATTERN = Pattern.compile("§7Time Elapsed: §.(\\d+):(\\d+)");
    private static final Pattern MOBS_KILLED_PATTERN = Pattern.compile("§7Mobs Killed: §.(\\d+)");
    private static final Pattern CHESTS_OPENED_PATTERN = Pattern.compile("§7Chests Open: §.(\\d+)");
    private static final Pattern CHALLENGES_COMPLETED_PATTERN = Pattern.compile("§7Challenges Completed: §.(\\d+)");

    //                             À§c§lLootrun Failed!
    //                         ÀÀ§7Better luck next time!
    //
    //                                 ÀÀ§dStatistics
    //                           ÀÀ§7Time Elapsed: §f13:45
    //                        ÀÀÀ§7Challenges Completed: §f7

    private static final Pattern LOOTRUN_FAILED_PATTERN = Pattern.compile("\uDB00\uDC6D§c§lLootrun Failed!");
    private static final Pattern CHALLENGE_FAILED_PATTERN = Pattern.compile("\uDB00\uDC6A§c§lChallenge Failed!");

    private static final Pattern MISSION_COMPLETED_PATTERN = Pattern.compile("[À\\s]*§b§lMission Completed");

    // Some missions don't have a mission completed message, so we also look for "active" missions
    // (missions that apply effects on challenge completion)
    private static final Pattern COMPLETED_MISSION_PATTERN = Pattern.compile("[À\\s]*?§.(?<mission>"
            + MissionType.missionTypes().stream().map(MissionType::getName).collect(Collectors.joining("|")) + ")");
    private static final Pattern ACTIVE_MISSION_PATTERN = Pattern.compile("[À\\s]*?§b§l(?<mission>"
            + MissionType.missionTypes().stream().map(MissionType::getName).collect(Collectors.joining("|")) + ")");

    private static final float BEACON_REMOVAL_RADIUS = 25f;

    // Beacon positions are sometimes off by a few blocks
    private static final int TASK_POSITION_ERROR = 3;

    // Sometimes the calculated distance between the player and a task is greater than the distance on the marker
    private static final int TASK_DISTANCE_ERROR = 15;

    // Task markers lose their distance number when the player is around this blocks away from the task
    private static final int MARKER_DISTANCE_THRESHOLD = 17;

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
    private Map<LootrunBeaconKind, TaskPrediction> beacons = new HashMap<>();

    // particles can accurately show task locations
    private Set<TaskLocation> possibleTaskLocations = new HashSet<>();

    private Map<LootrunBeaconKind, Integer> selectedBeacons = new TreeMap<>();
    private int timeLeft = 0;
    private CappedValue challenges = CappedValue.EMPTY;

    private boolean expectMissionComplete = false;

    // Data to be persisted
    @Persisted
    private final Storage<Map<String, Map<LootrunBeaconKind, Integer>>> selectedBeaconsStorage =
            new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<String, LootrunBeaconKind>> lastTaskBeaconColorStorage = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<String, Beacon<LootrunBeaconKind>>> closestBeaconStorage = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<String, Integer>> redBeaconTaskCountStorage = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<String, List<MissionType>>> missionStorage = new Storage<>(new TreeMap<>());

    private List<Pair<Beacon<LootrunBeaconKind>, EntityExtension>> activeBeacons = new ArrayList<>();

    public LootrunModel(MarkerModel markerModel) {
        super(List.of(markerModel));

        Handlers.Scoreboard.addPart(LOOTRUN_SCOREBOARD_PART);
        Handlers.Particle.registerParticleVerifier(ParticleType.LOOTRUN_TASK, new LootrunTaskParticleVerifier());
        Models.Marker.registerMarkerProvider(LOOTRUN_BEACON_COMPASS_PROVIDER);

        for (LootrunBeaconKind beaconKind : LootrunBeaconKind.values()) {
            Models.Beacon.registerBeacon(beaconKind);
        }

        for (LootrunBeaconMarkerKind markerKind : LootrunBeaconMarkerKind.values()) {
            Models.Beacon.registerBeaconMarker(markerKind);
        }
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_LOOTRUN_TASKS_NAMED).handleReader(this::handleLootrunTaskLocations);
    }

    private void handleLootrunTaskLocations(Reader reader) {
        Type type = new TypeToken<Map<LootrunLocation, Set<TaskLocation>>>() {}.getType();
        taskLocations = Managers.Json.GSON.fromJson(reader, type);
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

        missionStorage.get().putIfAbsent(id, new ArrayList<>());
        missionStorage.touched();
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

        Matcher matcher = MISSION_COMPLETED_PATTERN.matcher(styledText.getString());
        if (matcher.matches()) {
            expectMissionComplete = true;
            return;
        }

        if (expectMissionComplete) {
            matcher = COMPLETED_MISSION_PATTERN.matcher(styledText.getString());
            if (matcher.matches()) {
                MissionType mission = MissionType.fromName(matcher.group("mission"));
                addMission(mission);
                return;
            }
        }

        matcher = ACTIVE_MISSION_PATTERN.matcher(styledText.getString());
        if (matcher.find()) {
            MissionType mission = MissionType.fromName(matcher.group("mission"));
            addMission(mission);
            return;
        }

        matcher = CHALLENGE_FAILED_PATTERN.matcher(styledText.getString());
        if (matcher.matches()) {
            LootrunBeaconKind color = getLastTaskBeaconColor();
            if (color == LootrunBeaconKind.GRAY) {
                addMission(MissionType.FAILED);
            }
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
        int idToCheck;

        // Currently the items are ItemEntity's however this may change in the future so we want to check for
        // ItemDisplay's too to ensure future compatibility.
        if (entity instanceof ItemEntity) {
            idToCheck = ItemEntity.DATA_ITEM.id();
        } else if (entity instanceof Display.ItemDisplay) {
            idToCheck = Display.ItemDisplay.DATA_ITEM_STACK_ID.id();
        } else {
            return;
        }

        // We only care about items that are close to the lootrun master
        // If we don't know where the lootrun master is, we probably don't care
        if (closestLootrunMasterLocation == null) return;

        // Check if the item is close enough to the lootrun master
        if (closestLootrunMasterLocation.toBlockPos().distSqr(entity.blockPosition())
                > Math.pow(LOOTRUN_MASTER_REWARDS_RADIUS, 2)) {
            return;
        }

        // Check if we've already checked this item entity
        // Otherwise duplication can occur
        if (checkedItemEntities.contains(entity.getUUID())) return;

        checkedItemEntities.add(entity.getUUID());

        // Detect lootrun end reward items by checking the appearing item entities
        // This is much more reliable than checking the item in the chest,
        // as the chest can be rerolled, etc.
        for (SynchedEntityData.DataValue<?> packedItem : event.getPackedItems()) {
            if (packedItem.id() == idToCheck) {
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
                    WynntilsMod.postEvent(
                            new MythicFoundEvent(itemStack, MythicFoundEvent.MythicSource.LOOTRUN_REWARD_CHEST));
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
        // Resetting once when leaving the class is enough
        if (event.getNewState() == WorldState.WORLD) return;

        lootrunCompletedBuilder = null;
        lootrunFailedBuilder = null;

        possibleTaskLocations = new HashSet<>();

        lootrunningState = LootrunningState.NOT_RUNNING;
        taskType = null;
        beacons = new HashMap<>();
        activeBeacons = new ArrayList<>();
        LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();

        selectedBeacons = new TreeMap<>();

        challenges = CappedValue.EMPTY;
        timeLeft = 0;
    }

    // When we get close to a beacon, it gets removed.
    // This is our signal to know that this can be the current beacon,
    // but we don't know for sure until the scoreboard confirms it.
    @SubscribeEvent
    public void onBeaconRemove(BeaconEvent.Removed event) {
        Beacon beacon = event.getBeacon();
        if (!(beacon.beaconKind() instanceof LootrunBeaconKind lootrunBeaconKind)) return;

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
            beacons.remove(lootrunBeaconKind);
            LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();
        }

        activeBeacons.removeIf(beaconPair -> beaconPair.a().beaconKind() == lootrunBeaconKind);
    }

    @SubscribeEvent
    public void onBeaconAdded(BeaconEvent.Added event) {
        Beacon beacon = event.getBeacon();
        if (!(beacon.beaconKind() instanceof LootrunBeaconKind)) return;

        EntityExtension entity = ((EntityExtension) event.getEntity());

        // FIXME: Feature-model dependency
        CustomLootrunBeaconsFeature feature = Managers.Feature.getFeatureInstance(CustomLootrunBeaconsFeature.class);
        if (feature.removeOriginalBeacons.get() && feature.isEnabled()) {
            // Only set this once they are added.
            // This is cleaner than posting an event on render
            entity.setRendered(false);
        }

        activeBeacons.add(Pair.of(beacon, entity));
    }

    @SubscribeEvent
    public void onBeaconMoved(BeaconEvent.Moved event) {
        Beacon beacon = event.getNewBeacon();
        if (!(beacon.beaconKind() instanceof LootrunBeaconKind lootrunBeaconKind)) return;

        Pair<Beacon<LootrunBeaconKind>, EntityExtension> oldPair = null;

        for (Pair<Beacon<LootrunBeaconKind>, EntityExtension> activeBeacon : activeBeacons) {
            if (activeBeacon.a().beaconKind() == lootrunBeaconKind) {
                oldPair = activeBeacon;
                break;
            }
        }

        if (oldPair == null) return;

        Pair<Beacon<LootrunBeaconKind>, EntityExtension> newPair = Pair.of(beacon, oldPair.b());

        activeBeacons.remove(oldPair);
        activeBeacons.add(newPair);
    }

    @SubscribeEvent
    public void onBeaconMarkerAdded(BeaconMarkerEvent.Added event) {
        BeaconMarker beaconMarker = event.getBeaconMarker();
        if (!(beaconMarker.beaconMarkerKind() instanceof LootrunBeaconMarkerKind lootrunMarker)) return;

        EntityExtension entity = (EntityExtension) event.getEntity();

        // FIXME: Feature-model dependency
        CustomLootrunBeaconsFeature feature = Managers.Feature.getFeatureInstance(CustomLootrunBeaconsFeature.class);
        boolean shouldHide = feature.removeOriginalBeacons.get() && feature.isEnabled();
        if (shouldHide) {
            // Only set this once they are added.
            // This is cleaner than posting an event on render
            entity.setRendered(false);
        }

        // This will happen when getting close to the beacon so if we are close to the marker then we know why
        // there is no distance and can ignore it
        if (beaconMarker.distance().isEmpty()) {
            if (event.getEntity().position().distanceTo(McUtils.player().position()) >= MARKER_DISTANCE_THRESHOLD) {
                WynntilsMod.warn("Lootrun beacon has no distance");
                entity.setRendered(true);
            }

            return;
        }

        if (beaconMarker.color().isEmpty()) {
            WynntilsMod.warn("Lootrun beacon has no color");
            entity.setRendered(true);
            return;
        }

        Pair<Beacon<LootrunBeaconKind>, EntityExtension> beaconPair = null;

        for (Pair<Beacon<LootrunBeaconKind>, EntityExtension> activeBeacon : activeBeacons) {
            if (activeBeacon
                    .a()
                    .beaconKind()
                    .getCustomColor()
                    .equals(beaconMarker.color().get())) {
                beaconPair = activeBeacon;
                break;
            }
        }

        if (beaconPair == null) {
            entity.setRendered(true);
            return;
        }

        boolean foundBeacon = updateTaskLocationPrediction(
                        beaconPair.a(), lootrunMarker, beaconMarker.distance().get())
                || beacons.containsKey(beaconPair.a().beaconKind());

        entity.setRendered(!foundBeacon || !shouldHide);
        beaconPair.b().setRendered(!foundBeacon || !shouldHide);
    }

    // The marker is constantly remade so only the beacon visibility needs to be changed
    public void toggleBeacons(boolean visible) {
        for (Pair<Beacon<LootrunBeaconKind>, EntityExtension> beaconPair : activeBeacons) {
            // Only change visibility if it has been found, otherwise we need to keep the vanilla beacon
            if (beacons.containsKey(beaconPair.a().beaconKind())) {
                beaconPair.b().setRendered(!visible);
            }
        }
    }

    public int getBeaconCount(LootrunBeaconKind color) {
        return selectedBeacons.getOrDefault(color, 0);
    }

    public String getMissionStatus(int index, boolean colored) {
        List<MissionType> missions = missionStorage.get().getOrDefault(Models.Character.getId(), new ArrayList<>());
        if (index < 0 || index >= missions.size()) {
            return colored ? MissionType.UNKNOWN.getColoredName() : MissionType.UNKNOWN.getName();
        }

        MissionType mission = missionStorage.get().get(Models.Character.getId()).get(index);
        return colored ? mission.getColoredName() : mission.getName();
    }

    public LootrunningState getState() {
        return lootrunningState;
    }

    public Optional<LootrunTaskType> getTaskType() {
        return Optional.ofNullable(taskType);
    }

    public Map<LootrunBeaconKind, TaskPrediction> getBeacons() {
        return beacons;
    }

    public TaskLocation getTaskForColor(LootrunBeaconKind lootrunBeaconKind) {
        TaskPrediction taskPrediction = beacons.get(lootrunBeaconKind);
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

    public LootrunBeaconKind getLastTaskBeaconColor() {
        return lastTaskBeaconColorStorage.get().get(Models.Character.getId());
    }

    public Beacon getClosestBeacon() {
        return closestBeaconStorage.get().get(Models.Character.getId());
    }

    public int getRedBeaconTaskCount() {
        return redBeaconTaskCountStorage.get().getOrDefault(Models.Character.getId(), 0);
    }

    private void setLastTaskBeaconColor(LootrunBeaconKind lootrunBeaconKind) {
        if (lootrunBeaconKind == null) {
            lastTaskBeaconColorStorage.get().remove(Models.Character.getId());
        } else {
            lastTaskBeaconColorStorage.get().put(Models.Character.getId(), lootrunBeaconKind);
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
        missionStorage.get().putIfAbsent(Models.Character.getId(), new ArrayList<>());
        missionStorage.get().get(Models.Character.getId()).clear();
        missionStorage.touched();
    }

    private void addMission(MissionType mission) {
        List<MissionType> missions =
                missionStorage.get().computeIfAbsent(Models.Character.getId(), k -> new ArrayList<>());
        if (!missions.contains(mission)) {
            missions.add(mission);
        }

        missionStorage.touched();

        expectMissionComplete = false;
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
        if (getLastTaskBeaconColor() == LootrunBeaconKind.RED && amount.max() > oldChallenges.max()) {
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
                && closestBeacon != null
                && closestBeacon.beaconKind() instanceof LootrunBeaconKind color) {
            WynntilsMod.info("Selected a " + color + " beacon at " + closestBeacon.position());
            selectedBeacons.put(color, selectedBeacons.getOrDefault(closestBeacon.beaconKind(), 0) + 1);
            selectedBeaconsStorage.touched();
            setLastTaskBeaconColor(color);
            WynntilsMod.postEvent(new LootrunBeaconSelectedEvent(
                    closestBeacon, beacons.get(closestBeacon.beaconKind()).taskLocation()));

            possibleTaskLocations = new HashSet<>();

            // We selected a beacon, so other beacons are no longer relevant.
            beacons.clear();
            activeBeacons.clear();
            setClosestBeacon(null);
            LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();
            return;
        }
    }

    private boolean updateTaskLocationPrediction(Beacon beacon, LootrunBeaconMarkerKind lootrunMarker, int distance) {
        if (!(beacon.beaconKind() instanceof LootrunBeaconKind color)) return false;

        boolean foundTask = false;
        // Get the tasks found from particles as we know for certain there is a task there and it may include
        // unknown tasks
        Set<TaskLocation> currentTaskLocations = possibleTaskLocations.stream()
                .filter(possibleTask -> possibleTask.taskType() == lootrunMarker.getTaskType())
                .collect(Collectors.toSet());

        // Due to Wynncraft culling particles, after 5 or more beacon choices (sometimes less) we are no longer able
        // to rely on those for getting possible locations so we use the gathered task locations instead and we can
        // filter them based on the marker provided. The distance from the marker is also used to filter far
        // away tasks so whilst it would be ideal to only get tasks from current location this is fine for now.
        taskLocations.values().forEach(hashSet -> {
            currentTaskLocations.addAll(hashSet.stream()
                    .filter(task -> task.taskType() == lootrunMarker.getTaskType())
                    .collect(Collectors.toSet()));
        });

        if (currentTaskLocations.isEmpty()) {
            WynntilsMod.warn("No task locations found!");
            return foundTask;
        }

        List<TaskPrediction> usedTaskLocations = beacons.entrySet().stream()
                .filter(entry -> entry.getKey() != beacon.beaconKind())
                .map(Map.Entry::getValue)
                .toList();

        Map<Double, TaskLocation> predictionScores = new TreeMap<>();
        for (TaskLocation currentTaskLocation : currentTaskLocations) {
            Pair<Double, TaskLocation> prediction = calculatePredictionScore(beacon, currentTaskLocation, distance);
            if (prediction == null) continue;

            predictionScores.put(prediction.a(), prediction.b());
        }

        // According to TreeMap's sort order, the first entry is the lowest prediction score.
        for (Map.Entry<Double, TaskLocation> entry : predictionScores.entrySet()) {
            TaskLocation closestTaskLocation = entry.getValue();
            Double predictionValue = entry.getKey();

            TaskPrediction oldPrediction = beacons.get(color);
            TaskPrediction newTaskPrediction =
                    new TaskPrediction(beacon, lootrunMarker, distance, closestTaskLocation, predictionValue);

            // If the prediction is the same, don't update.
            if (oldPrediction != null
                    && Objects.equals(oldPrediction.taskLocation(), newTaskPrediction.taskLocation())) {
                if (newTaskPrediction.predictionScore() < oldPrediction.predictionScore()) {
                    // The prediction is the same, but the score is better, so update.
                    beacons.put(color, newTaskPrediction);
                }
                return true;
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
                    foundTask = true;
                    beacons.put(color, newTaskPrediction);
                    beacons.remove(usedTaskPrediction.beacon().beaconKind());

                    // Update the other beacon's prediction.
                    updateTaskLocationPrediction(
                            usedTaskPrediction.beacon(),
                            usedTaskPrediction.lootrunMarker(),
                            usedTaskPrediction.distance());
                    break;
                } else {
                    // We predict that the other beacon is closer to the task location than us.
                    // Use the second best prediction.
                    continue;
                }
            }

            // The prediction is not used by another beacon.
            beacons.put(color, newTaskPrediction);
            foundTask = true;
            break;
        }

        // Finally, update the markers.
        LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();
        return foundTask;
    }

    private Pair<Double, TaskLocation> calculatePredictionScore(
            Beacon beacon, TaskLocation currentTaskLocation, int markerDistance) {
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
        } else {
            // Player Location including Y
            Vector3d playerPosition3d = new Vector3d(
                    McUtils.player().position().x(),
                    McUtils.player().position().y(),
                    McUtils.player().position().z());
            // Task Location including Y
            Vector3d taskLocationPosition3d = new Vector3d(
                    currentTaskLocation.location().x(),
                    currentTaskLocation.location().y(),
                    currentTaskLocation.location().z());

            // Check the difference between the task and the player and the distance provided by the marker
            double taskLocationDistanceToPlayer3d = taskLocationPosition3d.distance(playerPosition3d);
            double distanceDiff = Math.abs(taskLocationDistanceToPlayer3d - markerDistance);

            if (distanceDiff > TASK_DISTANCE_ERROR) {
                // Difference is too different from the given distance from the beacon marker
                return null;
            }
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
}
