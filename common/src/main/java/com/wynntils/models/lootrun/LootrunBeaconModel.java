/*
 * Copyright © Wynntils 2026.
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
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.handlers.particle.event.ParticleVerifiedEvent;
import com.wynntils.handlers.particle.type.ParticleType;
import com.wynntils.mc.extension.EntityExtension;
import com.wynntils.models.beacons.event.BeaconEvent;
import com.wynntils.models.beacons.event.BeaconMarkerEvent;
import com.wynntils.models.beacons.type.Beacon;
import com.wynntils.models.beacons.type.BeaconMarker;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.lootrun.beacons.LootrunBeaconKind;
import com.wynntils.models.lootrun.beacons.LootrunBeaconMarkerKind;
import com.wynntils.models.lootrun.event.LootrunBeaconSelectedEvent;
import com.wynntils.models.lootrun.event.LootrunChallengeCountEvent;
import com.wynntils.models.lootrun.event.LootrunChallengeEvent;
import com.wynntils.models.lootrun.event.LootrunStartedEvent;
import com.wynntils.models.lootrun.event.LootrunStateEvent;
import com.wynntils.models.lootrun.markers.LootrunBeaconMarkerProvider;
import com.wynntils.models.lootrun.particle.LootrunTaskParticleVerifier;
import com.wynntils.models.lootrun.type.LootrunBeaconDetails;
import com.wynntils.models.lootrun.type.LootrunLocation;
import com.wynntils.models.lootrun.type.LootrunTaskType;
import com.wynntils.models.lootrun.type.LootrunningState;
import com.wynntils.models.lootrun.type.TaskLocation;
import com.wynntils.models.lootrun.type.TaskPrediction;
import com.wynntils.models.marker.MarkerModel;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.VectorUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.PosUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.mc.type.PreciseLocation;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import org.joml.Vector2d;
import org.joml.Vector3d;

public final class LootrunBeaconModel extends Model {
    private static final Pattern CHOOSE_BEACON_PATTERN = Pattern.compile("\uDB00\uDC66§6§lChoose a Beacon!");
    private static final Pattern BEACONS_PATTERN = Pattern.compile(
            "[\uDAFF\uDFFF-\uDB00\uDC78]§(?<beaconOneColor>[a-z0-9#]+)§l(?<beaconOneVibrant>Vibrant )?.+? Beacon(§r[\uDAFF\uDFFF-\uDB00\uDC78]§(?<beaconTwoColor>[a-z0-9#]+)§l(?<beaconTwoVibrant>Vibrant )?.+ Beacon)?");
    private static final Pattern ORANGE_AMOUNT_PATTERN =
            Pattern.compile(".*§7.*?(?:for |\\+)(?:§(?:[a-f0-9]|#[a-f0-9]{8}))?(\\d+)(?:§(?:r|7))? Challenges.*");
    private static final Pattern RAINBOW_AMOUNT_PATTERN =
            Pattern.compile(".*§7.*?next (?:§(?:[a-f0-9]|#[a-f0-9]{8}))?(\\d+)(?:§(?:r|7))? Challenges.*");

    private static final float BEACON_REMOVAL_RADIUS = 25f;

    // Beacon positions are sometimes off by a few blocks
    private static final int TASK_POSITION_ERROR = 3;

    // Sometimes the calculated distance between the player and a task is greater than the distance on the marker
    private static final int TASK_DISTANCE_ERROR = 15;

    // Task markers lose their distance number when the player is around this blocks away from the task
    private static final int MARKER_DISTANCE_THRESHOLD = 17;

    private static final LootrunBeaconMarkerProvider LOOTRUN_BEACON_COMPASS_PROVIDER =
            new LootrunBeaconMarkerProvider();

    @Persisted
    public final Config<Boolean> logDebugBeaconInfo = new Config<>(false);

    @Persisted
    private final Storage<Map<String, LootrunBeaconDetails>> lootrunBeaconDetailsStorage =
            new Storage<>(new TreeMap<>());

    private Map<LootrunLocation, Set<TaskLocation>> taskLocations = new HashMap<>();

    // particles can accurately show task locations though are culled so can only be used
    // for 1-4 tasks
    private Set<TaskLocation> possibleTaskLocations = new HashSet<>();

    // rely on color, beacon positions change
    private Map<LootrunBeaconKind, TaskPrediction> beacons = new HashMap<>();
    private Set<LootrunBeaconKind> vibrantBeacons = new HashSet<>();

    private List<Pair<Beacon<LootrunBeaconKind>, EntityExtension>> activeBeacons = new ArrayList<>();
    private Map<LootrunBeaconKind, LootrunTaskType> activeTaskTypes = new HashMap<>();

    // Obscured beacons all share the same color, so they cannot be told apart by kind. This maps
    // beacon entity id -> which of the obscured slots that particular beacon is tracked as.
    private Map<Integer, LootrunBeaconKind> obscuredBeaconSlots = new HashMap<>();

    private boolean hideBeacons = true;
    private boolean expectOrangeBeacon = false;
    private boolean expectRainbowBeacon = false;

    public LootrunBeaconModel(MarkerModel markerModel) {
        super(List.of(markerModel));

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
        registry.registerDownload(UrlId.DATA_STATIC_LOOTRUN_TASKS_NAMED_V2)
                .handleReader(this::handleLootrunTaskLocations);
    }

    @SubscribeEvent
    public void onCharacterChange(CharacterUpdateEvent event) {
        String id = Models.Character.getId();

        lootrunBeaconDetailsStorage.get().putIfAbsent(id, new LootrunBeaconDetails());
        lootrunBeaconDetailsStorage.touched();
    }

    @SubscribeEvent
    public void onLootrunStarted(LootrunStartedEvent event) {
        // Ensure we start with fresh details
        lootrunBeaconDetailsStorage.get().put(Models.Character.getId(), new LootrunBeaconDetails());
        lootrunBeaconDetailsStorage.touched();
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageEvent.Match event) {
        if (event.getRecipientType() != RecipientType.INFO) return;
        StyledText styledText = event.getMessage();

        Matcher matcher = styledText.getMatcher(BEACONS_PATTERN);
        if (matcher.matches()) {
            String beaconOneColorStr = matcher.group("beaconOneColor");
            CustomColor beaconOneColor = beaconOneColorStr.startsWith("#")
                    ? CustomColor.fromHexString(beaconOneColorStr)
                    : CustomColor.fromChatFormatting(ChatFormatting.getByCode(beaconOneColorStr.charAt(0)));
            LootrunBeaconKind beaconOneKind = LootrunBeaconKind.fromColor(beaconOneColor);

            if (beaconOneKind == null) return;

            boolean beaconOneVibrant = matcher.group("beaconOneVibrant") != null;
            if (beaconOneVibrant) {
                vibrantBeacons.add(beaconOneKind);
            }

            expectOrangeBeacon = expectOrangeBeacon || beaconOneKind == LootrunBeaconKind.ORANGE;
            expectRainbowBeacon = expectRainbowBeacon || beaconOneKind == LootrunBeaconKind.RAINBOW;

            String beaconTwoColorStr = matcher.group("beaconTwoColor");

            if (beaconTwoColorStr == null) return;

            CustomColor beaconTwoColor = beaconTwoColorStr.startsWith("#")
                    ? CustomColor.fromHexString(beaconTwoColorStr)
                    : CustomColor.fromChatFormatting(ChatFormatting.getByCode(beaconTwoColorStr.charAt(0)));
            LootrunBeaconKind beaconTwoKind = LootrunBeaconKind.fromColor(beaconTwoColor);

            if (beaconTwoKind == null) return;

            boolean beaconTwoVibrant = matcher.group("beaconTwoVibrant") != null;
            if (beaconTwoVibrant) {
                vibrantBeacons.add(beaconTwoKind);
            }

            expectOrangeBeacon = expectOrangeBeacon || beaconTwoKind == LootrunBeaconKind.ORANGE;
            expectRainbowBeacon = expectRainbowBeacon || beaconTwoKind == LootrunBeaconKind.RAINBOW;

            return;
        }

        if (styledText.matches(CHOOSE_BEACON_PATTERN)) {
            newBeacons();
            return;
        }

        if (expectOrangeBeacon) {
            Matcher orangeMatcher = styledText.getMatcher(ORANGE_AMOUNT_PATTERN);

            if (orangeMatcher.find()) {
                expectOrangeBeacon = false;
                getCurrentLootrunBeaconDetails().setOrangeAmount(Integer.parseInt(orangeMatcher.group(1)));
                lootrunBeaconDetailsStorage.touched();
            }
        }

        if (expectRainbowBeacon) {
            Matcher rainbowMatcher = styledText.getMatcher(RAINBOW_AMOUNT_PATTERN);

            if (rainbowMatcher.find()) {
                expectRainbowBeacon = false;
                getCurrentLootrunBeaconDetails().setRainbowAmount(Integer.parseInt(rainbowMatcher.group(1)));
                lootrunBeaconDetailsStorage.touched();
            }
        }
    }

    // First we get the lootrun particles. This is mainly used for unknown challenges due to particle culling
    // from Wynncraft, but if we have them we can use them to find the task location more accurately
    @SubscribeEvent
    public void onLootrunParticle(ParticleVerifiedEvent event) {
        if (event.getParticle().particleType() != ParticleType.LOOTRUN_TASK) return;
        if (Models.Character.getId().equals("-")) return;

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

        if (!logDebugBeaconInfo.get()) return;

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
    public void onBeaconAdded(BeaconEvent.Added event) {
        Beacon beacon = event.getBeacon();
        if (!(beacon.beaconKind() instanceof LootrunBeaconKind)) return;

        EntityExtension entity = ((EntityExtension) event.getEntity());

        if (hideBeacons) {
            // Only set this once they are added.
            // This is cleaner than posting an event on render
            entity.setRendered(false);
        }

        activeBeacons.add(Pair.of(beacon, entity));
    }

    @SubscribeEvent
    public void onBeaconMarkerAdded(BeaconMarkerEvent.Added event) {
        BeaconMarker beaconMarker = event.getBeaconMarker();
        if (!(beaconMarker.beaconMarkerKind() instanceof LootrunBeaconMarkerKind lootrunMarker)) return;

        EntityExtension entity = (EntityExtension) event.getEntity();

        if (hideBeacons) {
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

        int beaconIndex = findClosestActiveBeaconIndex(beaconMarker);

        if (beaconIndex < 0) {
            entity.setRendered(true);
            return;
        }

        Pair<Beacon<LootrunBeaconKind>, EntityExtension> beaconPair = activeBeacons.get(beaconIndex);
        LootrunBeaconKind beaconKind = resolveObscuredSlot(beaconPair);

        if (beaconKind != beaconPair.a().beaconKind()) {
            // Re-label the beacon we are tracking, so that removal, selection and hiding all agree
            // on which obscured slot this beacon belongs to.
            beaconPair = Pair.of(new Beacon<>(beaconPair.a().position(), beaconKind), beaconPair.b());
            activeBeacons.set(beaconIndex, beaconPair);
        }

        activeTaskTypes.putIfAbsent(beaconKind, lootrunMarker.getTaskType());

        boolean foundBeacon = updateTaskLocationPrediction(
                        beaconPair.a(), lootrunMarker, beaconMarker.distance().get())
                || beacons.containsKey(beaconKind);

        entity.setRendered(!foundBeacon || !hideBeacons);
        beaconPair.b().setRendered(!foundBeacon || !hideBeacons);
    }

    /**
     * Finds the active beacon a marker belongs to. Markers only carry a color, and several beacons can
     * share one (the two obscured beacons of Route Indigo), so the closest matching beacon wins
     * instead of whichever happened to be registered first.
     */
    private int findClosestActiveBeaconIndex(BeaconMarker beaconMarker) {
        if (beaconMarker.color().isEmpty()) return -1;

        CustomColor markerColor = beaconMarker.color().get();
        int closestIndex = -1;
        double closestDistance = Double.MAX_VALUE;

        for (int i = 0; i < activeBeacons.size(); i++) {
            if (!activeBeacons.get(i).a().beaconKind().getCustomColor().equals(markerColor)) continue;

            double distance = distanceSquared(activeBeacons.get(i).a().position(), beaconMarker.position());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestIndex = i;
            }
        }

        return closestIndex;
    }

    /**
     * Obscured beacons all share the same color and therefore the same kind, but two of them can be
     * offered at once, so the second beacon we see is tracked in a second slot. The assignment is
     * sticky per beacon entity so that repeated marker updates stay consistent.
     */
    private LootrunBeaconKind resolveObscuredSlot(Pair<Beacon<LootrunBeaconKind>, EntityExtension> beaconPair) {
        LootrunBeaconKind beaconKind = beaconPair.a().beaconKind();
        if (!beaconKind.isObscured()) return beaconKind;

        int entityId = ((Entity) beaconPair.b()).getId();
        LootrunBeaconKind assignedSlot = obscuredBeaconSlots.get(entityId);
        if (assignedSlot != null) return assignedSlot;

        boolean firstSlotTaken = obscuredBeaconSlots.containsValue(LootrunBeaconKind.OBSCURED);
        assignedSlot = firstSlotTaken ? LootrunBeaconKind.OBSCURED_2 : LootrunBeaconKind.OBSCURED;
        obscuredBeaconSlots.put(entityId, assignedSlot);

        return assignedSlot;
    }

    private int findActiveBeaconIndex(Beacon<?> beacon) {
        for (int i = 0; i < activeBeacons.size(); i++) {
            if (hasSamePosition(activeBeacons.get(i).a(), beacon)) return i;
        }

        return -1;
    }

    private static boolean hasSamePosition(Beacon<?> first, Beacon<?> second) {
        return first.position().x() == second.position().x()
                && first.position().y() == second.position().y()
                && first.position().z() == second.position().z();
    }

    private static double distanceSquared(PreciseLocation position, Vec3 other) {
        double dx = position.x() - other.x;
        double dy = position.y() - other.y;
        double dz = position.z() - other.z;

        return dx * dx + dy * dy + dz * dz;
    }

    @SubscribeEvent
    public void onBeaconMoved(BeaconEvent.Moved event) {
        Beacon beacon = event.getNewBeacon();
        if (!(beacon.beaconKind() instanceof LootrunBeaconKind)) return;

        int beaconIndex = findActiveBeaconIndex(event.getOldBeacon());

        if (beaconIndex < 0) return;

        Pair<Beacon<LootrunBeaconKind>, EntityExtension> oldPair = activeBeacons.get(beaconIndex);

        // Keep the kind we are tracking this beacon as; the event only carries the color-derived
        // kind, which loses the obscured slot assignment.
        Pair<Beacon<LootrunBeaconKind>, EntityExtension> newPair =
                Pair.of(new Beacon<>(beacon.position(), oldPair.a().beaconKind()), oldPair.b());

        activeBeacons.set(beaconIndex, newPair);
    }

    // When we get close to a beacon, it gets removed.
    // This is our signal to know that this can be the current beacon,
    // but we don't know for sure until the scoreboard confirms it.
    @SubscribeEvent
    public void onBeaconRemove(BeaconEvent.Removed event) {
        Beacon beacon = event.getBeacon();
        if (!(beacon.beaconKind() instanceof LootrunBeaconKind color)) return;

        // The event only carries the color-derived kind, so an obscured beacon tracked in the second
        // obscured slot would be confused with the first one. Use the kind we actually track it as.
        int beaconIndex = findActiveBeaconIndex(beacon);
        LootrunBeaconKind trackedKind =
                beaconIndex < 0 ? color : activeBeacons.get(beaconIndex).a().beaconKind();

        Beacon closestBeacon = getClosestBeacon();

        double newBeaconDistanceToPlayer = VectorUtils.distanceIgnoringY(
                beacon.position(), McUtils.mc().player.position());
        double oldBeaconDistanceToPlayer = closestBeacon == null
                ? Double.MAX_VALUE
                : VectorUtils.distanceIgnoringY(
                        closestBeacon.position(), McUtils.mc().player.position());
        if (newBeaconDistanceToPlayer < BEACON_REMOVAL_RADIUS
                && newBeaconDistanceToPlayer <= oldBeaconDistanceToPlayer) {
            setClosestBeacon(
                    beaconIndex < 0 ? beacon : activeBeacons.get(beaconIndex).a());
        } else {
            // Note: If we get more accurate predictions, we don't need to remove if we are close.
            beacons.remove(trackedKind);
            LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();
        }

        if (beaconIndex < 0) {
            activeBeacons.removeIf(beaconPair -> beaconPair.a().beaconKind() == trackedKind);
        } else {
            obscuredBeaconSlots.remove(((Entity) activeBeacons.get(beaconIndex).b()).getId());
            activeBeacons.remove(beaconIndex);
        }
    }

    @SubscribeEvent
    public void onWorldStateChanged(WorldStateEvent event) {
        // The world state event is sometimes late compared to lootrun events (beacons, scoreboard)
        // Resetting once when leaving the class is enough
        if (event.getNewState() == WorldState.WORLD) return;

        possibleTaskLocations = new HashSet<>();
        beacons = new HashMap<>();
        activeBeacons = new ArrayList<>();
        activeTaskTypes = new HashMap<>();
        obscuredBeaconSlots = new HashMap<>();
        LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();
    }

    @SubscribeEvent
    private void onLootrunStateChanged(LootrunStateEvent event) {
        if (event.getNewState() == LootrunningState.NOT_RUNNING) {
            resetBeaconStorage();

            setClosestBeacon(null);
            setLastTaskBeaconColor(null);
            resetBeaconCounts();

            possibleTaskLocations = new HashSet<>();

            beacons = new HashMap<>();
            vibrantBeacons = new HashSet<>();
            return;
        }

        if (event.getOldState() == LootrunningState.CHOOSING_BEACON
                && event.getNewState() == LootrunningState.IN_TASK) {
            // Always reduce beacon counts when starting a new task, regardless of whether
            // we know which beacon was selected. This avoids a race condition where the
            // scoreboard updates to IN_TASK before the beacon removal event sets closestBeacon.
            reduceBeaconCounts();

            Beacon closestBeacon = getClosestBeacon();
            if (closestBeacon != null && closestBeacon.beaconKind() instanceof LootrunBeaconKind color) {
                WynntilsMod.info("Selected a " + color + " beacon at " + closestBeacon.position());
                getCurrentLootrunBeaconDetails().incrementBeaconCount(color);
                lootrunBeaconDetailsStorage.touched();

                setLastTaskBeaconColor(color);
                WynntilsMod.postEvent(new LootrunBeaconSelectedEvent(
                        closestBeacon,
                        beacons.get(closestBeacon.beaconKind()).taskLocation(),
                        activeTaskTypes.getOrDefault(closestBeacon.beaconKind(), LootrunTaskType.UNKNOWN)));
            } else {
                WynntilsMod.warn("Started a task but closestBeacon was not set; beacon-specific tracking skipped");
                // Clear stale color to prevent challengeCompleted() from incorrectly
                // adding rainbow/orange counts based on the previous challenge's color.
                setLastTaskBeaconColor(null);
            }

            possibleTaskLocations = new HashSet<>();

            // We selected a beacon, so other beacons are no longer relevant.
            beacons.clear();
            vibrantBeacons.clear();
            activeBeacons.clear();
            activeTaskTypes.clear();
            obscuredBeaconSlots.clear();
            setClosestBeacon(null);
            LOOTRUN_BEACON_COMPASS_PROVIDER.reloadTaskMarkers();
        }
    }

    @SubscribeEvent
    public void onChallengeCompleted(LootrunChallengeEvent.Completed event) {
        LootrunBeaconKind color = getLastTaskBeaconColor();
        LootrunBeaconDetails lootrunBeaconDetails = getCurrentLootrunBeaconDetails();

        if (color == LootrunBeaconKind.RAINBOW) {
            if (lootrunBeaconDetails.getRainbowAmount() != -1) {
                int oldCount = lootrunBeaconDetails.getRainbowBeaconCount();

                int newCount = Math.max(oldCount + lootrunBeaconDetails.getRainbowAmount(), 0);
                lootrunBeaconDetails.setRainbowBeaconCount(newCount);
            } else {
                WynntilsMod.warn("Completed rainbow beacon challenge but had no rainbow amount");
            }
        } else if (color == LootrunBeaconKind.ORANGE) {
            if (lootrunBeaconDetails.getOrangeAmount() != -1) {
                List<Integer> orangeList =
                        new ArrayList<>(getCurrentLootrunBeaconDetails().getOrangeBeaconCounts());

                orangeList.add(lootrunBeaconDetails.getOrangeAmount());
                lootrunBeaconDetails.setOrangeBeaconCounts(orangeList);
            } else {
                WynntilsMod.warn("Completed orange beacon challenge but had no orange amount");
            }
        }

        lootrunBeaconDetails.setOrangeAmount(-1);
        lootrunBeaconDetails.setRainbowAmount(-1);
        lootrunBeaconDetailsStorage.get().put(Models.Character.getId(), lootrunBeaconDetails);
    }

    @SubscribeEvent
    public void onChallengeFailed(LootrunChallengeEvent.Failed event) {
        getCurrentLootrunBeaconDetails().setOrangeAmount(-1);
        getCurrentLootrunBeaconDetails().setRainbowAmount(-1);
        lootrunBeaconDetailsStorage.touched();
    }

    @SubscribeEvent
    public void onLootrunChallengeCountUpdate(LootrunChallengeCountEvent event) {
        CappedValue newCount = event.getNewCount();
        CappedValue oldCount = event.getOldCount();

        // First, check if we completed a challenge.
        if (newCount.current() > oldCount.current()) {
            addToRedBeaconTaskCount(-1);
        }

        // Then, check if we completed have new challenges from a red beacon.
        if (getLastTaskBeaconColor() == LootrunBeaconKind.RED && newCount.max() > oldCount.max()) {
            addToRedBeaconTaskCount(newCount.max() - oldCount.max());
        }
    }

    public Map<LootrunBeaconKind, TaskPrediction> getBeacons() {
        return Collections.unmodifiableMap(beacons);
    }

    public int getBeaconCount(LootrunBeaconKind color) {
        return getCurrentLootrunBeaconDetails().getSelectedBeacons().getOrDefault(color, 0);
    }

    public LootrunBeaconKind getLastTaskBeaconColor() {
        return getCurrentLootrunBeaconDetails().getLastTaskBeaconColor();
    }

    public boolean isBeaconVibrant(LootrunBeaconKind lootrunBeaconKind) {
        // Both obscured slots represent the same in-game beacon, so they share the vibrant flag.
        if (lootrunBeaconKind.isObscured()) {
            return vibrantBeacons.contains(LootrunBeaconKind.OBSCURED);
        }

        return vibrantBeacons.contains(lootrunBeaconKind);
    }

    public TaskLocation getTaskForColor(LootrunBeaconKind lootrunBeaconKind) {
        TaskPrediction taskPrediction = beacons.get(lootrunBeaconKind);
        if (taskPrediction == null) return null;

        return taskPrediction.taskLocation();
    }

    private void setLastTaskBeaconColor(LootrunBeaconKind lootrunBeaconKind) {
        getCurrentLootrunBeaconDetails().setLastTaskBeaconColor(lootrunBeaconKind);
        getCurrentLootrunBeaconDetails().setLastTaskVibrantBeacon(vibrantBeacons.contains(lootrunBeaconKind));
        lootrunBeaconDetailsStorage.touched();
    }

    private void setClosestBeacon(Beacon beacon) {
        getCurrentLootrunBeaconDetails().setClosestBeacon(beacon);
        lootrunBeaconDetailsStorage.touched();
    }

    private void resetBeaconStorage() {
        getCurrentLootrunBeaconDetails().setSelectedBeacons(new TreeMap<>());
        lootrunBeaconDetailsStorage.touched();
    }

    public Beacon getClosestBeacon() {
        return getCurrentLootrunBeaconDetails().getClosestBeacon();
    }

    public boolean wasLastBeaconVibrant() {
        return getCurrentLootrunBeaconDetails().getLastTaskVibrantBeacon();
    }

    public int getRedBeaconTaskCount() {
        return getCurrentLootrunBeaconDetails().getRedBeaconTaskCount();
    }

    public int getActiveOrangeBeacons() {
        return getCurrentLootrunBeaconDetails().getOrangeBeaconCounts().size();
    }

    public int getChallengesTillNextOrangeExpires() {
        List<Integer> orangeBeaconCounts = getCurrentLootrunBeaconDetails().getOrangeBeaconCounts();

        if (orangeBeaconCounts.isEmpty()) {
            return 0;
        } else {
            return Collections.min(orangeBeaconCounts);
        }
    }

    public int getActiveRainbowBeacons() {
        return getCurrentLootrunBeaconDetails().getRainbowBeaconCount();
    }

    public void toggleHideBeacons(boolean shouldHide) {
        hideBeacons = shouldHide;

        for (Pair<Beacon<LootrunBeaconKind>, EntityExtension> beaconPair : activeBeacons) {
            // Only change visibility if it has been found, otherwise we need to keep the vanilla beacon
            if (beacons.containsKey(beaconPair.a().beaconKind())) {
                beaconPair.b().setRendered(!hideBeacons);
            }
        }
    }

    private void newBeacons() {
        possibleTaskLocations.clear();
        vibrantBeacons.clear();
    }

    private void reduceBeaconCounts() {
        LootrunBeaconDetails lootrunBeaconDetails = getCurrentLootrunBeaconDetails();
        int oldRainbowCount = lootrunBeaconDetails.getRainbowBeaconCount();

        if (oldRainbowCount > 0) {
            int newCount = oldRainbowCount - 1;
            lootrunBeaconDetails.setRainbowBeaconCount(newCount);
        }

        List<Integer> orangeCounts = getOrangeCounts(lootrunBeaconDetails);
        lootrunBeaconDetails.setOrangeBeaconCounts(orangeCounts);
        lootrunBeaconDetailsStorage.get().put(Models.Character.getId(), lootrunBeaconDetails);
    }

    private List<Integer> getOrangeCounts(LootrunBeaconDetails lootrunBeaconDetails) {
        List<Integer> orangeCounts = new ArrayList<>(lootrunBeaconDetails.getOrangeBeaconCounts());

        if (!orangeCounts.isEmpty()) {
            ListIterator<Integer> orangeIterator = orangeCounts.listIterator();

            while (orangeIterator.hasNext()) {
                int currentOrangeCount = orangeIterator.next();
                currentOrangeCount--;

                orangeIterator.remove();
                if (currentOrangeCount > 0) {
                    orangeIterator.add(currentOrangeCount);
                }
            }
        }
        return orangeCounts;
    }

    private void addToRedBeaconTaskCount(int changeAmount) {
        int oldCount = getCurrentLootrunBeaconDetails().getRedBeaconTaskCount();

        int newCount = Math.max(oldCount + changeAmount, 0);
        getCurrentLootrunBeaconDetails().setRedBeaconTaskCount(newCount);
        lootrunBeaconDetailsStorage.touched();
    }

    private void resetBeaconCounts() {
        getCurrentLootrunBeaconDetails().setRedBeaconTaskCount(0);
        getCurrentLootrunBeaconDetails().setOrangeBeaconCounts(new ArrayList<>());
        getCurrentLootrunBeaconDetails().setRainbowBeaconCount(0);
        lootrunBeaconDetailsStorage.touched();
    }

    private boolean updateTaskLocationPrediction(Beacon beacon, LootrunBeaconMarkerKind lootrunMarker, int distance) {
        if (!(beacon.beaconKind() instanceof LootrunBeaconKind color)) return false;

        boolean foundTask = false;
        // Get the tasks found from particles as we know for certain there is a task there and it may include
        // unknown tasks
        Set<TaskLocation> currentTaskLocations = possibleTaskLocations.stream()
                .filter(possibleTask -> possibleTask.taskType() == lootrunMarker.getTaskType()
                        || possibleTask.taskType() == LootrunTaskType.UNKNOWN)
                .collect(Collectors.toSet());

        // Due to Wynncraft culling particles, after 5 or more beacon choices (sometimes less) we are no longer able
        // to rely on those for getting possible locations so we use the gathered task locations instead and we can
        // filter them based on the marker provided. The distance from the marker is also used to filter far
        // away tasks so whilst it would be ideal to only get tasks from current location this is fine for now.
        taskLocations
                .values()
                .forEach(hashSet -> currentTaskLocations.addAll(hashSet.stream()
                        .filter(task -> task.taskType() == lootrunMarker.getTaskType())
                        .collect(Collectors.toSet())));

        if (currentTaskLocations.isEmpty()) {
            WynntilsMod.warn("No task locations found!");
            return false;
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

    private void handleLootrunTaskLocations(Reader reader) {
        Type type = new TypeToken<Map<LootrunLocation, Set<TaskLocation>>>() {}.getType();
        taskLocations = Managers.Json.GSON.fromJson(reader, type);
    }

    private LootrunBeaconDetails getCurrentLootrunBeaconDetails() {
        return lootrunBeaconDetailsStorage.get().getOrDefault(Models.Character.getId(), new LootrunBeaconDetails());
    }
}
